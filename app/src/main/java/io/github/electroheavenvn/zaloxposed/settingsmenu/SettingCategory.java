package io.github.electroheavenvn.zaloxposed.settingsmenu;

import java.util.List;

import io.github.electroheavenvn.zaloxposed.utilities.S;

public final class SettingCategory implements ISettingItem
{
    private final List<SettingSubCategory> subCategories;
    private final String keyTitle;
    private final String keyDesc;

    public List<SettingSubCategory> getSubCategories() { return subCategories; }
    @Override
    public String getTitle() { return S.tr(keyTitle); }
    @Override
    public String getDescription() { return S.tr(keyDesc); }

    public SettingCategory(String titleKey, String descKey, List<SettingSubCategory> subCategories)
    {
        keyTitle = titleKey;
        keyDesc = descKey;
        this.subCategories = subCategories;
    }
}
