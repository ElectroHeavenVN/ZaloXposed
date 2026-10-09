package com.ehvn.zaloxposed.hooks.nativehook;

import com.ehvn.zaloxposed.hooks.BaseHook;

import java.lang.reflect.Executable;

import io.github.libxposed.api.XposedInterface;

public abstract class BaseNativeHook extends BaseHook
{
    @Override
    public final void hook() throws Throwable
    {
        nativeHook();
    }

    public abstract void nativeHook() throws Throwable;

    public Object getModule()
    {
        return module;
    }

    public Object doHook(Executable origin, long nativePtr)
    {
        XposedInterface.Hooker xHooker = NativeHookUtils.CreateXHookerProxy(nativePtr);
        return module.hook(origin).intercept(xHooker);
    }

    public Object doHook(Executable origin, int priority, long nativePtr)
    {
        XposedInterface.Hooker xHooker = NativeHookUtils.CreateXHookerProxy(nativePtr);
        return module.hook(origin).setPriority(priority).intercept(xHooker); 
    }
    
    public Object doHook(Executable origin, Object hooker)
    {
        if (!(hooker instanceof XposedInterface.Hooker xHooker))
            throw new IllegalArgumentException("hooker must be an instance of XposedInterface.Hooker");
        return module.hook(origin).intercept(xHooker);
    }

    public Object doHook(Executable origin, int priority, Object hooker)
    {
        if (!(hooker instanceof XposedInterface.Hooker xHooker))
            throw new IllegalArgumentException("hooker must be an instance of XposedInterface.Hooker");
        return module.hook(origin).setPriority(priority).intercept(xHooker);
    }
}
