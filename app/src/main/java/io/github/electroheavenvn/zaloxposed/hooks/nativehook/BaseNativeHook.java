package io.github.electroheavenvn.zaloxposed.hooks.nativehook;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;

import java.lang.reflect.Executable;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;

public abstract class BaseNativeHook extends BaseHook
{
    private static XposedModule module;

    public static void setModule(XposedModule module) { BaseNativeHook.module = module; }

    public abstract void installHooks() throws Throwable;

    protected Object doHook(Executable origin, Object hooker)
    {
        if (!(hooker instanceof XposedInterface.Hooker xHooker))
            throw new IllegalArgumentException("hooker must be an instance of XposedInterface.Hooker");
        return module.hook(origin).intercept(xHooker);
    }

    protected Object doHook(Executable origin, int priority, Object hooker)
    {
        if (!(hooker instanceof XposedInterface.Hooker xHooker))
            throw new IllegalArgumentException("hooker must be an instance of XposedInterface.Hooker");
        return module.hook(origin).setPriority(priority).intercept(xHooker);
    }

    protected Object doHook(Executable origin, long nativePtr)
    {
        return doHook(origin, NativeHookUtils.CreateXHookerProxy(nativePtr));
    }

    protected Object doHook(Executable origin, int priority, long nativePtr)
    {
        return doHook(origin, priority, NativeHookUtils.CreateXHookerProxy(nativePtr));
    }

    protected Object doHookClInit(Class<?> origin, Object hooker)
    {
        if (!(hooker instanceof XposedInterface.Hooker xHooker))
            throw new IllegalArgumentException("hooker must be an instance of XposedInterface.Hooker");
        return module.hookClassInitializer(origin).intercept(xHooker);
    }

    protected Object doHookClInit(Class<?> origin, int priority, Object hooker)
    {
        if (!(hooker instanceof XposedInterface.Hooker xHooker))
            throw new IllegalArgumentException("hooker must be an instance of XposedInterface.Hooker");
        return module.hookClassInitializer(origin).setPriority(priority).intercept(xHooker);
    }

    protected Object doHookClInit(Class<?> origin, long nativePtr)
    {
        return doHookClInit(origin, NativeHookUtils.CreateXHookerProxy(nativePtr));
    }

    protected Object doHookClInit(Class<?> origin, int priority, long nativePtr)
    {
        return doHookClInit(origin, priority, NativeHookUtils.CreateXHookerProxy(nativePtr));
    }
}