package com.ehvn.zaloxposed;

import android.util.Log;

import androidx.annotation.NonNull;

import com.ehvn.zaloxposed.hooks.*;
import com.ehvn.zaloxposed.hooks.custommenu.ZaloXposedSettingsMenuHook;
import com.ehvn.zaloxposed.hooks.permanent.*;
import com.ehvn.zaloxposed.utilities.Config;
import com.ehvn.zaloxposed.utilities.Logger;
import com.ehvn.zaloxposed.utilities.Utils;

import org.luckypray.dexkit.DexKitBridge;

import java.util.ArrayList;

import io.github.libxposed.api.XposedModule;

public class ZaloXposedLoader extends XposedModule
{
    private static DexKitBridge bridge;
    private static final ArrayList<BaseHook> hooks = new ArrayList<>();

    static
    {
        System.loadLibrary("dexkit");
        hooks.add(new ZaloXposedSettingsMenuHook());
        hooks.add(new ChatInputBarTitleHook());
        hooks.add(new EnableE2EEHook());
        hooks.add(new RestoreDevToolsMenuHook());
        hooks.add(new EnableChatHeadHook());
        hooks.add(new ExtendedGridMenuHook());
        hooks.add(new FakeAdminHook());
        hooks.add(new FakeOwnerHook());
        hooks.add(new TTLHook());
        hooks.add(new UnlockZCloudHook());

        hooks.add(new TestHook());
    }

    @Override
    public void onModuleLoaded(@NonNull ModuleLoadedParam param)
    {
        log(Log.INFO, "ZaloXposed", "Loaded");
        Logger.Init(this);
    }

    @Override
    public void onPackageReady(@NonNull PackageReadyParam param)
    {
        if (!param.getPackageName().startsWith("com.zing.zalo"))
            return;
        Logger.i("Loading ZaloXposed");
        try
        {
            if (bridge == null)
                bridge = DexKitBridge.create(param.getApplicationInfo().sourceDir);
            Utils.Init(param.getApplicationInfo(), param.getClassLoader(), bridge);
        }
        catch (Exception e)
        {
            Logger.e(e);
            return;
        }
        Config.Load();
        for (BaseHook hook : hooks)
        {
            try
            {
                hook.init(this, bridge, param);
                hook.hook();
            }
            catch (Throwable e)
            {
                Logger.e("Error in " + hook.getClass().getSimpleName());
                Logger.e(e);
            }
        }
    }
}