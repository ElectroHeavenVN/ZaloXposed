package io.github.electroheavenvn.zaloxposed.settingsmenu;

import io.github.electroheavenvn.zaloxposed.utilities.Config;

public final class NumberSettingFeature extends SettingFeature
{
    private final String configKey;
    public NumberSettingFeature(String keyTitle, String keyDesc, String configKey)
    {
        super(keyTitle, keyDesc, FeatureConfigType.NUMBER);
        this.configKey = configKey;
        setOnLoadListener((item, context) -> {
            Object value = Config.get(configKey, false);
            if (value == null || value instanceof Number)
                currentState = value;
        });
        setOnFieldValueChangedListener(((item, context, fieldValue) -> {
            Config.set(configKey, fieldValue);
            currentState = fieldValue;
        }));
    }
}
