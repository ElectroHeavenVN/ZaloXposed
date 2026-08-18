package com.ehvn.zaloxposed.hooks;

import android.content.res.AssetManager;

import org.luckypray.dexkit.DexKitBridge;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

public abstract class BaseHook
{
    protected XposedModule module;
    protected DexKitBridge bridge;
    protected ClassLoader classLoader;
    protected AssetManager assetManager;

    public void init(XposedModule module, DexKitBridge bridge, XposedModuleInterface.PackageReadyParam param, AssetManager assetManager)
    {
        this.module = module;
        this.bridge = bridge;
        this.classLoader = param.getClassLoader();
        this.assetManager = assetManager;
    }

    public abstract void hook() throws Throwable;
}