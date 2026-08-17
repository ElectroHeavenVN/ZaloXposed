package com.ehvn.zaloxposed.hooks.custommenu;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.lang.reflect.Method;

public final class ZButtonHelper
{
    private static Class<?> buttonClass;

    public static void Init(ClassLoader loader) throws ClassNotFoundException
    {
        buttonClass = Class.forName("com.zing.zalo.zdesign.component.Button", false, loader);
    }

    public static Object CreateNew(Context context) throws Exception
    {
        return buttonClass.getConstructor(Context.class).newInstance(context);
    }

    public static void SetText(Object button, CharSequence text) throws Exception
    {
        CheckType(button);
        buttonClass.getMethod("setText", CharSequence.class).invoke(button, text);
    }

    public static void SetSupportiveIcon(Object button, Drawable drawable) throws Exception
    {
        CheckType(button);
        buttonClass.getMethod("setSupportiveIcon", Drawable.class).invoke(button, drawable);
    }

    public static void SetSupportiveIcon(Object button, int resId) throws Exception
    {
        CheckType(button);
        buttonClass.getMethod("setSupportiveIcon", int.class).invoke(button, resId);
    }

    public static void SetSupportiveIconSize(Object button, int px) throws Exception
    {
        CheckType(button);
        buttonClass.getMethod("setSupportiveIconSize", int.class).invoke(button, px);
    }

    public static void SetSupportiveIconPadding(Object button, int px) throws Exception
    {
        CheckType(button);
        buttonClass.getMethod("setSupportiveIconPadding", int.class).invoke(button, px);
    }

    public static void SetSupportiveIconTintColor(Object button, ColorStateList colorStateList) throws Exception
    {
        CheckType(button);
        buttonClass.getMethod("setSupportiveIconTintColor", ColorStateList.class).invoke(button, colorStateList);
    }

    public static void SetForceTintSupportiveIcon(Object button, boolean force) throws Exception
    {
        CheckType(button);
        buttonClass.getMethod("setForceTintSupportiveIcon", boolean.class).invoke(button, force);
    }

    public static void SetBtnType(Object button, int type) throws Exception
    {
        CheckType(button);
        buttonClass.getMethod("setBtnType", int.class).invoke(button, type);
    }

    public static void SetBtnTypeSize(Object button, int size) throws Exception
    {
        CheckType(button);
        buttonClass.getMethod("setBtnTypeSize", int.class).invoke(button, size);
    }

    public static int GetBtnType(Object button) throws Exception
    {
        CheckType(button);
        return (int) buttonClass.getMethod("getBtnType").invoke(button);
    }

    public static int GetBtnTypeSize(Object button) throws Exception
    {
        CheckType(button);
        return (int) buttonClass.getMethod("getBtnTypeSize").invoke(button);
    }

    public static void SetIdTracking(Object button, String id) throws Exception
    {
        CheckType(button);
        buttonClass.getMethod("setIdTracking", String.class).invoke(button, id);
    }

    public static void SetTrackingExtraData(Object button, Object data) throws Exception
    {
        CheckType(button);
        for (Method m : buttonClass.getMethods())
        {
            if (!m.getName().equals("setTrackingExtraData") || m.getParameterTypes().length != 1)
                continue;
            m.invoke(button, data);
            break;
        }
    }

    public static void SetOnClickListener(Object button, View.OnClickListener onClickListener) throws Exception
    {
        CheckType(button);
        buttonClass.getMethod("setOnClickListener", View.OnClickListener.class).invoke(button, onClickListener);
    }

    public static void SetEnabled(Object button, boolean enabled) throws Exception
    {
        CheckType(button);
        buttonClass.getMethod("setEnabled", boolean.class).invoke(button, enabled);
    }

    public static boolean IsEnabled(Object button) throws Exception
    {
        CheckType(button);
        return (boolean) buttonClass.getMethod("isEnabled").invoke(button);
    }

    private static void CheckType(Object button) throws Exception
    {
        if (!buttonClass.isInstance(button))
            throw new IllegalArgumentException("Object is not an instance of Button");
    }
}
