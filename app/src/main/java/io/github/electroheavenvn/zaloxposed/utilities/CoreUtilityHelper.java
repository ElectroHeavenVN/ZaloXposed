package io.github.electroheavenvn.zaloxposed.utilities;

import android.content.Context;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class CoreUtilityHelper
{
    private CoreUtilityHelper() { }

    private static Class<?> coreUtility = null;
    private static Field myUserIDField = null;
    private static Field myUserTokenField = null;

    public static String GetCurrentUserID()
    {
        try
        {
            loadCoreUtility();
            return (String)myUserIDField.get(null);
        }
        catch (Exception e) { Logger.e(e); }
        return "0";
    }

    public static String GetCurrentUserToken()
    {
        try
        {
            loadCoreUtility();
            return (String)myUserTokenField.get(null);
        }
        catch (Exception e) { Logger.e(e); }
        return "";
    }

    public static Context GetAppContext()
    {
        try
        {
            loadCoreUtility();
            return (Context)coreUtility.getDeclaredMethod("getAppContext").invoke(null);
        }
        catch (Exception e) { Logger.e(e); }
        return null;
    }

    private static void loadCoreUtility() throws ClassNotFoundException, IllegalAccessException
    {
        if (coreUtility != null)
            return;
        coreUtility = Class.forName("com.zing.zalocore.CoreUtility", true, Utils.GetClassLoader());
        for (Field field : coreUtility.getDeclaredFields())
        {
            field.setAccessible(true);
            if (!Modifier.isStatic(field.getModifiers()))
                continue;
            if (field.getType() == String.class)
            {
                String value = (String)field.get(null);
                if (value == null)
                    continue;
                if (value.matches("\\d+"))
                    myUserIDField = field;
                // token may not contain dashes, include it anyway
                else if (value.matches("\\w{4}\\.\\d{9}\\.a[0-1]\\.[\\w-]*"))
                    myUserTokenField = field;
            }
        }
    }
}
