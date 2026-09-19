package com.ehvn.zaloxposed;

import android.content.res.AssetManager;
import android.util.Log;

import com.ehvn.zaloxposed.hooks.*;
import com.ehvn.zaloxposed.hooks.functional.*;
import com.ehvn.zaloxposed.hooks.morphe.*;
import com.ehvn.zaloxposed.hooks.privacy.*;
import com.ehvn.zaloxposed.hooks.permanent.*;
import com.ehvn.zaloxposed.hooks.tracking.*;
import com.ehvn.zaloxposed.hooks.ui.*;
import com.ehvn.zaloxposed.hooks.ads.*;
import com.ehvn.zaloxposed.hooks.custommenu.ZaloXposedSettingsMenuHook;
import com.ehvn.zaloxposed.utilities.Config;
import com.ehvn.zaloxposed.utilities.Logger;
import com.ehvn.zaloxposed.utilities.Utils;

import org.luckypray.dexkit.DexKitBridge;

import java.lang.reflect.Method;
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
        hooks.add(new CustomBackgroundHook());

        hooks.add(new ChatInputBarTitleHook());
        hooks.add(new EnableE2EEHook());
        hooks.add(new EnableLabelHook());
        hooks.add(new RestoreDevToolsMenuHook());
        hooks.add(new EnableSetNicknameInGroupHook());
        hooks.add(new DisableDohHook());
        hooks.add(new AntiRecallDeleteChatRowHook());
        hooks.add(new DisableFirebaseLoggingHook());
        hooks.add(new DisableZaloTrackingHook());
        hooks.add(new HideAdsHook());
        hooks.add(new CustomizeBottomRowHook());
        hooks.add(new EnableChatHeadHook());
        hooks.add(new ExtendedGridMenuHook());
        hooks.add(new ExtendedGroupSettingMenuHook());
        hooks.add(new FakeAdminHook());
        hooks.add(new FakeOwnerHook());
        hooks.add(new TTLHook());
        hooks.add(new UnlockZCloudHook());
        hooks.add(new EnableShareHiddenStickerPackHook());
        hooks.add(new AntiRecallDeleteHook());
        hooks.add(new SilentTypingHook());
        hooks.add(new BlockSendSeenHook());
        hooks.add(new ExtendedGroupRightMenuHook());
        hooks.add(new EnableChatProtectionHook());

        hooks.add(new TestHook());

        if (MorpheConstants.isPatchedByMorphe())
        {
            hooks.add(new SpoofAppSignatureHook());
            hooks.add(new SpoofPackageNameHook());
        }
    }

    @Override
    public void onModuleLoaded(ModuleLoadedParam param)
    {
        log(Log.INFO, "ZaloXposed", "Loaded");
        Logger.Init(this);
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    @Override
    public void onPackageReady(PackageReadyParam param)
    {
        if (!param.getPackageName().startsWith("com.zing.zalo"))
            return;
        Logger.i("Loading ZaloXposed");
        try
        {
            System.loadLibrary("ZaloXposedNative");
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
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
        try
        {
            AssetManager assetManager = AssetManager.class.getDeclaredConstructor().newInstance();
            Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, getModuleApplicationInfo().sourceDir);
            for (BaseHook hook : hooks)
            {
                try
                {
                    hook.init(this, bridge, param, assetManager);
                    hook.hook();
                }
                catch (Throwable e)
                {
                    Logger.e(e);
                }
            }
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
    }
}