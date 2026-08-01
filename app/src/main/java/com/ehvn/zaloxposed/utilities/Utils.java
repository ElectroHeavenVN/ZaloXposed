package com.ehvn.zaloxposed.utilities;

import android.util.Log;
import java.lang.reflect.Field;
import de.robv.android.xposed.*;
import java.util.List;
import java.lang.reflect.Method;
import java.io.*;
import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.result.MethodData;

public final class Utils {
    private static final String TAG = "ZaloXposed";
    private static ClassLoader sClassLoader;
    private static String externalFilesDir = "";
    private static Class<?> cfgClass;
    private static Method getCurrentUserInfoMethod;

    public static void Init(ClassLoader classLoader, DexKitBridge bridge) throws NoSuchMethodException
    {
        sClassLoader = classLoader;
         List<MethodData> methods = bridge.findMethod(
            FindMethod.create()
                .matcher(MethodMatcher.create()
                .paramCount(0)
                .returnType("int")
                .addUsingString("CHAT_MULTI_SELECTION_MAX_NUMBER_OF_SELECTED_MESSAGES", StringMatchType.Equals)
                )
         );
         cfgClass = methods.isEmpty() ? null : methods.get(0).getMethodInstance(sClassLoader).getDeclaringClass();
         methods = bridge.findMethod(
            FindMethod.create()
                .matcher(MethodMatcher.create()
                .paramCount(0)
                .returnType("java.lang.String")
                .addUsingString("UserInfo", StringMatchType.Equals)
                )
         );
         getCurrentUserInfoMethod = methods.isEmpty() ? null : methods.get(0).getMethodInstance(sClassLoader);
    }

    public static String GetCurrentUserID() 
    {
        try {
            Class<?> clazz = Class.forName("com.zing.zalocore.CoreUtility", true, sClassLoader);
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getType() != String.class) 
                    continue;
                field.setAccessible(true);
                String value = (String) field.get(null);
                if (value != null && value.matches("\\d+")) 
                    return value;
            }
        } catch (Exception e) {
            Log.e(TAG, "GetCurrentUserID error: " + e.getMessage());
        }
        return "0";
    }

    public static String GetCurrentUserToken() 
    {
        try {
            String userID = GetCurrentUserID();
            if (userID.isEmpty()) 
                return "";
            Class<?> clazz = Class.forName("com.zing.zalocore.CoreUtility", true, sClassLoader);
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getType() != String.class) 
                    continue;
                field.setAccessible(true);
                String value = (String) field.get(null);
                if (value != null && value.contains(userID) && !value.equals(userID)) 
                    return value;
            }
        } catch (Exception e) {
            Log.e(TAG, "GetCurrentUserToken error: " + e.getMessage());
        }
        return "";
    }

    public static String GetAppVersion() 
    {
        try {
            Class<?> clazz = Class.forName("com.zing.zalocore.CoreUtility", true, sClassLoader);
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getType() != String.class) 
                    continue;
                field.setAccessible(true);
                String value = (String) field.get(null);
                if (value != null && value.matches("\\d{2}\\.\\d{2}\\.\\d{2}")) 
                    return value;
            }
        } catch (Exception e) {
            Log.e(TAG, "GetAppVersion error: " + e.getMessage());
        }
        return "";
    }

    public static String GetStackTrace() 
    {
        Throwable throwable = new Throwable();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    public static String GetCurrentUserInfo()
    {
        if (getCurrentUserInfoMethod == null)
            return "";
        try {
            Object userInfo = getCurrentUserInfoMethod.invoke(null);
            if (userInfo != null) {
                return userInfo.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, "GetCurrentUserInfo error: " + e.getMessage());
        }
        return "";
    }

    public static String GetExternalFilesDir()
    {
        loadExternalFilesDir();
        return externalFilesDir;
    }

    private static void loadExternalFilesDir()
    {
        if (!externalFilesDir.isEmpty()) 
            return;
        try {
            Object app = XposedHelpers.callStaticMethod(Class.forName("android.app.ActivityThread"), "currentApplication");
            if (app != null) {
                File dir = ((android.content.Context) app).getExternalFilesDir(null);
                if (dir != null)
                    externalFilesDir = dir.getAbsolutePath();
            }
        } catch (Exception e) {
            XposedBridge.log("[ZaloXposed] Cannot get externalFilesDir: " + e.getMessage());
        }
        if (!externalFilesDir.isEmpty())
            XposedBridge.log("[ZaloXposed] externalFilesDir: " + externalFilesDir);
    }
}