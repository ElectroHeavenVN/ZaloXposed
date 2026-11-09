package io.github.electroheavenvn.zaloxposed.hooks.custommenu;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.settingsmenu.ListSelectionSettingFeature;
import io.github.electroheavenvn.zaloxposed.settingsmenu.SettingFeature;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;

public abstract class SettingViewsDefacerBase extends BaseHook
{
    public abstract void hook() throws Throwable;

    protected static ColorStateList textColor = null;

    protected static View ProcessSettingFeature(LinearLayout rootLayout, SettingFeature feature, boolean hideDivider) throws Exception
    {
        Context context = rootLayout.getContext();
        RelativeLayout listItemSetting = ListItemSettingHelper.CreateNew(context);
        if (textColor == null)
        {
            TextView listItemSettingTitle = findTitleTextView(listItemSetting);
            if (listItemSettingTitle != null)
                textColor = listItemSettingTitle.getTextColors();
        }
        if (hideDivider)
            ListItemSettingHelper.HideDivider(listItemSetting);
        else
            ListItemSettingHelper.ShowDivider(listItemSetting);
        ListItemSettingHelper.SetTitle(listItemSetting, feature.getTitle());
        ListItemSettingHelper.SetSubtitle(listItemSetting, feature.getDescription());
        switch (feature.getConfigType())
        {
            case OPTION:
                ListItemSettingHelper.SetShowChevronRight(listItemSetting, true);
                ListItemSettingHelper.SetOnClickListener(listItemSetting, view -> feature.onOptionClicked(view.getContext()));
                break;
            case SWITCH:
                ListItemSettingHelper.SetSwitch(listItemSetting, (boolean)feature.getCurrentState());
                ListItemSettingHelper.SetCheckedChangeListener(listItemSetting, value -> feature.onCheckedChanged(context, value));
                break;
            case CHECKBOX:
                ListItemSettingHelper.SetCheckBox(listItemSetting, (boolean)feature.getCurrentState());
                ListItemSettingHelper.SetCheckedChangeListener(listItemSetting, value -> feature.onCheckedChanged(context, value));
                break;
            case LIST:
            {
                //TODO: open drop down menu instead of rotating
                ListSelectionSettingFeature listFeature = (ListSelectionSettingFeature)feature;
                ListItemSettingHelper.SetStateSetting(listItemSetting, listFeature.getSelectedTitle());
                ListItemSettingHelper.SetOnClickListener(listItemSetting, v ->
                {
                    try
                    {
                        listFeature.onOptionClicked(context);
                        ListItemSettingHelper.SetStateSetting(listItemSetting, listFeature.getSelectedTitle());
                    }
                    catch (Exception e) { Logger.e(e); }
                });
                break;
            }
            case BUTTON:
            {
                int pad16 = dp(context, 16);
                Object btn = ZButtonHelper.CreateNew(context);
                ZButtonHelper.SetText(btn, feature.getTitle());
                ZButtonHelper.SetOnClickListener(btn, view -> feature.onOptionClicked(view.getContext()));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(pad16, dp(context, 8), pad16, 0);
                ((View)btn).setLayoutParams(lp);
                return (View)btn;
            }
        }
        listItemSetting.setEnabled(feature.getEnabled());
        return listItemSetting;
    }

    protected static void hideKeyboard(View view)
    {
        InputMethodManager imm = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    protected static TextView findTitleTextView(View view)
    {
        if (view instanceof TextView)
            return (TextView)view;
        if (view instanceof ViewGroup group)
        {
            for (int i = 0; i < group.getChildCount(); i++)
            {
                TextView found = findTitleTextView(group.getChildAt(i));
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    protected static View createSeparator(Context context)
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

    protected static int dp(Context context, int value)
    {
        return (int)(value * context.getResources().getDisplayMetrics().density);
    }
}
