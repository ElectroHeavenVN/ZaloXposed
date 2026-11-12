package io.github.electroheavenvn.zaloxposed.hooks.nativehook;

import android.util.Log;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;

import java.lang.reflect.Executable;

import io.github.electroheavenvn.zaloxposed.utilities.Logger;
import io.github.libxposed.api.XposedInterface;

public abstract class BaseNativeHook extends BaseHook
{
    public abstract void installHooks() throws Throwable;

    protected Object doHook(Executable origin, Object hooker)
    {
        if (!(hooker instanceof XposedInterface.Hooker xHooker))
            throw new IllegalArgumentException("hooker must be an instance of XposedInterface.Hooker");
        return doHook(origin, xHooker);
    }

    protected Object doHook(Executable origin, int priority, Object hooker)
    {
        if (!(hooker instanceof XposedInterface.Hooker xHooker))
            throw new IllegalArgumentException("hooker must be an instance of XposedInterface.Hooker");
        return doHook(origin, priority, xHooker);
    }

    protected Object doHook(Executable origin, long nativePtr) { return doHook(origin, NativeHookUtils.CreateXHookerProxy(nativePtr)); }

    protected Object doHook(Executable origin, int priority, long nativePtr) { return doHook(origin, priority, NativeHookUtils.CreateXHookerProxy(nativePtr)); }
}