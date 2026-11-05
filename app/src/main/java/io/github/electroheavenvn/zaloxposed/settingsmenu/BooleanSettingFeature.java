package io.github.electroheavenvn.zaloxposed.settingsmenu;

import io.github.electroheavenvn.zaloxposed.utilities.Config;

public class BooleanSettingFeature extends SettingFeature
{
    private final String configKey;
    public BooleanSettingFeature(String keyTitle, String keyDesc, String configKey, FeatureConfigType configType)
    {
        super(keyTitle, keyDesc, configType);
        this.configKey = configKey;
        setOnLoadListener((item, context) -> {
            Object value = Config.get(configKey, false);
            if (value == null || value instanceof Boolean)
                currentState = value;
        });
        setOnCheckedChangedListener((item, context, value) -> {
            Config.set(configKey, value);
            currentState = value;
        });
    }
}
