package com.ehvn.zaloxposed.utilities;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class CoreUtilityHelper
{
    private CoreUtilityHelper() { }

    private static Class<?> coreUtility = null;
    private static Field myUserIDField = null;
    private static Field myUserTokenField = null;
    private static Field appVersionField = null;

    public static String GetCurrentUserID()
    {
        try
        {
            loadCoreUtility();
            return (String)myUserIDField.get(null);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
        return "0";
    }

    public static String GetCurrentUserToken()
    {
        try
        {
            loadCoreUtility();
            return (String)myUserTokenField.get(null);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
        return "";
    }

    public static String GetAppVersion()
    {
        try
        {
            loadCoreUtility();
            return (String)appVersionField.get(null);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
        return "";
    }

    private static void loadCoreUtility() throws ClassNotFoundException, IllegalAccessException
    {
        if (coreUtility != null)
            return;
        coreUtility = Class.forName("com.zing.zalocore.CoreUtility", true, Utils.GetClassLoader());
        for (Field field : coreUtility.getDeclaredFields())
        {
            field.setAccessible(true);
            // Logger.i("com.zing.zalocore.CoreUtility::" + field.getName() + " (" + field.getType().getTypeName() + ") = " + field.get(null));
            if (!Modifier.isStatic(field.getModifiers()))
                continue;
            if (field.getType() == String.class)
            {
                String value = (String)field.get(null);
                if (value == null)
                    continue;
                if (value.matches("\\d{2}\\.\\d{2}\\.\\d{2}"))
                    appVersionField = field;
                else if (value.matches("\\d+"))
                    myUserIDField = field;
                // token may not contain dashes, include it anyway
                else if (value.matches("\\w{4}\\.\\d{9}\\.a[0-1]\\.[\\w-]*"))
                    myUserTokenField = field;
            }
        }
        // if (myUserIDField == null)
        //     throw new ClassNotFoundException("myUserIDField not found");
        // String userID = (String)myUserIDField.get(null);
        // if (userID == null)
        //     throw new ClassNotFoundException("userID is null");
        // for (Field field : coreUtility.getDeclaredFields())
        // {
        //     if (!Modifier.isStatic(field.getModifiers()))
        //         continue;
        //     if (field.getType() != String.class)
        //         continue;
        //     field.setAccessible(true);
        //     String value = (String)field.get(null);
        //     if (value == null)
        //         continue;
        //     if (value.contains(userID) && !value.equals(userID))
        //         myUserTokenField = field;
        // }
    }
}
