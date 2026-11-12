package io.github.electroheavenvn.zaloxposed.hooks;

import android.content.res.AssetManager;
import android.util.Log;

import org.luckypray.dexkit.DexKitBridge;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.electroheavenvn.zaloxposed.hooks.nativehook.BaseNativeHook;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;
import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

public abstract class BaseHook
{
    private static final HashMap<Executable, ArrayList<Object>> hooks = new HashMap<>();
    private XposedModule module;
    protected DexKitBridge bridge;
    protected ClassLoader classLoader;
    protected AssetManager assetManager;

    public void Init(XposedModule module, DexKitBridge bridge, XposedModuleInterface.PackageReadyParam param, AssetManager assetManager)
    {
        this.module = module;
        this.bridge = bridge;
        this.classLoader = param.getClassLoader();
        this.assetManager = assetManager;
    }

    public abstract void installHooks() throws Throwable;

    protected XposedInterface.HookHandle doHook(Executable origin, XposedInterface.Hooker hooker)
    {
        logHook(origin, XposedInterface.PRIORITY_DEFAULT, hooker);
        XposedInterface.HookHandle handle = module.hook(origin).intercept(hooker);
        if (!hooks.containsKey(origin) || hooks.get(origin) == null)
            hooks.put(origin, new ArrayList<>());
        ArrayList<Object> handles = hooks.get(origin);
        assert handles != null;
        handles.add(handle);
        return handle;
    }

    protected XposedInterface.HookHandle doHook(Executable origin, int priority, XposedInterface.Hooker hooker)
    {
        logHook(origin, priority, hooker);
        return module.hook(origin).setPriority(priority).intercept(hooker);
    }

    protected XposedInterface.HookHandle doHookClInit(Class<?> origin, XposedInterface.Hooker hooker)
    {
        logHook(origin, XposedInterface.PRIORITY_DEFAULT, hooker);
        XposedInterface.HookHandle handle = module.hookClassInitializer(origin).intercept(hooker);
        Executable exe = handle.getExecutable();
        if (!hooks.containsKey(exe) || hooks.get(exe) == null)
            hooks.put(exe, new ArrayList<>());
        ArrayList<Object> handles = hooks.get(exe);
        assert handles != null;
        handles.add(handle);
        return handle;
    }

    protected XposedInterface.HookHandle doHookClInit(Class<?> origin, int priority, XposedInterface.Hooker hooker)
    {
        logHook(origin, priority, hooker);
        return module.hookClassInitializer(origin).setPriority(priority).intercept(hooker);
    }

    protected boolean unhook(XposedInterface.HookHandle handle)
    {
        logUnhook(handle);
        handle.unhook();
        ArrayList<Object> handles = hooks.get(handle.getExecutable());
        if (handles == null)
            return false;
        return handles.remove(handle);
    }

    protected boolean unhook(Executable origin)
    {
        ArrayList<Object> handles = hooks.get(origin);
        if (handles == null)
            return false;
        boolean failed = false;
        for (Object o : handles)
        {
            XposedInterface.HookHandle handle = (XposedInterface.HookHandle)o;
            failed |= unhook(handle);
        }
        return !failed;
    }

    private void logHook(Object origin, int priority, XposedInterface.Hooker hooker)
    {
        if (this instanceof BaseNativeHook)
            return;
        final String LIME = "\u001B[38;2;166;226;46m";
        final String PURPLE = "\u001B[38;2;174;129;255m";
        final String CYAN = "\u001B[38;2;0;255;255m";
        final String RESET = "\u001B[0m";
        final String GRAY = "\u001B[90m";
        String target = "";
        if (origin instanceof Executable exe)
            target = exe.toString();
        else if (origin instanceof Class<?> clazz)
            target = clazz + ".<clinit>()";
        target = target.replace("java.lang.String", "String");
        String callerInfo = getCallerInfo();
        String str = "Hooking: ";
        if (priority != XposedInterface.PRIORITY_DEFAULT)
            str = "Hooking [priority " + priority + "]: ";
        String hookerName = hooker.getClass().toString().replace("io.github.electroheavenvn.zaloxposed.hooks", "$zaloxposed");
        if (hookerName.startsWith("class "))
            hookerName = hookerName.substring("class ".length());
        Log.i(Logger.TAG, CYAN + callerInfo + LIME + str + PURPLE + target + GRAY + " (" + hookerName + ")" + RESET);
        if (module != null)
            module.log(Log.INFO, Logger.TAG, callerInfo + str + target + " (" + hookerName + ")");
    }

    private void logUnhook(XposedInterface.HookHandle handle)
    {
        if (this instanceof BaseNativeHook)
            return;
        final String LIME = "\u001B[38;2;166;226;46m";
        final String PURPLE = "\u001B[38;2;174;129;255m";
        final String CYAN = "\u001B[38;2;0;255;255m";
        final String RESET = "\u001B[0m";
        final String GRAY = "\u001B[90m";

        String callerInfo = getCallerInfo();
        String target = handle.getExecutable().toString().replace("java.lang.String", "String");
        Log.i(Logger.TAG, CYAN + callerInfo + LIME + "Unhooking: " + PURPLE + target + RESET);
        if (module != null)
            module.log(Log.INFO, Logger.TAG, callerInfo + "Unhooking: " + target);
    }

    private static String getCallerInfo()
    {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 3; i < stackTrace.length; i++)
        {
            StackTraceElement element = stackTrace[i];
            String className = element.getClassName();
            if (className.equals(BaseHook.class.getName()) || className.equals(BaseNativeHook.class.getName()))
                continue;
            String simpleName = className.substring(className.lastIndexOf('.') + 1);
            return "[" + simpleName + "] ";
        }
        return "";
    }
}