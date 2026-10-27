package io.github.electroheavenvn.zaloxposed.hooks.morphe;

import io.github.electroheavenvn.zaloxposed.hooks.nativehook.BaseNativeHook;

public class SpoofAppSignatureHook extends BaseNativeHook
{
    @Override
    public native void nativeHook() throws Throwable;
}