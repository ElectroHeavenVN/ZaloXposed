package io.github.electroheavenvn.zaloxposed.hooks;

import android.content.res.AssetManager;

import org.luckypray.dexkit.DexKitBridge;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.HashMap;

import io.github.libxposed.api.XposedModuleInterface;

public abstract class BaseHook
{
    private static final HashMap<Executable, ArrayList<Object>> hooks = new HashMap<>();

    protected DexKitBridge bridge;
    protected ClassLoader classLoader;
    protected AssetManager assetManager;

    public void Init(DexKitBridge bridge, XposedModuleInterface.PackageReadyParam param, AssetManager assetManager)
    {
        this.bridge = bridge;
        this.classLoader = param.getClassLoader();
        this.assetManager = assetManager;
    }

    public abstract void installHooks() throws Throwable;
}