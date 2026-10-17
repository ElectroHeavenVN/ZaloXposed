package com.ehvn.zaloxposed;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

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
    private View customSplash = null;

    static
    {
        System.loadLibrary("dexkit");

        hooks.add(new ResourceHook());
        hooks.add(new ZaloXposedSettingsMenuHook());
        hooks.add(new CustomBackgroundHook());

        hooks.add(new WatermarkHook());
        hooks.add(new EnableE2EEHook());
        hooks.add(new EnableLabelHook());
        hooks.add(new RestoreGoogleDriveBackupHook());
        hooks.add(new RestoreProfileMusicHook());
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
            hooks.add(new FixProviderHook());
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint({"DiscouragedApi", "SetTextI18n"})
    private void hookDisplayCustomSplash(ClassLoader classLoader, AssetManager assetManager) throws ClassNotFoundException, NoSuchMethodException
    {
        Class<?> clazz = Class.forName("com.zing.zalo.ui.ZaloLauncherActivity", false, classLoader);
        Method onCreate = clazz.getDeclaredMethod("onCreate", Bundle.class);
        hook(onCreate).setPriority(5).intercept(chain ->
        {
            Object result = chain.proceed();
            Activity activity = (Activity)chain.getThisObject();
            try
            {
                if (assetManager == null)
                    return result;
                var zaloRes = activity.getResources();
                Resources modRes = new Resources(assetManager, zaloRes.getDisplayMetrics(), zaloRes.getConfiguration());
                String modulePkg = "com.ehvn.zaloxposed";
                int layoutId = modRes.getIdentifier("custom_splash", "layout", modulePkg);
                if (layoutId == 0)
                    return result;
                Context modContext = new ContextThemeWrapper(activity, 0)
                {
                    @Override
                    public Resources getResources()
                    {
                        return modRes;
                    }

                    @Override
                    public AssetManager getAssets()
                    {
                        return modRes.getAssets();
                    }
                };
                LayoutInflater inflater = activity.getLayoutInflater().cloneInContext(modContext);
                customSplash = inflater.inflate(layoutId, null, false);
                if (customSplash == null)
                    return result;
                var textViewId = modRes.getIdentifier("tv_custom", "id", modulePkg);
                if (textViewId != 0)
                {
                    var textView = customSplash.findViewById(textViewId);
                    if (textView instanceof TextView)
                        ((TextView)textView).setText(MorpheConstants.getModuleName() + " by ElectroHeavenVN");
                }
                View decor = activity.getWindow() != null ? activity.getWindow().getDecorView() : null;
                if (!(decor instanceof ViewGroup))
                    return result;
                var lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                ((ViewGroup)decor).addView(customSplash, lp);
            }
            catch (Throwable t)
            {
                Logger.e(t);
            }
            new Handler(Looper.getMainLooper()).postDelayed(()->
            {
                try
                {
                    if (customSplash != null && customSplash.getParent() != null)
                        ((ViewGroup)customSplash.getParent()).removeView(customSplash);
                }
                catch (Throwable ignored) { }
            }, 2100);
            return result;
        });
    }

    @Override
    public void onModuleLoaded(ModuleLoadedParam param)
    {
        log(Log.INFO, MorpheConstants.getModuleName(), "Loaded");
        Logger.Init(this);
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    @Override
    public void onPackageReady(PackageReadyParam param)
    {
        if (!param.getPackageName().startsWith("com.zing.zalo"))
            return;
        long startTime = System.currentTimeMillis();
        Logger.i("Loading ZaloXposed");
        try
        {
            System.loadLibrary("ZaloXposedNative");
        }
        catch (Exception e)
        {
            Logger.e(e);
            return;
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
        Common.setPackageName(param.getPackageName());
        Config.Load();
        try
        {
            AssetManager assetManager = AssetManager.class.getDeclaredConstructor().newInstance();
            Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, getModuleApplicationInfo().sourceDir);
            hookDisplayCustomSplash(param.getClassLoader(), assetManager);
            for (BaseHook hook : hooks)
            {
                try
                {
                    long startHookTime = System.currentTimeMillis();
                    hook.init(this, bridge, param, assetManager);
                    hook.hook();
                    Logger.d(hook.getClass().getSimpleName() + " finished in " + (System.currentTimeMillis() - startHookTime) + "ms");
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
        new Handler(Looper.getMainLooper()).postDelayed(()->
        {
            try
            {
                if (customSplash != null && customSplash.getParent() != null)
                    ((ViewGroup)customSplash.getParent()).removeView(customSplash);
            }
            catch (Throwable ignored) { }
        }, 2000);
        Logger.d("ZaloXposed loaded in " + (System.currentTimeMillis() - startTime) + "ms");
    }
}