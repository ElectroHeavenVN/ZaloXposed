package io.github.electroheavenvn.zaloxposed.settingsmenu;

import java.util.List;

import io.github.electroheavenvn.zaloxposed.utilities.S;

public final class SettingSubCategory implements ISettingItem
{
    private final List<SettingFeature> features;
    private final String key;

    public List<SettingFeature> getFeatures() { return features; }
    @Override
    public String getTitle() { return S.tr(key); }
    @Override
    public String getDescription() { return ""; }

    public SettingSubCategory(String key, List<SettingFeature> features)
    {
        this.key = key;
        this.features = features;
    }
}
