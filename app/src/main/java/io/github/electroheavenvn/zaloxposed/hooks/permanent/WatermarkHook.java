package io.github.electroheavenvn.zaloxposed.hooks.permanent;

import io.github.electroheavenvn.zaloxposed.hooks.nativehook.BaseNativeHook;

public class WatermarkHook extends BaseNativeHook
{
    @Override
    public native void nativeHook() throws Throwable;
}