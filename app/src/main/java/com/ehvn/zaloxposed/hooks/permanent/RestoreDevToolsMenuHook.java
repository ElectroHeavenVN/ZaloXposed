package com.ehvn.zaloxposed.hooks.permanent;

import android.view.View;
import android.view.ViewGroup;

import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Logger;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class RestoreDevToolsMenuHook extends BaseHook
{
    private static final Set<String> FORCE_VISIBLE_IDS = new HashSet<>();
    private static final Set<String> FORCE_CLICKABLE_IDS = new HashSet<>();

    static
    {
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
    public void hook() throws Throwable
    {
        Class<?> aboutViewClass;
        try
        {
            aboutViewClass = Class.forName("com.zing.zalo.ui.settings.AboutView", false, classLoader);
        }
        catch (Throwable t)
        {
            Logger.e("AboutView not found");
            Logger.e(t);
            return;
        }
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(3)
                .returnType("android.view.View")
                .addUsingString("Missing required view with ID: ", StringMatchType.Equals)
                .declaredClass(aboutViewClass)
            ));
        for (MethodData md : methods)
        {
            Method method = md.getMethodInstance(classLoader);
            Logger.i("Hooking: " + method);
            module.hook(method).intercept(chain ->
            {
                Object result = chain.proceed();
                Object thisObj = chain.getThisObject();
                View.OnClickListener listener = (thisObj instanceof View.OnClickListener) ? (View.OnClickListener)thisObj : null;
                if (result instanceof View)
                    unlockTree((View)result, listener);
                return result;
            });
        }
    }

    private static void unlockTree(View view, View.OnClickListener listener)
    {
        if (view == null)
            return;   
        if (view instanceof ViewGroup)
        {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++)
                unlockTree(group.getChildAt(i), listener);
        }
        try
        {
            int id = view.getId();
            if (id == View.NO_ID)
                return;
            String name = view.getResources().getResourceEntryName(id);
            if (FORCE_VISIBLE_IDS.contains(name) && view.getVisibility() != View.VISIBLE)
                view.setVisibility(View.VISIBLE);
            if (FORCE_CLICKABLE_IDS.contains(name) && listener != null)
            {
                view.setClickable(true);
                view.setEnabled(true);
                view.setOnClickListener(listener);
            }
        }
        catch (Throwable ignored) { }
    }
}