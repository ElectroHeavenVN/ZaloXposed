package com.ehvn.zaloxposed.hooks;

import android.util.Log;

import org.luckypray.dexkit.DexKitBridge;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public abstract class BaseHook
{
    protected DexKitBridge bridge;
    protected LoadPackageParam lpparam;

    public void init(DexKitBridge bridge, LoadPackageParam lpparam)
    {
        this.bridge = bridge;
        this.lpparam = lpparam;
    }

    public abstract void hook() throws Throwable;

    public void log(String message)
    {
        if (message != null)
        {
            XposedBridge.log("[ZaloXposed] [" + getClass().getSimpleName() + "]: " + message);
            Log.i("ZaloXposed", "[" + getClass().getSimpleName() + "]: " + message);
        }
    }
}