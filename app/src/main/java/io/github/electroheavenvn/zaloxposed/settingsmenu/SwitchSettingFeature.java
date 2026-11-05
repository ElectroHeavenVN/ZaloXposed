package io.github.electroheavenvn.zaloxposed.settingsmenu;

public class SwitchSettingFeature extends BooleanSettingFeature
{
    public SwitchSettingFeature(String keyTitle, String keyDesc, String configKey)
    {
        super(keyTitle, keyDesc, configKey, FeatureConfigType.SWITCH);
    }
}
