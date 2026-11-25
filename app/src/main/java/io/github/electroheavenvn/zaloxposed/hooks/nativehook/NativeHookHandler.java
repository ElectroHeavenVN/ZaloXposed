package io.github.electroheavenvn.zaloxposed.hooks.nativehook;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import io.github.electroheavenvn.zaloxposed.utilities.Logger;
import io.github.libxposed.api.XposedInterface;

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
        {
            try
            {
                return dispatch(nativePtr, args[0]);
            }
            catch (Exception e) { Logger.e(e); }
            if (args[0] instanceof XposedInterface.Chain chain)
                return chain.proceed();
        }
        return switch (methodName)
        {
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            case "toString" -> "NativeHookHandlerProxy";
            default -> null;
        };
    }

    private static native Object dispatch(long ptr, Object chain);
}