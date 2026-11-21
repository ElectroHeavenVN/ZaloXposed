package io.github.electroheavenvn.zaloxposed.settingsmenu;

public final class Button extends SettingFeature
{
    public Button(String key, OnOptionClickListener listener)
    {
        super(key, "", FeatureConfigType.BUTTON);
        setOnOptionClickedListener(listener);
    }
}
