package com.ehvn.zaloxposed.hooks.nativehook;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class NativeHookHandler implements InvocationHandler
{
    private final long nativePtr;

    public NativeHookHandler(long nativePtr)
    {
        this.nativePtr = nativePtr;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        if ("intercept".equals(method.getName()) && args != null && args.length > 0)
            return dispatch(nativePtr, args[0]);
        return null;
    }

    private static native Object dispatch(long ptr, Object chain);
}