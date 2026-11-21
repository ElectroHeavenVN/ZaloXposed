package io.github.electroheavenvn.zaloxposed.settingsmenu;

import java.util.function.Supplier;

public final class TextLabel extends SettingFeature
{
    private final Supplier<String> textProvider;
    private final boolean centered;
    private String text = "";

    public TextLabel(Supplier<String> textProvider)
    {
        this(textProvider, false);
    }

    public TextLabel(Supplier<String> textProvider, boolean centered)
    {
        super("", "", FeatureConfigType.TEXT);
        this.textProvider = textProvider;
        this.centered = centered;
        setOnLoadListener((item, context) -> refresh());
        refresh();
    }

    @Override
    public String getTitle() { return text; }

    public boolean isCentered() { return centered; }

    private void refresh()
    {
        try
        {
            text = textProvider == null ? "" : String.valueOf(textProvider.get());
        }
        catch (Exception e)
        {
            text = "";
        }
    }
}