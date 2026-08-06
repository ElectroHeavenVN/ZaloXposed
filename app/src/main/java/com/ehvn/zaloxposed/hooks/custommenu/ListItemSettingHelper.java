package com.ehvn.zaloxposed.hooks.custommenu;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;

public final class ListItemSettingHelper
{
    private static Class<?> listItemSettingClass;
    private static Class<?> listItemClass;

    public static void Init(ClassLoader loader) throws ClassNotFoundException
    {
        listItemSettingClass = Class.forName("com.zing.zalo.ui.settings.widget.ListItemSetting", false, loader);
        listItemClass = Class.forName("com.zing.zalo.zdesign.component.ListItem", false, loader);
    }

    public static RelativeLayout CreateNew(Context context) throws Exception
    { 
        Object listItemSetting = listItemSettingClass.getConstructor(Context.class).newInstance(context);
        Method onFinishInflate = listItemSettingClass.getDeclaredMethod("onFinishInflate");
        onFinishInflate.setAccessible(true);
        onFinishInflate.invoke(listItemSetting);
        return (RelativeLayout)listItemSetting;
    }
    
    public static void SetTitle(RelativeLayout listItemSetting, String title) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setTitle", CharSequence.class).invoke(listItemSetting, title);
    }

    public static void SetSwitch(RelativeLayout listItemSetting, boolean checked) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setSwitch", boolean.class).invoke(listItemSetting, checked);
    }

    public static void SetCheckedChangeListener(RelativeLayout listItemSetting, Consumer<Boolean> onChanged) throws Exception
    { 
        CheckType(listItemSetting);
        for (Method m : listItemSetting.getClass().getMethods())
        {
            if (!m.getName().equals("setCheckedChangeListener") || m.getParameterTypes().length != 1)
                continue;
            Class<?> listenerInterface = m.getParameterTypes()[0];
            Object proxy = Proxy.newProxyInstance(listenerInterface.getClassLoader(), new Class<?>[]{listenerInterface}, (proxyObj, method, args) ->
            {
                if (args == null)
                    return null;
                for (Object arg : args)
                {
                    if (arg instanceof Boolean)
                    {
                        onChanged.accept((Boolean) arg);
                        break;
                    }
                }
                return null;
            });
            m.invoke(listItemSetting, proxy);
            break;
        } 
    }

    public static void SetOnClickListener(RelativeLayout listItemSetting, View.OnClickListener onClickListener) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setOnClickListener", View.OnClickListener.class).invoke(listItemSetting, onClickListener);
    }

    public static void SetIDTracking(RelativeLayout listItemSetting, String id) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setIdTracking", String.class).invoke(listItemSetting, id);
    }

    public static void HideDivider(RelativeLayout listItemSetting) throws Exception
    {
        CheckType(listItemSetting);
        LinearLayout divider = (LinearLayout)listItemClass.getMethod("getDivider").invoke(listItemSetting);
        if (divider != null)
            divider.setVisibility(View.GONE);
    }

    public static void ShowDivider(RelativeLayout listItemSetting) throws Exception
    {
        CheckType(listItemSetting);
        LinearLayout divider = (LinearLayout)listItemClass.getMethod("getDivider").invoke(listItemSetting);
        if (divider != null)
            divider.setVisibility(View.VISIBLE);
    }

    public static void SetStateSetting(RelativeLayout listItemSetting, String state) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setStateSetting", String.class).invoke(listItemSetting, state);
    }

    private static void CheckType(RelativeLayout listItemSetting) throws Exception
    {
        if (!listItemSettingClass.isInstance(listItemSetting))
            throw new IllegalArgumentException("Object is not an instance of ListItemSetting");
    }
}