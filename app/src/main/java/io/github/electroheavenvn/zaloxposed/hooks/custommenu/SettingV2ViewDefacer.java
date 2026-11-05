package io.github.electroheavenvn.zaloxposed.hooks.custommenu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.FieldMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import io.github.electroheavenvn.zaloxposed.MorpheConstants;
import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.settingsmenu.ISettingItem;
import io.github.electroheavenvn.zaloxposed.settingsmenu.SettingCategory;
import io.github.electroheavenvn.zaloxposed.settingsmenu.SettingFeature;
import io.github.electroheavenvn.zaloxposed.settingsmenu.SettingSubCategory;
import io.github.electroheavenvn.zaloxposed.settingsmenu.SettingsMenu;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;
import io.github.electroheavenvn.zaloxposed.utilities.S;
import io.github.electroheavenvn.zaloxposed.utilities.Utils;

public class SettingV2ViewDefacer extends BaseHook
{
    private static Class<?> settingPrivateV2View;
    private static Class<?> zaloViewManager;
    private static Class<?> zaloView;
    private static Method getViewManager;
    private static Method openView;
    // this field is rarely used in the original app, so we can take advantage of it to store our custom data
    // private static Field unmapDialogContent;

    @SuppressLint("StaticFieldLeak")
    private static LinearLayout rootLayout = null;

    private static void createCustomMenu(Object zaloView) throws Exception
    {
        Context context = rootLayout.getContext();
        List<ISettingItem> items = SettingsMenu.getSettingItems();
        for (int i = 0; i < items.size(); i++)
        {
            ISettingItem item = items.get(i);
            if (item instanceof SettingSubCategory)
                continue;
            if (item instanceof SettingFeature feature)
            {
                feature.onLoad(context);
                boolean hideDivider = true;
                if (i + 1 < items.size())
                {
                    if (items.get(i + 1) instanceof SettingFeature)
                        hideDivider = false;
                }
                SettingPrivateV2ViewDefacer.ProcessSettingFeature(rootLayout, feature, hideDivider);
                continue;
            }
            if (item instanceof SettingCategory && i > 0 && items.get(i - 1) instanceof SettingFeature)
                rootLayout.addView(createSeparator(context));
            RelativeLayout listItemSetting = ListItemSettingHelper.CreateNew(context);
            rootLayout.addView(listItemSetting);
            if (i + 1 < items.size())
                ListItemSettingHelper.ShowDivider(listItemSetting);
            else
                ListItemSettingHelper.HideDivider(listItemSetting);
            ListItemSettingHelper.SetTitle(listItemSetting, item.getTitle());
            ListItemSettingHelper.SetSubtitle(listItemSetting, item.getDescription());
            final int index = i;
            ListItemSettingHelper.SetOnClickListener(listItemSetting, v -> openSubCategory(zaloView, index));
            if (item instanceof SettingCategory)
                ListItemSettingHelper.SetShowChevronRight(listItemSetting, true);
        }

        rootLayout.addView(createSeparator(context));

        int pad16 = dp(context, 16);

        Object reloadCacheButton = ZButtonHelper.CreateNew(context);
        rootLayout.addView((View)reloadCacheButton);
        ZButtonHelper.SetText(reloadCacheButton, S.tr("reloadDexKitCacheBtnTitle"));
        ZButtonHelper.SetOnClickListener(reloadCacheButton, v ->
        {
            DexKitCache.ClearCache();
            restartApp(context);
        });
        LinearLayout.LayoutParams reloadCacheLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        reloadCacheLp.setMargins(pad16, dp(context, 8), pad16, 0);
        ((View)reloadCacheButton).setLayoutParams(reloadCacheLp);

        Object restartButton = ZButtonHelper.CreateNew(context);
        rootLayout.addView((View)restartButton);
        ZButtonHelper.SetText(restartButton, S.tr("restartAppBtnTitle"));
        ZButtonHelper.SetOnClickListener(restartButton, v -> restartApp(context));
        LinearLayout.LayoutParams restartLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        restartLp.setMargins(pad16, pad16, pad16, 0);
        ((View)restartButton).setLayoutParams(restartLp);

        for (int i = 0; i < 5; i++)
            rootLayout.addView(createSeparator(context));
    }

    @Override
    public void hook() throws Throwable
    {
        settingPrivateV2View = Class.forName("com.zing.zalo.ui.settings.SettingPrivateV2View", false, classLoader);
        MethodMatcher openViewMatcher = MethodMatcher.create()
            .declaredClass("com.zing.zalo.zview.", StringMatchType.StartsWith)
            .modifiers(Modifier.PUBLIC | Modifier.FINAL)
            .returnType("void")
            .paramCount(4)
            .paramTypes(Class.class, Bundle.class, int.class, boolean.class)
            .addUsingNumber(0);
        zaloViewManager = DexKitCache.GetClass("ZaloXposedSettings_ZaloViewManager", () -> bridge.findClass(FindClass.create()
            .matcher(ClassMatcher.create()
                .modifiers(Modifier.PUBLIC)
                .className("com.zing.zalo.zview", StringMatchType.StartsWith)
                .addMethod(openViewMatcher)
                .addUsingString("ZaloViewManager", StringMatchType.Equals)
                .addUsingString("Cannot show ZaloView, return. mParentZaloView.mInnerView is null", StringMatchType.Equals)
                .addUsingString("did not call through to super.", StringMatchType.Contains)
                .addUsingString("Cannot create ZaloView", StringMatchType.Contains)
            )), classLoader);
        if (zaloViewManager == null)
            return;
        openViewMatcher.declaredClass(zaloViewManager);
        openView = DexKitCache.GetMethod("ZaloXposedSettings_ZaloViewManager_OpenView", () -> bridge.findMethod(FindMethod.create().matcher(openViewMatcher)), classLoader);
        if (openView == null)
            return;
        zaloView = DexKitCache.GetClass("ZaloXposedSettings_ZaloView", () -> bridge.findClass(FindClass.create()
            .matcher(ClassMatcher.create()
                .modifiers(Modifier.PUBLIC)
                .addUsingString("ZaloView ", StringMatchType.Equals)
                .addUsingString("not attached to", StringMatchType.Contains)
                .addUsingString("Can't access ViewModels from detached fragment: ", StringMatchType.Equals)
                .addUsingString("Called getViewLifecycleOwner() but onCreateView() returned null", StringMatchType.Equals)
                .addUsingString("Can't access the ZaloView View's LifecycleOwner when getView() is null i.e., before onCreateView() or after onDestroyView()", StringMatchType.Equals)
            )), classLoader);
        if (zaloView == null)
            return;
        getViewManager = DexKitCache.GetMethod("ZaloXposedSettings_ZaloView_GetViewManager", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(0)
                .returnType(zaloViewManager)
                .declaredClass(zaloView)
                .addUsingString("ZaloView ", StringMatchType.Equals)
                .addUsingString(" not associated with a zalo view manager.", StringMatchType.Equals)
            )), classLoader);
        if (getViewManager == null)
            return;
        //unmapDialogContent = DexKitCache.GetField("ZaloXposedSettings_SettingV2View_UnmapDialogContent", () -> bridge.findField(FindField.create()
        //    .matcher(FieldMatcher.create()
        //        .declaredClass("com.zing.zalo.ui.settings.SettingV2View")
        //        .type(String.class)
        //        .modifiers(Modifier.PUBLIC)
        //    )), classLoader);
        //if (unmapDialogContent == null)
        //    return;
        Method inflate = DexKitCache.GetMethod("ZaloXposedSettings_SettingV2View_Inflate", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.settings.SettingV2View")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("android.view.View")
                .paramCount(3)
                .paramTypes("android.view.LayoutInflater", "android.widget.LinearLayout", "android.os.Bundle")
            )), classLoader);
        if (inflate == null)
            return;
        Logger.h(inflate);
        module.hook(inflate).setPriority(10).intercept(chain ->
        {
            Object result = chain.proceed();
            // Bundle bundle = (Bundle)chain.getArg(2);
            // if (bundle != null && bundle.getBoolean("deface"))
            if (ZaloXposedSettingsMenu.isOpening)
            {
                rootLayout = (LinearLayout)chain.getArg(1);
                //unmapDialogContent.set(chain.getThisObject(), "true|" + unmapDialogContent.get(chain.getThisObject()));
                ZaloXposedSettingsMenu.isOpening = false;
                ZaloXposedSettingsMenu.isOpenSettingMenu = true;
            }
            else
            {
                rootLayout = null;
                ZaloXposedSettingsMenu.isOpenSettingMenu = false;
                //String str = (String)unmapDialogContent.get(chain.getThisObject());
                //if (str != null && str.startsWith("true|"))
                //    unmapDialogContent.set(chain.getThisObject(), str.substring("true|".length()));
            }
            return result;
        });
        // 261101901:
        // @Override // com.zing.zalo.ui.settings.BaseSettingView
        // public final void T6() { ... }
        Method initUI = DexKitCache.GetMethod("ZaloXposedSettings_SettingV2View_InitUI", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.settings.SettingV2View")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("void")
                .paramCount(0)
                .addUsingString("WARNING_FULL_STORAGE_ENABLED_${UserID}", StringMatchType.Equals)
                .addUsingNumber(8.0f)
                .addUsingNumber(28)
            )), classLoader);
        if (initUI == null)
            return;
        Logger.h(initUI);
        module.hook(initUI).setPriority(10).intercept(chain ->
        {
            Object result = chain.proceed();
            if (!isOpenZaloXposedSettings(chain.getThisObject()))
                return result;
            if (rootLayout == null)
                return result;
            try
            {
                // Hide all existing children
                int count = rootLayout.getChildCount();
                for (int i = 0; i < count; i++)
                {
                    View child = rootLayout.getChildAt(i);
                    child.setVisibility(View.GONE);
                }
                createCustomMenu(chain.getThisObject());
            }
            catch (Exception e) { Logger.e(e); }
            return result;
        });
        Method setActionBarTitle = DexKitCache.GetMethod("ZaloXposedSettings_SettingV2View_SetActionBarTitle", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.settings.SettingV2View")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("void")
                .paramCount(0)
                .addUsingField(FieldMatcher.create().name("setting_title"))
                .addInvoke(MethodMatcher.create()
                    .declaredClass("com.zing.zalo.zdesign.component.header.ZdsActionBar")
                    .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                    .returnType("void")
                    .name("setMiddleTitle"))
            )), classLoader);
        if (setActionBarTitle == null)
            return;
        Logger.h(setActionBarTitle);
        module.hook(setActionBarTitle).setPriority(10).intercept(chain ->
        {
            Field actionBarField = DexKitCache.GetFieldRaw("ZaloXposedSettings_SettingV2View_ActionBar", classLoader);
            if (actionBarField == null)
            {
                actionBarField = Utils.FindFieldByType(Class.forName("com.zing.zalo.ui.zviews.ZaloViewNewActionBar", false, classLoader), "com.zing.zalo.zdesign.component.header.ZdsActionBar", false);
                if (actionBarField != null)
                    DexKitCache.SetFieldRaw("ZaloXposedSettings_SettingV2View_ActionBar", actionBarField);
            }
            Object result = chain.proceed();
            if (!isOpenZaloXposedSettings(chain.getThisObject()))
                return result;
            if (actionBarField == null)
                return result;
            try
            {
                Object actionBar = actionBarField.get(chain.getThisObject());
                if (actionBar == null)
                    return result;
                Method setMiddleTitle = actionBar.getClass().getMethod("setMiddleTitle", String.class);
                String moduleName = MorpheConstants.getModuleName();
                setMiddleTitle.invoke(actionBar, S.tr("moduleSettingsHeaderTitle", moduleName));
            }
            catch (Exception t) { Logger.e(t); }
            return result;
        });
    }

    private static boolean isOpenZaloXposedSettings(Object zaloViewInst) throws IllegalAccessException
    {
        if (zaloViewInst == null || !zaloView.isInstance(zaloViewInst))
            return false;
        return ZaloXposedSettingsMenu.isOpenSettingMenu;
        // boolean result = false;
        // String str = (String)unmapDialogContent.get(zaloViewInst);
        // if (str != null && str.startsWith("true|"))
        //    result = true;
        // return result;
    }

    private static View createSeparator(Context context)
    {
        View separator = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (int)(10 * context.getResources().getDisplayMetrics().density)
        );
        separator.setLayoutParams(params);
        separator.setBackgroundColor(Color.TRANSPARENT);
        return separator;
    }

    private static void openSubCategory(Object zaloViewInst, int categoryIndex)
    {
        if (zaloViewInst == null || !zaloView.isInstance(zaloViewInst))
            return;
        ZaloXposedSettingsMenu.categoryIndex = categoryIndex;
        try
        {
            Object viewManager = getViewManager.invoke(zaloViewInst);
            // Bundle bundle = new Bundle();
            // bundle.putBoolean("deface", true);
            // bundle.putInt("category_index", categoryIndex);
            openView.invoke(viewManager, settingPrivateV2View, null, 1, true);
        }
        catch (Throwable t) { Logger.e(t); }
    }

    private static void restartApp(Context context)
    {
        try
        {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            if (intent == null)
                return;
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            if (Activity.class.isAssignableFrom(context.getClass()))
                ((Activity)context).finish();
            new Handler(Looper.getMainLooper()).postDelayed(() -> Runtime.getRuntime().exit(0), 0);
        }
        catch (Exception e) { Logger.e(e); }
    }

    private static int dp(Context context, int value)
    {
        return (int)(value * context.getResources().getDisplayMetrics().density);
    }
}
