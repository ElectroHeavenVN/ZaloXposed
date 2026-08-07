package com.ehvn.zaloxposed.hooks;

import org.luckypray.dexkit.DexKitBridge;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

public abstract class BaseHook
{
    protected XposedModule module;
    protected DexKitBridge bridge;
    protected ClassLoader classLoader;

    public void init(XposedModule module, DexKitBridge bridge, XposedModuleInterface.PackageReadyParam param)
    {
        this.module = module;
        this.bridge = bridge;
        this.classLoader = param.getClassLoader();
    }

    public abstract void hook() throws Throwable;
}