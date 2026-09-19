package com.ehvn.zaloxposed.hooks.morphe;

import android.annotation.SuppressLint;

import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Logger;

import java.lang.reflect.Method;

public class SpoofPackageNameHook extends BaseHook
{
    @SuppressLint("PrivateApi")
    @Override
    public void hook() throws Throwable
    { 
        Method getPackageName = Class.forName("android.app.ContextImpl", false, classLoader).getDeclaredMethod("getPackageName");
        Logger.i("Hooking: " + getPackageName);
        module.hook(getPackageName).intercept(chain ->
        {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            boolean calledFromNative = false;
            for (StackTraceElement element : stackTrace)
            {
                if (element.isNativeMethod())
                {
                    if (element.getMethodName().equals("getStaticValue"))
                    {
                        calledFromNative = true;
                        break;
                    }
                }
                if (element.getClassName().equals(Runtime.class.getName()))
                {
                    if (element.getMethodName().equals("nativeLoad") || element.getMethodName().equals("loadLibrary0"))
                    {
                        calledFromNative = true;
                        break;
                    }
                }
            }
            if (!calledFromNative)
                return chain.proceed();
            return "com.zing.zalo";
        });
    }
}
