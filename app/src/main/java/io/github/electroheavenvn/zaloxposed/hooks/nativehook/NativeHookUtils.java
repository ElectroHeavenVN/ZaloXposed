package io.github.electroheavenvn.zaloxposed.hooks.nativehook;

import java.lang.reflect.Proxy;

import io.github.libxposed.api.XposedInterface;

public final class NativeHookUtils
{
    private NativeHookUtils() { }

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