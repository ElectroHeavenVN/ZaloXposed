package com.ehvn.zaloxposed.hooks.morphe;

import com.ehvn.zaloxposed.hooks.nativehook.BaseNativeHook;

public class SpoofPackageNameHook extends BaseNativeHook
{
    @Override
    public native void nativeHook() throws Throwable;
}
