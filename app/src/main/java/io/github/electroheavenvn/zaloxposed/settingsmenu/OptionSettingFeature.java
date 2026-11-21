package io.github.electroheavenvn.zaloxposed.settingsmenu;

public final class OptionSettingFeature extends SettingFeature
{
    public OptionSettingFeature(String keyTitle, String keyDesc, OnOptionClickListener listener)
    {
        super(keyTitle, keyDesc, FeatureConfigType.OPTION);
        setOnOptionClickedListener(listener);
    }
}
