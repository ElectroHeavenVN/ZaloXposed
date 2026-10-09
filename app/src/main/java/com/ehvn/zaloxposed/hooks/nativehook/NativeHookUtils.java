package com.ehvn.zaloxposed.hooks.nativehook;

import java.lang.reflect.Proxy;

import io.github.libxposed.api.XposedInterface;

public final class NativeHookUtils
{
    private NativeHookUtils() { }

    // We cannot cast the return value to XposedInterface.Hooker
    // as it would defeat the purpose of creating a Java proxy 
    // for calling libxposed code.
    // Maybe keep 1 hooker proxy for multiple similar hooks?
    public static XposedInterface.Hooker CreateXHookerProxy(long nativePtr)
    {
        NativeHookHandler handler = new NativeHookHandler(nativePtr);
        Class<?>[] interfaces = new Class<?>[]
            {
                XposedInterface.Hooker.class,
            };
        return (XposedInterface.Hooker)Proxy.newProxyInstance(XposedInterface.Hooker.class.getClassLoader(), interfaces, handler);
    }

    public static Object CreateHookerProxy(long nativePtr)
    {
        return CreateXHookerProxy(nativePtr);
    }
}
