package io.github.electroheavenvn.zaloxposed.utilities;

import android.util.Log;

import java.lang.reflect.Executable;

import io.github.libxposed.api.XposedModule;

public final class Logger
{
    private Logger() { }

    private static XposedModule module;

    public static final String TAG = "ZaloXposed";

    private static final String RESET = "\u001B[0m";
    private static final String GRAY = "\u001B[90m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[38;2;0;255;255m";
    private static final String ORANGE = "\u001B[38;5;208m";

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
            if (className.equals(Logger.class.getName()))
                continue;
            String simpleName = className.substring(className.lastIndexOf('.') + 1);
            return "[" + simpleName + "] ";
        }
        return "";
    }

    private static String formatCaller(String callerInfo)
    {
        return callerInfo.isEmpty() ? "" : CYAN + callerInfo + RESET;
    }

    public static void v(String msg)
    {
        String callerInfo = getCallerInfo();
        Log.v(TAG, formatCaller(callerInfo) + GRAY + msg + RESET);
        if (module != null)
            module.log(Log.VERBOSE, TAG, callerInfo + msg);
    }

    public static void d(String msg)
    {
        String callerInfo = getCallerInfo();
        Log.d(TAG, formatCaller(callerInfo) + GREEN + msg + RESET);
        if (module != null)
            module.log(Log.DEBUG, TAG, callerInfo + msg);
    }

    public static void i(String msg)
    {
        String callerInfo = getCallerInfo();
        Log.i(TAG, formatCaller(callerInfo) + msg);
        if (module != null)
            module.log(Log.INFO, TAG, callerInfo + msg);
    }

    public static void w(String msg)
    {
        String callerInfo = getCallerInfo();
        Log.w(TAG, formatCaller(callerInfo) + YELLOW + msg + RESET);
        if (module != null)
            module.log(Log.WARN, TAG, callerInfo + msg);
    }

    public static void e(String msg)
    {
        String callerInfo = getCallerInfo();
        Log.e(TAG, formatCaller(callerInfo) + RED + msg + RESET);
        if (module != null)
            module.log(Log.ERROR, TAG, callerInfo + msg);
    }

    public static void e(String msg, Throwable t)
    {
        String callerInfo = getCallerInfo();
        String colored = formatCaller(callerInfo) + RED + msg + RESET;
        if (t != null)
            colored += "\n" + ORANGE + Log.getStackTraceString(t).trim() + RESET;
        Log.e(TAG, colored);
        if (module != null)
            module.log(Log.ERROR, TAG, callerInfo + msg, t);
    }

    public static void e(Throwable t)
    {
        String callerInfo = getCallerInfo();
        String colored;
        if (t != null)
            colored = (callerInfo.isEmpty() ? "" : formatCaller(callerInfo) + "\n") + ORANGE + Log.getStackTraceString(t).trim() + RESET;
        else
            colored = formatCaller(callerInfo) + RED + "null" + RESET;
        Log.e(TAG, colored);
        if (module != null)
            module.log(Log.ERROR, TAG, callerInfo, t);
    }
}