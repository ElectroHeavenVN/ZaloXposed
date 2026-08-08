package com.ehvn.zaloxposed.utilities;

import android.util.Log;

import io.github.libxposed.api.XposedModule;

public final class Logger
{
    private Logger() { }

    private static XposedModule module;

    private static final String TAG = "ZaloXposed";

    public static void Init(XposedModule xposedModule)
    {
        module = xposedModule;
    }

    private static String getCallerInfo()
    {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 3; i < stackTrace.length; i++)
        {
            StackTraceElement element = stackTrace[i];
            String className = element.getClassName();
            if (!className.equals(Logger.class.getName())) 
            {
                String simpleName = className.substring(className.lastIndexOf('.') + 1);
                return "[" + simpleName + "] ";
            }
        }
        return "";
    }

    public static void v(String msg)
    {
        String callerInfo = getCallerInfo();
        Log.v(TAG, callerInfo + msg);
        module.log(Log.VERBOSE, TAG, callerInfo + msg);
    }

    public static void d(String msg)
    {
        String callerInfo = getCallerInfo();
        Log.d(TAG, callerInfo + msg);
        module.log(Log.DEBUG, TAG, callerInfo + msg);
    }

    public static void i(String msg)
    {
        String callerInfo = getCallerInfo();
        Log.i(TAG, callerInfo + msg);
        module.log(Log.INFO, TAG, callerInfo + msg);
    }

    public static void w(String msg)
    {
        String callerInfo = getCallerInfo();
        Log.w(TAG, callerInfo + msg);
        module.log(Log.WARN, TAG, callerInfo + msg);
    }

    public static void e(String msg)
    {
        String callerInfo = getCallerInfo();
        Log.e(TAG, callerInfo + msg);
        module.log(Log.ERROR, TAG, callerInfo + msg);
    }

    public static void e(String msg, Throwable t)
    {
        String callerInfo = getCallerInfo();
        Log.e(TAG, callerInfo + msg, t);
        module.log(Log.ERROR, TAG, callerInfo + msg, t);
    }

    public static void e(Throwable t)
    {
        String callerInfo = getCallerInfo();
        Log.e(TAG, callerInfo, t);
        module.log(Log.ERROR, TAG, callerInfo, t);
    }
}