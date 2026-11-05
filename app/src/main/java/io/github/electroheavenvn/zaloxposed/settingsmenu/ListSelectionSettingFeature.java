package io.github.electroheavenvn.zaloxposed.settingsmenu;

import java.util.List;
import java.util.Objects;

import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.S;

//TODO: open drop down menu instead of rotating
public final class ListSelectionSettingFeature extends SettingFeature
{
    public static class Selection
    {
        private final String titleKey;
        private final Object value;

        public Selection(String titleKey, Object value)
        {
            this.titleKey = titleKey;
            this.value = value;
        }

        public String getTitle()
        {
            return S.tr(titleKey);
        }

        public Object getValue()
        {
            return value;
        }
    }

    private final String configKey;
    private List<Selection> selections;
    private int index = -1;

    public ListSelectionSettingFeature(String keyTitle, String keyDesc, String configKey, List<Selection> selections)
    {
        super(keyTitle, keyDesc, FeatureConfigType.LIST);
        this.configKey = configKey;
        this.selections = selections;
        setOnLoadListener((item, context) -> {
            Object savedValue = Config.get(configKey, null);
            for (int i = 0; i < selections.size(); i++)
            {
                Selection selection = selections.get(i);
                if (!Objects.equals(selection.value, savedValue))
                    continue;
                currentState = selection;
                index = i;
                break;
            }
            if (currentState == null && !selections.isEmpty())
                currentState = selections.get(index = 0);
        });
        setOnOptionClickedListener((item, context) -> {
            index++;
            if (index >= selections.size() || index < 0)
                index = 0;
            currentState = selections.get(index);
            Config.set(configKey, ((Selection)currentState).value);
        });
    }

    public List<Selection> getSelections() { return selections; }
    public Selection getSelected() { return selections.get(index); }
    public String getSelectedTitle() { return getSelected().getTitle(); }
    public Object getSelectedValue() { return getSelected().getValue(); }
}

