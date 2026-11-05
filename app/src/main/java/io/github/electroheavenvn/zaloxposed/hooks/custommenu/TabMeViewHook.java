package io.github.electroheavenvn.zaloxposed.hooks.custommenu;

import android.os.Bundle;

import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindField;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.FieldMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import io.github.electroheavenvn.zaloxposed.MorpheConstants;
import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.hooks.ResourceHook;
import io.github.electroheavenvn.zaloxposed.settingsmenu.SettingItemID;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;
import io.github.electroheavenvn.zaloxposed.utilities.S;
import io.github.electroheavenvn.zaloxposed.utilities.Utils;

public class TabMeViewHook extends BaseHook
{
    private static final String CUSTOM_ITEM_MARKER = "zalo_xposed_settings";
    private static final int CUSTOM_DRAWABLE_ID = -6767;

    private static Method openView;
    private static Field viewManager;

    @Override
    @SuppressWarnings("unchecked")
    public void hook() throws Throwable
    {
        ResourceHook.map(CUSTOM_DRAWABLE_ID, "extension_24px", "zds_oic_premium_crown_color_24");
        Class<?> tabMeViewClass = Class.forName("com.zing.zalo.ui.maintab.me.TabMeView", false, classLoader);
        Class<?> zaloViewActionBar = DexKitCache.GetClass("ZaloXposedSettings_ZaloViewActionBar", () -> bridge.findClass(FindClass.create()
            .matcher(ClassMatcher.create()
                .modifiers(Modifier.PUBLIC)
                .addInterface("android.content.ComponentCallbacks")
                .addInterface("android.view.View$OnCreateContextMenuListener")
                .addUsingString("Can't access ViewModels from detached fragment: ", StringMatchType.Equals)
                .addUsingString("Called getViewLifecycleOwner() but onCreateView() returned null", StringMatchType.Equals)
                .addUsingString("Can't access the ZaloView View's LifecycleOwner when getView() is null i.e., before onCreateView() or after onDestroyView()", StringMatchType.Equals)
                .addUsingString("Reuse ZaloView instance - Restarter must be created only during owner's initialization stage", StringMatchType.Equals)
                .addUsingString("not attached to an activity", StringMatchType.Contains)
            )), classLoader);
        if (zaloViewActionBar == null)
            return;
        viewManager = DexKitCache.GetField("ZaloXposedSettings_ZaloViewActionBar_ViewManager", () -> bridge.findField(FindField.create()
            .matcher(FieldMatcher.create()
                .modifiers(Modifier.PUBLIC)
                .declaredClass(zaloViewActionBar)
                .type(ClassMatcher.create()
                    .modifiers(Modifier.PUBLIC | Modifier.INTERFACE)
                    .addMethod(MethodMatcher.create().name("getAssets"))
                    .addMethod(MethodMatcher.create().name("getCurrentFocus"))
                    .addMethod(MethodMatcher.create().name("getIntent"))
                    .addMethod(MethodMatcher.create().name("getLayoutInflater"))
                    .addMethod(MethodMatcher.create().name("getRequestedOrientation"))
                    .addMethod(MethodMatcher.create().name("runOnUiThread"))
                    .addMethod(MethodMatcher.create().name("startActivityForResult"))
                    .addMethod(MethodMatcher.create().name("isDestroyed"))
                    .addInterface("com.zing.zalo.activity.RequestPermissionInterface"))
            )), classLoader);
        if (viewManager == null)
            return;
        openView = DexKitCache.GetMethod("ZaloXposedSettings_TabMeView_OpenView", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(viewManager.getType())
                .returnType("void")
                .paramCount(3)
                .paramTypes(Class.class, Bundle.class, int.class)
            )), classLoader);
        if (openView == null)
            return;
        List<Method> methods = DexKitCache.GetMethods("ZaloXposedSettings_TabMeView_EnableFullZInstant", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.maintab.me.TabMeView")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("boolean")
                .addUsingString("features@tabme@fullzins@enable", StringMatchType.Equals)
                .addUsingNumber(0)
                .addUsingNumber(1)
            )), classLoader);
        if (methods.isEmpty())
        {
            Logger.e("Target method not found 1");
            return;
        }
        for (Method method : methods)
        {
            Logger.h(method);
            module.hook(method).setPriority(10).intercept(chain -> false);
        }
        methods = DexKitCache.GetMethods("ZaloXposedSettings_TabMeView_GetItemList", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.maintab.me.TabMeView")
                .modifiers(Modifier.STATIC)
                .returnType("java.util.ArrayList")
                .addUsingString("tab_me_privacy", StringMatchType.Equals)
                .addUsingString("tab_me_tool_storage", StringMatchType.Equals)
                .addUsingString("tab_me_account_and_security", StringMatchType.Equals)
                .addUsingString("tab_me_business_tools", StringMatchType.Equals)
            )), classLoader);
        if (methods.isEmpty())
        {
            Logger.e("Target method not found 2");
            return;
        }
        for (Method method : methods)
        {
            Logger.h(method);
            module.hook(method).setPriority(10).intercept(chain ->
            {
                Object result = chain.proceed();
                if (!(result instanceof ArrayList))
                    return result;
                try
                {
                    String moduleName = MorpheConstants.getModuleName();
                    Object separatorItem = null;
                    ArrayList<Object> items = (ArrayList<Object>)result;
                    for (int i = items.size() - 1; i >= 0; i--)
                    {
                        Object item = items.get(i);
                        if (item.toString().contains("SettingData(id="))
                            continue;
                        separatorItem = Utils.Clone(item);
                        break;
                    }
                    Object customItem = TabMeSettingItemHelper.CreateNew(SettingItemID.PRIVACY, CUSTOM_DRAWABLE_ID, moduleName);
                    TabMeSettingItemHelper.SetDescription(customItem, S.tr("moduleSettingsDesc", moduleName));
                    TabMeSettingItemHelper.SetTracking(customItem, CUSTOM_ITEM_MARKER);
                    items.add(2, customItem);
                    items.add(3, separatorItem);
                    return items;
                }
                catch (Throwable t) { Logger.e(t); }
                return result;
            });
        }
        Method onClick = DexKitCache.GetMethod("ZaloXposedSettings_OnClickSettingData", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC)
                .paramCount(1)
                .returnType("void")
                .addUsingString("STR_SOURCE_START_VIEW", StringMatchType.Equals)
                .addUsingString("EXTRA_FEATURE_ID", StringMatchType.Equals)
                .addUsingString("https://h5.zdn.vn/zapps/220259427665569271/", StringMatchType.Equals)
                .addUsingString("features@business_account@zinstant@business_tool@enable", StringMatchType.Equals)
                .addUsingString("EXTRA_SOURCE_OPEN_MA", StringMatchType.Equals)
                .addUsingString("features@business_account@item_promotion@open_action@action_name", StringMatchType.Equals)
                .addUsingString("tab_me_open_item", StringMatchType.Equals)
                .addUsingNumber(1350)
            )), classLoader);
        if (onClick == null)
            return;
        Logger.h(onClick);
        module.hook(onClick).setPriority(10).intercept(chain ->
        {
            Object tabMeItem = chain.getArg(0);
            if (tabMeItem == null)
                return chain.proceed();
            try
            {
                if (!CUSTOM_ITEM_MARKER.equals(TabMeSettingItemHelper.GetTracking(tabMeItem)))
                    return chain.proceed();
                Object tabMeViewInst = null;
                for (Field field : onClick.getDeclaringClass().getFields())
                {
                    Object obj = field.get(chain.getThisObject());
                    if (obj == null)
                        continue;
                    if (tabMeViewClass.isAssignableFrom(obj.getClass()))
                    {
                        tabMeViewInst = obj;
                        break;
                    }
                }
                if (tabMeViewInst == null)
                    return chain.proceed();
                Object viewManagerInst = viewManager.get(tabMeViewInst);
                if (viewManagerInst != null)
                {
                    ZaloXposedSettingsMenu.isOpening = true;
                    Class<?> settingV2ViewClass = Class.forName("com.zing.zalo.ui.settings.SettingV2View", false, classLoader);
                    openView.invoke(viewManagerInst, settingV2ViewClass, null, 1);
                }
                return null;
            }
            catch (Throwable t) { Logger.e(t); }
            return chain.proceed();
        });
    }
}
