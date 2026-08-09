package com.ehvn.zaloxposed.hooks.custommenu;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

    public static void SetSubtitle(RelativeLayout listItemSetting, String title) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setSubtitle", CharSequence.class).invoke(listItemSetting, title);
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

    public static void SetTitleMaxLine(RelativeLayout listItemSetting, int maxLine) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setTitleMaxLine", int.class).invoke(listItemSetting, maxLine);
    }

    public static void SetSubtitleMaxLine(RelativeLayout listItemSetting, int maxLine) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setSubtitleMaxLine", int.class).invoke(listItemSetting, maxLine);
    }

    public static void SetTitleColor(RelativeLayout listItemSetting, int color) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setTitleColor", int.class).invoke(listItemSetting, color);
    }

    public static void SetSubtitleColor(RelativeLayout listItemSetting, int color) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setSubtitleColor", int.class).invoke(listItemSetting, color);
    }

    public static void SetTitleStyleBold(RelativeLayout listItemSetting, boolean bold) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setTitleStyleBold", boolean.class).invoke(listItemSetting, bold);
    }

    public static void SetTitleFontStyle(RelativeLayout listItemSetting, int fontStyle) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setTitleFontStyle", int.class).invoke(listItemSetting, fontStyle);
    }

    public static void SetSubtitleFontStyle(RelativeLayout listItemSetting, int fontStyle) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setSubtitleFontStyle", int.class).invoke(listItemSetting, fontStyle);
    }

    public static void SetTitleDisableScaleText(RelativeLayout listItemSetting, boolean disable) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setTitleDisableScaleText", boolean.class).invoke(listItemSetting, disable);
    }

    public static void SetSubtitleDisableScaleText(RelativeLayout listItemSetting, boolean disable) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setSubtitleDisableScaleText", boolean.class).invoke(listItemSetting, disable);
    }

    public static void SetBracket(RelativeLayout listItemSetting, String value) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setBracket", CharSequence.class).invoke(listItemSetting, value);
    }

    public static void SetCheckBox(RelativeLayout listItemSetting, boolean checked) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setCheckBox", boolean.class).invoke(listItemSetting, checked);
    }

    public static void SetCheckBoxRight(RelativeLayout listItemSetting, boolean checked) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setCheckBoxRight", boolean.class).invoke(listItemSetting, checked);
    }

    public static void SetTick(RelativeLayout listItemSetting, boolean visible) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setTick", boolean.class).invoke(listItemSetting, visible);
    }

    public static void SetDisableSwitch(RelativeLayout listItemSetting, boolean disabled) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setDisableSwitch", boolean.class).invoke(listItemSetting, disabled);
    }

    public static void SetShowChevronRight(RelativeLayout listItemSetting, boolean show) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setShowChevronRight", boolean.class).invoke(listItemSetting, show);
    }

    public static void SetMarginLeftDivider(RelativeLayout listItemSetting, int margin) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setMarginLeftDivider", int.class).invoke(listItemSetting, margin);
    }

    public static void SetMessageItemStateUnread(RelativeLayout listItemSetting, boolean unread) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setMessageItemStateUnread", boolean.class).invoke(listItemSetting, unread);
    }

    public static void SetLeadingItemVisibility(RelativeLayout listItemSetting, int visibility) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setLeadingItemVisibility", int.class).invoke(listItemSetting, visibility);
    }

    public static void SetTrailingItemVisibility(RelativeLayout listItemSetting, int visibility) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setTrailingItemVisibility", int.class).invoke(listItemSetting, visibility);
    }

    public static void SetBottomItemVisibility(RelativeLayout listItemSetting, int visibility) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setBottomItemVisibility", int.class).invoke(listItemSetting, visibility);
    }

    public static void SetTopItemVisibility(RelativeLayout listItemSetting, int visibility) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setTopItemVisibility", int.class).invoke(listItemSetting, visibility);
    }

    public static void SetAboveItemVisibility(RelativeLayout listItemSetting, int visibility) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setAboveItemVisibility", int.class).invoke(listItemSetting, visibility);
    }

    public static void SetBelowItemVisibility(RelativeLayout listItemSetting, int visibility) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setBelowItemVisibility", int.class).invoke(listItemSetting, visibility);
    }

    public static void SetTitlePrefixViewVisibility(RelativeLayout listItemSetting, int visibility) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setTitlePrefixViewVisibility", int.class).invoke(listItemSetting, visibility);
    }

    public static void SetTrailingItemFirstLineVisibility(RelativeLayout listItemSetting, int visibility) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setTrailingItemFirstLineVisibility", int.class).invoke(listItemSetting, visibility);
    }

    public static void SetTvState(RelativeLayout listItemSetting, TextView textView) throws Exception
    {
        CheckType(listItemSetting);
        for (Method setTvState : listItemSetting.getClass().getMethods())
        {
            if (!setTvState.getName().equals("setTvState"))
                continue;
            setTvState.invoke(listItemSetting, textView);
            break;
        }
    }

    public static void SetSwitch(RelativeLayout listItemSetting, CompoundButton sw) throws Exception
    {
        CheckType(listItemSetting);
        for (Method setSwitch : listItemSetting.getClass().getMethods())
        {
            if (!setSwitch.getName().equals("setSwitch"))
                continue;
            if (setSwitch.getParameterTypes().length != 1)
                continue;
            if (!CompoundButton.class.isAssignableFrom(setSwitch.getParameterTypes()[0]))
                continue;
            setSwitch.invoke(listItemSetting, sw);
            break;
        }
    }

    public static void SetIconTick(RelativeLayout listItemSetting, ImageView imageView) throws Exception
    {
        CheckType(listItemSetting);
        for (Method setIconTick : listItemSetting.getClass().getMethods())
        {
            if (!setIconTick.getName().equals("setIconTick"))
                continue;
            if (setIconTick.getParameterTypes().length != 1)
                continue;
            if (!ImageView.class.isAssignableFrom(setIconTick.getParameterTypes()[0]))
                continue;
            setIconTick.invoke(listItemSetting, imageView);
            break;
        }
    }

    public static void SetIconSetting(RelativeLayout listItemSetting, ImageView imageView) throws Exception
    {
        CheckType(listItemSetting);
        for (Method setIconSetting : listItemSetting.getClass().getMethods())
        {
            if (!setIconSetting.getName().equals("setIconSetting"))
                continue;
            if (setIconSetting.getParameterTypes().length != 1)
                continue;
            if (!ImageView.class.isAssignableFrom(setIconSetting.getParameterTypes()[0]))
                continue;
            setIconSetting.invoke(listItemSetting, imageView);
            break;
        }
    }

    public static void SetIconRemind(RelativeLayout listItemSetting, ImageView imageView) throws Exception
    {
        CheckType(listItemSetting);
        for (Method setIconRemind : listItemSetting.getClass().getMethods())
        {
            if (!setIconRemind.getName().equals("setIconRemind"))
                continue;
            if (setIconRemind.getParameterTypes().length != 1)
                continue;
            if (!ImageView.class.isAssignableFrom(setIconRemind.getParameterTypes()[0]))
                continue;
            setIconRemind.invoke(listItemSetting, imageView);
            break;
        }
    }

    public static void SetCheckBox(RelativeLayout listItemSetting, CheckBox checkBox) throws Exception
    {
        CheckType(listItemSetting);
        for (Method setCheckBox : listItemSetting.getClass().getMethods())
        {
            if (!setCheckBox.getName().equals("setCheckBox"))
                continue;
            if (setCheckBox.getParameterTypes().length != 1)
                continue;
            if (!ImageView.class.isAssignableFrom(setCheckBox.getParameterTypes()[0]))
                continue;
            setCheckBox.invoke(listItemSetting, checkBox);
            break;
        }
    }

    public static void SetLlRight(RelativeLayout listItemSetting, LinearLayout linearLayout) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setLlRight", LinearLayout.class).invoke(listItemSetting, linearLayout);
    }

    public static void SetEnabled(RelativeLayout listItemSetting, boolean enabled) throws Exception
    {
        CheckType(listItemSetting);
        listItemSetting.getClass().getMethod("setEnabled", boolean.class).invoke(listItemSetting, enabled);
    }

    public static CharSequence GetTitle(RelativeLayout listItemSetting) throws Exception
    {
        CheckType(listItemSetting);
        return (CharSequence) listItemSetting.getClass().getMethod("getTitle").invoke(listItemSetting);
    }

    public static CharSequence GetSubtitle(RelativeLayout listItemSetting) throws Exception
    {
        CheckType(listItemSetting);
        return (CharSequence) listItemSetting.getClass().getMethod("getSubtitle").invoke(listItemSetting);
    }

    public static CharSequence GetBracket(RelativeLayout listItemSetting) throws Exception
    {
        CheckType(listItemSetting);
        return (CharSequence) listItemSetting.getClass().getMethod("getBracket").invoke(listItemSetting);
    }

    public static String GetViewIdTracking(RelativeLayout listItemSetting) throws Exception
    {
        CheckType(listItemSetting);
        return (String) listItemSetting.getClass().getMethod("getViewIdTracking").invoke(listItemSetting);
    }

    public static boolean GetDisableSwitch(RelativeLayout listItemSetting) throws Exception
    {
        CheckType(listItemSetting);
        return (boolean) listItemSetting.getClass().getMethod("getDisableSwitch").invoke(listItemSetting);
    }

    public static boolean IsEnabled(RelativeLayout listItemSetting) throws Exception
    {
        CheckType(listItemSetting);
        return (boolean) listItemSetting.getClass().getMethod("isEnabled").invoke(listItemSetting);
    }

    public static ImageView GetIconSetting(RelativeLayout listItemSetting) throws Exception
    {
        CheckType(listItemSetting);
        return (ImageView) listItemSetting.getClass().getMethod("getIconSetting").invoke(listItemSetting);
    }

    public static ImageView GetIconTick(RelativeLayout listItemSetting) throws Exception
    {
        CheckType(listItemSetting);
        return (ImageView) listItemSetting.getClass().getMethod("getIconTick").invoke(listItemSetting);
    }

    public static CompoundButton GetSwitch(RelativeLayout listItemSetting) throws Exception
    {
        CheckType(listItemSetting);
        return (CompoundButton) listItemSetting.getClass().getMethod("getSwitch").invoke(listItemSetting);
    }

    public static CheckBox GetCheckBox(RelativeLayout listItemSetting) throws Exception
    {
        CheckType(listItemSetting);
        return (CheckBox) listItemSetting.getClass().getMethod("getCheckBox").invoke(listItemSetting);
    }

    public static TextView GetTvState(RelativeLayout listItemSetting) throws Exception
    {
        CheckType(listItemSetting);
        return (TextView) listItemSetting.getClass().getMethod("getTvState").invoke(listItemSetting);
    }

    public static LinearLayout GetLlRight(RelativeLayout listItemSetting) throws Exception
    {
        CheckType(listItemSetting);
        return (LinearLayout) listItemSetting.getClass().getMethod("getLlRight").invoke(listItemSetting);
    }

    public static ImageView GetIconRemind(RelativeLayout listItemSetting) throws Exception
    {
        CheckType(listItemSetting);
        return (ImageView) listItemSetting.getClass().getMethod("getIconRemind").invoke(listItemSetting);
    }

    private static void CheckType(RelativeLayout listItemSetting) throws Exception
    {
        if (!listItemSettingClass.isInstance(listItemSetting))
            throw new IllegalArgumentException("Object is not an instance of ListItemSetting");
    }
}