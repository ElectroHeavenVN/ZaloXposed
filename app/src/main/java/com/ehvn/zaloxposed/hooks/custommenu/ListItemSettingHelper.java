package com.ehvn.zaloxposed.hooks.custommenu;

import android.content.Context;
import android.view.View;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;

public final class ListItemSettingHelper
{
    private static Class<?> listItemSettingClass;

    public static void Init(ClassLoader loader) throws ClassNotFoundException
    {
        listItemSettingClass = Class.forName("com.zing.zalo.ui.settings.widget.ListItemSetting", false, loader);
    }

    public static View CreateNew(Context context) throws Exception
    { 
        Object listItemSetting = listItemSettingClass.getConstructor(Context.class).newInstance(context);
        Method onFinishInflate = listItemSettingClass.getDeclaredMethod("onFinishInflate");
        onFinishInflate.setAccessible(true);
        onFinishInflate.invoke(listItemSetting);
        return (View)listItemSetting; 
    }
    
    public static void SetTitle(View listItemSetting, String title) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setTitle", CharSequence.class).invoke(listItemSetting, title);
    }

    public static void SetSwitch(View listItemSetting, boolean checked) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setSwitch", boolean.class).invoke(listItemSetting, checked);
    }

    public static void SetCheckedChangeListener(View listItemSetting, Consumer<Boolean> onChanged) throws Exception
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

    private static void CheckType(View listItemSetting) throws Exception
    {
        if (!listItemSettingClass.isInstance(listItemSetting))
            throw new IllegalArgumentException("Object is not an instance of ListItemSetting");
    }
}