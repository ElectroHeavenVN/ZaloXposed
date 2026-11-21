package io.github.electroheavenvn.zaloxposed.settingsmenu;

import android.content.Context;
import android.widget.LinearLayout;

import io.github.electroheavenvn.zaloxposed.utilities.S;

/**
 * Represent a ListItemSetting (has title, description, and an optional switch / state string) in the ZaloXposed settings menu.
 */
public class SettingFeature implements ISettingItem
{
    public interface OnAddAdditionalViewsListener 
    {
        void onAddAdditionalViews(SettingFeature item, LinearLayout layout);
    }

    public interface OnCheckedChangedListener 
    {
        void onCheckedChanged(SettingFeature item, Context context, boolean value);
    }

    public interface OnOptionClickListener 
    {
        void onOptionClicked(SettingFeature item, Context context);
    }

    public interface OnLoadListener
    {
        void onLoad(SettingFeature item, Context context);
    }

    public interface OnFieldValueChangedListener
    {
        void onFieldValueChanged(SettingFeature item, Context context, Object fieldValue);
    }

    Object currentState = null;
    private boolean isEnabled = true;
    private final String titleKey;
    private final String descKey;
    private final FeatureConfigType configType;
    private OnAddAdditionalViewsListener onAddAdditionalViews;
    private OnCheckedChangedListener onCheckedChanged;
    private OnOptionClickListener onOptionClicked;
    private OnLoadListener onLoad;
    private OnFieldValueChangedListener onFieldValueChanged;

    @Override
    public String getTitle() { return S.tr(titleKey); }
    @Override
    public String getDescription() { return S.tr(descKey); }
    public FeatureConfigType getConfigType() { return configType; }
    public Object getCurrentState() { return currentState; }
    public boolean getEnabled() { return isEnabled; }

    public SettingFeature(String keyTitle, String keyDesc, FeatureConfigType configType)
    {
        this.titleKey = keyTitle;
        this.descKey = keyDesc;
        this.configType = configType;
    }

    public SettingFeature setOnAddAdditionalViewsListener(OnAddAdditionalViewsListener listener)
    {
        this.onAddAdditionalViews = listener;
        return this;
    }

    public SettingFeature setEnabled(boolean value)
    {
        isEnabled = value;
        return this;
    }

    public SettingFeature setOnCheckedChangedListener(OnCheckedChangedListener listener)
    {
        this.onCheckedChanged = listener;
        return this;
    }

    public SettingFeature setOnFieldValueChangedListener(OnFieldValueChangedListener listener)
    {
        this.onFieldValueChanged = listener;
        return this;
    }

    public SettingFeature setOnOptionClickedListener(OnOptionClickListener listener)
    {
        this.onOptionClicked = listener;
        return this;
    }

    public SettingFeature setOnLoadListener(OnLoadListener listener)
    {
        this.onLoad = listener;
        return this;
    }

    public void addAdditionalViews(LinearLayout layout)
    {
        if (onAddAdditionalViews != null) 
            onAddAdditionalViews.onAddAdditionalViews(this, layout);
    }

    public void onCheckedChanged(Context context, boolean value)
    {
        if (onCheckedChanged != null) 
            onCheckedChanged.onCheckedChanged(this, context, value);
    }

    public void onOptionClicked(Context context)
    {
        if (onOptionClicked != null) 
            onOptionClicked.onOptionClicked(this, context);
    }

    public void onFieldValueChanged(Context context, Object fieldValue)
    {
        if (onFieldValueChanged != null)
            onFieldValueChanged.onFieldValueChanged(this, context, fieldValue);
    }

    public void onLoad(Context context)
    {
        if (onLoad != null)
            onLoad.onLoad(this, context);
    }
}
