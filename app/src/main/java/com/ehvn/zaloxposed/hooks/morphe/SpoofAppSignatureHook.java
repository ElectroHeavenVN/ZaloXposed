package com.ehvn.zaloxposed.hooks.morphe;

import com.ehvn.zaloxposed.hooks.nativehook.BaseNativeHook;

public class SpoofAppSignatureHook extends BaseNativeHook
{
    @Override
    public native void nativeHook() throws Throwable;

    // @Override
    // public void hook() throws Throwable
    // {
    //     Method method = methods.get(0).getMethodInstance(classLoader);
    //     Method getAppContext = Class.forName("com.zing.zalo.MainApplication", false, classLoader).getMethod("getAppContext");
    //     new Thread(() ->
    //     {
    //         while (true)
    //         {
    //             try
    //             {
    //                 Thread.sleep(10000);
    //                 Object value = method.invoke(null, getAppContext.invoke(null));
    //                 Logger.i("SHA1 signature value: " + value);
    //             }
    //             catch (Exception e)
    //             {
    //                 Logger.e(e);
    //             }
    //         }
    //     }).start();
    // }
}