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

    public static void v(String msg)
    {
        Log.v(TAG, msg);
        module.log(Log.VERBOSE, TAG, msg);
    }

    public static void d(String msg)
    {
        Log.d(TAG, msg);
        module.log(Log.DEBUG, TAG, msg);
    }

    public static void i(String msg)
    {
        Log.i(TAG, msg);
        module.log(Log.INFO, TAG, msg);
    }

    public static void w(String msg)
    {
        Log.w(TAG, msg);
        module.log(Log.WARN, TAG, msg);
    }

    public static void e(String msg)
    {
        Log.e(TAG, msg);
        module.log(Log.ERROR, TAG, msg);
    } 
    
    public static void e(String msg, Throwable t)
    {
        Log.e(TAG, msg, t);
        module.log(Log.ERROR, TAG, msg, t);
    }

    public static void e(Throwable t)
    {
        Log.e(TAG, "", t);
        module.log(Log.ERROR, TAG, "", t);
    }
}
