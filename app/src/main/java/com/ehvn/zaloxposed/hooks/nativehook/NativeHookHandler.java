package io.github.electroheavenvn.zaloxposed.hooks.nativehook;

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
        String methodName = method.getName();
        if ("intercept".equals(methodName) && args != null && args.length > 0)
            return dispatch(nativePtr, args[0]);
        if ("hashCode".equals(methodName)) 
            return System.identityHashCode(proxy);
        if ("equals".equals(methodName)) 
            return proxy == args[0];
        if ("toString".equals(methodName)) 
            return "NativeHookHandlerProxy";
        return null;
    }

    private static native Object dispatch(long ptr, Object chain);
}