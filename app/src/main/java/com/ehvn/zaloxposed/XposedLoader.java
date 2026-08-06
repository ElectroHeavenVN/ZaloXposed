package com.ehvn.zaloxposed;

import android.util.Log;

import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Utils;

import org.luckypray.dexkit.DexKitBridge;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedLoader implements IXposedHookLoadPackage, IXposedHookZygoteInit
{
    static
    {
        System.loadLibrary("dexkit");
    }

    private DexKitBridge bridge;
    private String modulePath = null;

    private static final String HOOKS_PACKAGE = "com.ehvn.zaloxposed.hooks.";

    private List<Class<? extends BaseHook>> discoverHookClasses(String apkPath)
    {
        List<Class<? extends BaseHook>> hooks = new ArrayList<>();
        if (apkPath == null)
        {
            XposedBridge.log("[ZaloXposed] modulePath is null, cannot discover hooks");
            return hooks;
        }
        try
        {
            ClassLoader moduleClassLoader = XposedLoader.class.getClassLoader();
            DexFile dexFile = new DexFile(apkPath);
            Enumeration<String> entries = dexFile.entries();
            while (entries.hasMoreElements())
            {
                String className = entries.nextElement();
                if (!className.startsWith(HOOKS_PACKAGE))
                    continue;
                if (className.equals(HOOKS_PACKAGE + "BaseHook"))
                    continue;
                if (className.contains("$"))
                    continue;
                try
                {
                    Class<?> clazz = Class.forName(className, false, moduleClassLoader);
                    if (BaseHook.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers()) && Modifier.isPublic(clazz.getModifiers()))
                    {
                        hooks.add((Class<? extends BaseHook>) clazz);
                        XposedBridge.log("[ZaloXposed] Found hook: " + className);
                    }
                }
                catch (Throwable e)
                {
                    XposedBridge.log("[ZaloXposed] Failed to load: " + className + " - " + e.getMessage());
                }
            }
            dexFile.close();
            XposedBridge.log("[ZaloXposed] Discovered " + hooks.size() + " hook(s)");
        }
        catch (IOException e)
        {
            XposedBridge.log("[ZaloXposed] Failed to scan dex: " + e.getMessage());
        }
        return hooks;
    }

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable
    {
        if (!lpparam.packageName.startsWith("com.zing.zalo"))
            return;
        XposedBridge.log("[ZaloXposed] Loading ZaloXposed");
        if (bridge == null)
        {
            try
            {
                bridge = DexKitBridge.create(lpparam.appInfo.sourceDir);
            }
            catch (Exception e)
            {
                XposedBridge.log(e);
            }
        }
        Utils.Init(lpparam.appInfo, lpparam.classLoader, bridge, modulePath);
        if (bridge == null)
            return;
        for (Class<? extends BaseHook> clazz : discoverHookClasses(modulePath))
        {
            try
            {
                BaseHook instance = clazz.getDeclaredConstructor().newInstance();
                instance.init(bridge, lpparam);
                instance.hook();
            }
            catch (Exception e)
            {
                XposedBridge.log("[ZaloXposed] Error in " + clazz.getSimpleName());
                XposedBridge.log(e);
                Log.e("ZaloXposed", "Error in " + clazz.getSimpleName(), e);
            }
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable
    {
        modulePath = startupParam.modulePath;
    }
}