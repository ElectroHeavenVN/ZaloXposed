package io.github.electroheavenvn.zaloxposed.settingsmenu;

public final class CheckboxSettingFeature extends BooleanSettingFeature
{
    public CheckboxSettingFeature(String keyTitle, String keyDesc, String configKey)
    {
        super(keyTitle, keyDesc, configKey, FeatureConfigType.CHECKBOX);
    }
}
