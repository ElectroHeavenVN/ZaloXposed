package com.ehvn.zaloxposed.hooks;

import de.robv.android.xposed.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.*;
import org.json.*;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;
import android.view.View;
import android.view.ViewGroup;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

@SuppressWarnings("unused")
public class RestoreDevToolsMenuHook extends BaseHook {

    private static final Set<String> FORCE_VISIBLE_IDS = new HashSet<>();
    private static final Set<String> FORCE_CLICKABLE_IDS = new HashSet<>();

    static {
        FORCE_VISIBLE_IDS.add("sv_all_dev_tools");
        FORCE_VISIBLE_IDS.add("dev_tools_separator_item");
        FORCE_VISIBLE_IDS.add("rl_dev_tools_switch");
        FORCE_VISIBLE_IDS.add("btn_disable_dev_tools");
        FORCE_CLICKABLE_IDS.add("btn_disable_dev_tools");
        FORCE_CLICKABLE_IDS.add("ll_platform_tools");
        FORCE_CLICKABLE_IDS.add("ll_chat_tools");
        FORCE_CLICKABLE_IDS.add("ll_call_tools");
        FORCE_CLICKABLE_IDS.add("ll_storage_tools");
        FORCE_CLICKABLE_IDS.add("ll_autodownload_tools");
        FORCE_CLICKABLE_IDS.add("ll_backuprestore_tools");
        FORCE_CLICKABLE_IDS.add("ll_zalo_cloud_tools");
        FORCE_CLICKABLE_IDS.add("ll_social_tools");
        FORCE_CLICKABLE_IDS.add("ll_core_tools");
        FORCE_CLICKABLE_IDS.add("ll_zpf_tools");
    }

    @Override
    public void hook() throws Throwable {

        Class<?> aboutViewClass;
        try {
            aboutViewClass = Class.forName("com.zing.zalo.ui.settings.AboutView", false, lpparam.classLoader);
        } catch (Throwable t) {
            log("AboutView not found: " + t);
            return;
        }

        List<MethodData> methods = bridge.findMethod(
            FindMethod.create()
                .matcher(MethodMatcher.create()
                    .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                    .paramCount(3)
                    .returnType("android.view.View")
                    .addUsingString("Missing required view with ID: ", StringMatchType.Equals)
                    .declaredClass(aboutViewClass)
                )
        );
        for (MethodData md : methods) {
            Method method = md.getMethodInstance(lpparam.classLoader);
            log("Hooking: " + method);
            XposedBridge.hookMethod(method, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Object thisObj = param.thisObject;
                    View.OnClickListener listener = (thisObj instanceof View.OnClickListener) ? (View.OnClickListener) thisObj : null;
                    Object result = param.getResult();
                    if (result instanceof View) 
                        unlockTree((View) result, listener);
                }
            });
        }
    }

    private static void unlockTree(View view, View.OnClickListener listener) {
        if (view == null) 
            return;
        try {
            int id = view.getId();
            if (id != View.NO_ID) {
                String name = view.getResources().getResourceEntryName(id);
                if (FORCE_VISIBLE_IDS.contains(name) && view.getVisibility() != View.VISIBLE) 
                    view.setVisibility(View.VISIBLE);
                if (FORCE_CLICKABLE_IDS.contains(name) && listener != null) {
                    view.setClickable(true);
                    view.setEnabled(true);
                    view.setOnClickListener(listener);
                }
            }
        } catch (Throwable ignored) {
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) 
                unlockTree(group.getChildAt(i), listener);
        }
    }
}