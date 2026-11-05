package io.github.electroheavenvn.zaloxposed.hooks.custommenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.FieldMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.NumberFormat;
import java.util.List;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.settingsmenu.ListSelectionSettingFeature;
import io.github.electroheavenvn.zaloxposed.settingsmenu.SettingCategory;
import io.github.electroheavenvn.zaloxposed.settingsmenu.SettingFeature;
import io.github.electroheavenvn.zaloxposed.settingsmenu.SettingSubCategory;
import io.github.electroheavenvn.zaloxposed.settingsmenu.SettingsMenu;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;
import io.github.electroheavenvn.zaloxposed.utilities.Utils;

public class SettingPrivateV2ViewDefacer extends BaseHook
{
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout rootLayout = null;
    private static Class<?> headerTextViewClass = null;
    @SuppressLint("StaticFieldLeak")
    private static TextView templateHeader = null;
    private static boolean deface = false;
    private static int categoryIndex = -1;
    private static ColorStateList textColor = null;

    private static void createCustomMenu() throws Exception
    {
        Context context = rootLayout.getContext();
        TextView subCategoryTitle;
        SettingCategory settingCategory = (SettingCategory)SettingsMenu.getSettingItems().get(categoryIndex);
        List<SettingSubCategory> subCategories = settingCategory.getSubCategories();
        for (int i = 0; i < subCategories.size(); i++)
        {
            SettingSubCategory subCategory = subCategories.get(i);
            subCategoryTitle = createSubCategoryTitle(context);
            subCategoryTitle.setText(subCategory.getTitle());
            rootLayout.addView(subCategoryTitle);

            List<SettingFeature> features = subCategory.getFeatures();
            for (int j = 0; j < features.size(); j++)
            {
                SettingFeature feature = features.get(j);
                feature.onLoad(context);
                ProcessSettingFeature(rootLayout, feature, j + 1 >= features.size());
            }
            rootLayout.addView(createSeparator(context));
        }
        for (int i = 0; i < 5; i++)
            rootLayout.addView(createSeparator(context));
    }

    static void ProcessSettingFeature(LinearLayout rootLayout, SettingFeature feature, boolean hideDivider) throws Exception
    {
        Context context = rootLayout.getContext();
        RelativeLayout listItemSetting = ListItemSettingHelper.CreateNew(context);
        if (textColor == null)
        {
            TextView listItemSettingTitle = findTitleTextView(listItemSetting);
            if (listItemSettingTitle != null)
                textColor = listItemSettingTitle.getTextColors();
        }
        if (hideDivider)
            ListItemSettingHelper.HideDivider(listItemSetting);
        else
            ListItemSettingHelper.ShowDivider(listItemSetting);
        ListItemSettingHelper.SetTitle(listItemSetting, feature.getTitle());
        ListItemSettingHelper.SetSubtitle(listItemSetting, feature.getDescription());
        switch (feature.getConfigType())
        {
            case OPTION:
                break;
            case SWITCH:
                ListItemSettingHelper.SetSwitch(listItemSetting, (boolean)feature.getCurrentState());
                ListItemSettingHelper.SetCheckedChangeListener(listItemSetting, value -> feature.onCheckedChanged(context, value));
                break;
            case CHECKBOX:
                ListItemSettingHelper.SetCheckBox(listItemSetting, (boolean)feature.getCurrentState());
                ListItemSettingHelper.SetCheckedChangeListener(listItemSetting, value -> feature.onCheckedChanged(context, value));
                break;
            case LIST:
                //TODO: open drop down menu instead of rotating
                ListSelectionSettingFeature listFeature = (ListSelectionSettingFeature)feature;
                ListItemSettingHelper.SetStateSetting(listItemSetting, listFeature.getSelectedTitle());
                ListItemSettingHelper.SetOnClickListener(listItemSetting, v ->
                {
                    try
                    {
                        listFeature.onOptionClicked(context);
                        ListItemSettingHelper.SetStateSetting(listItemSetting, listFeature.getSelectedTitle());
                    }
                    catch (Exception e) { Logger.e(e); }
                });
                break;
            case NUMBER:
                TextView headerTitle = createHeaderTitle(context);
                headerTitle.setText(feature.getTitle());
                TextView titleView = findTitleTextView(headerTitle);
                if (titleView != null)
                {
                    TextView listItemSettingTitle = findTitleTextView(listItemSetting);
                    if (listItemSettingTitle != null)
                        titleView.setTextColor(listItemSettingTitle.getTextColors());
                }
                rootLayout.addView(headerTitle);
                EditText input = new EditText(context);
                rootLayout.addView(input);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                input.setImeOptions(EditorInfo.IME_ACTION_DONE);
                input.setHint(feature.getDescription());
                styleEditText(input, context, listItemSetting);

                input.setText(feature.getCurrentState().toString());
                input.setOnEditorActionListener((textView, actionId, keyEvent) ->
                {
                    if (actionId == EditorInfo.IME_ACTION_DONE)
                    {
                        try
                        {
                            Number value = NumberFormat.getInstance().parse(textView.getText().toString());
                            //TODO: add default value to NumberSettingFeature
                            if (value == null)
                                value = 0;
                            input.setText(value.toString());
                            feature.onFieldValueChanged(context, value);
                        }
                        catch (Exception e) { Logger.e(e); }
                        textView.clearFocus();
                        return true;
                    }
                    return false;
                });
                input.setOnFocusChangeListener((view, hasFocus) ->
                {
                    if (hasFocus)
                        return;
                    try
                    {
                        EditText textView = (EditText)view;
                        Number value = NumberFormat.getInstance().parse(textView.getText().toString());
                        //TODO: add default value to NumberSettingFeature
                        if (value == null)
                            value = 0;
                        input.setText(value.toString());
                        feature.onFieldValueChanged(context, value);
                        hideKeyboard(textView);
                    }
                    catch (Exception e) { Logger.e(e); }
                });
                rootLayout.setOnClickListener(v -> input.clearFocus());
                return;
        }
        listItemSetting.setEnabled(feature.getEnabled());
        rootLayout.addView(listItemSetting);
    }

    @Override
    public void hook() throws Throwable
    {
        headerTextViewClass = Class.forName("com.zing.zalo.ui.widget.RobotoTextView", false, classLoader);

        Method inflate = DexKitCache.GetMethod("ZaloXposedSettings_SettingPrivateV2View_Inflate", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.settings.SettingPrivateV2View")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("android.view.View")
                .paramCount(3)
                .paramTypes("android.view.LayoutInflater", "android.widget.LinearLayout", "android.os.Bundle")
                .addUsingString("EXTRA_CURRENT_SEEN_SETTING", StringMatchType.Equals)
            )), classLoader);
        if (inflate == null)
            return;
        Logger.h(inflate);
        module.hook(inflate).setPriority(10).intercept(chain ->
        {
            Object result = chain.proceed();
            // Bundle bundle = (Bundle)chain.getArg(2);
            // if (bundle != null && bundle.getBoolean("deface"))
            if (ZaloXposedSettingsMenu.isOpenSettingMenu)
            {
                rootLayout = (LinearLayout)chain.getArg(1);
                deface = true;
                //categoryIndex = bundle.getInt("category_index", -1);
                categoryIndex = ZaloXposedSettingsMenu.categoryIndex;
            }
            else
            {
                rootLayout = null;
                deface = false;
                categoryIndex = -1;
            }
            textColor = null;
            return result;
        });
        Method initUI = DexKitCache.GetMethod("ZaloXposedSettings_SettingPrivateV2View_InitUI", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.settings.SettingPrivateV2View")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("void")
                .paramCount(0)
                .addUsingString("view_birthday", StringMatchType.Equals)
                .addUsingString("recently_online_status", StringMatchType.Equals)
                .addUsingString("display_seen_status", StringMatchType.Equals)
                .addUsingString("accept_stranger_call", StringMatchType.Equals)
                .addUsingString("allow_auto_friend_click", StringMatchType.Equals)
                .addUsingString("SETTING_PRIVACY_APP", StringMatchType.Equals)
            )), classLoader);
        if (initUI == null)
            return;
        Logger.h(initUI);
        module.hook(initUI).setPriority(10).intercept(chain ->
        {
            Object result = chain.proceed();
            if (!deface)
                return result;
            if (rootLayout == null)
                return result;
            templateHeader = null;
            try
            {
                loadTemplates();
                int count = rootLayout.getChildCount();
                for (int i = 0; i < count; i++)
                {
                    View child = rootLayout.getChildAt(i);
                    child.setVisibility(View.GONE);
                }
                createCustomMenu();
            }
            catch (Exception e) { Logger.e(e); }
            return result;
        });
        Method setActionBarTitle = DexKitCache.GetMethod("ZaloXposedSettings_SettingPrivateV2View_SetActionBarTitle", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.settings.SettingPrivateV2View")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("void")
                .paramCount(0)
                .addUsingField(FieldMatcher.create().name("str_title_setting_private"))
                .addInvoke(MethodMatcher.create()
                    .declaredClass("com.zing.zalo.zdesign.component.header.ZdsActionBar")
                    .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                    .returnType("void")
                    .name("setMiddleTitle")
                )
            )), classLoader);
        if (setActionBarTitle == null)
            return;
        Logger.h(setActionBarTitle);
        module.hook(setActionBarTitle).setPriority(10).intercept(chain ->
        {
            Field actionBarField = DexKitCache.GetFieldRaw("ZaloXposedSettings_SettingPrivateV2View_ActionBar", classLoader);
            if (actionBarField == null)
            {
                actionBarField = Utils.FindFieldByType(Class.forName("com.zing.zalo.ui.zviews.ZaloViewNewActionBar", false, classLoader), "com.zing.zalo.zdesign.component.header.ZdsActionBar", false);
                if (actionBarField != null)
                    DexKitCache.SetFieldRaw("ZaloXposedSettings_SettingPrivateV2View_ActionBar", actionBarField);
            }
            Object result = chain.proceed();
            if (!deface)
                return result;
            if (actionBarField == null)
                return result;
            try
            {
                Object actionBar = actionBarField.get(chain.getThisObject());
                if (actionBar == null)
                    return result;
                Method setMiddleTitle = actionBar.getClass().getMethod("setMiddleTitle", String.class);
                SettingCategory settingCategory = (SettingCategory)SettingsMenu.getSettingItems().get(categoryIndex);
                setMiddleTitle.invoke(actionBar, settingCategory.getTitle());
            }
            catch (Exception e) { Logger.e(e); }
            return result;
        });
    }


    private static View createSeparator(Context context)
    {
        View separator = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (int)(10 * context.getResources().getDisplayMetrics().density)
        );
        separator.setLayoutParams(params);
        separator.setBackgroundColor(Color.TRANSPARENT);
        return separator;
    }

    private static void styleEditText(EditText editText, Context context, RelativeLayout templateItem)
    {
        int pad16 = dp(context, 16);
        int pad12 = dp(context, 12);
        editText.setPadding(pad16, pad12, pad16, pad12);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        try
        {
            TextView titleView = findTitleTextView(templateItem);
            if (titleView != null)
            {
                editText.setTextColor(titleView.getTextColors());
                editText.setHintTextColor(titleView.getTextColors());
            }
            var state = templateItem.getBackground().getConstantState();
            if (state != null)
                editText.setBackground(state.newDrawable().mutate());
        }
        catch (Exception ignored) {}
        editText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private static TextView createSubCategoryTitle(Context context) throws Exception
    {
        TextView textView = (TextView)headerTextViewClass.getConstructor(Context.class).newInstance(context);
        textView.setVisibility(View.VISIBLE);
        if (templateHeader != null)
        {
            textView.setTextColor(templateHeader.getTextColors());
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, templateHeader.getTextSize());
            textView.setTypeface(templateHeader.getTypeface());
            textView.setPadding(templateHeader.getPaddingLeft(), templateHeader.getPaddingTop(), templateHeader.getPaddingRight(), templateHeader.getPaddingBottom());
            try
            {
                var state = templateHeader.getBackground().getConstantState();
                if (state != null)
                    textView.setBackground(state.newDrawable().mutate());
            }
            catch (Exception ignored)
            {
                textView.setBackgroundColor(Color.TRANSPARENT);
            }
            if (templateHeader.getLayoutParams() instanceof LinearLayout.LayoutParams)
            {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((LinearLayout.LayoutParams)templateHeader.getLayoutParams());
                textView.setLayoutParams(lp);
            }
        }
        else
        {
            textView.setTextColor(0xFF808080);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            textView.setBackgroundColor(Color.TRANSPARENT);
            int pad16 = dp(context, 16);
            int pad8 = dp(context, 8);
            textView.setPadding(pad16, pad16, pad16, pad8);
        }
        return textView;
    }

    private static TextView createHeaderTitle(Context context) throws Exception
    {
        TextView textView = createSubCategoryTitle(context);
        textView.setTextColor(textColor);
        return textView;
    }

    private static TextView findTitleTextView(View view)
    {
        if (view instanceof TextView)
            return (TextView)view;
        if (view instanceof ViewGroup group)
        {
            for (int i = 0; i < group.getChildCount(); i++)
            {
                TextView found = findTitleTextView(group.getChildAt(i));
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    private static void loadTemplates()
    {
        int count = rootLayout.getChildCount();
        for (int i = 0; i < count; i++)
        {
            View child = rootLayout.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE)
                continue;
            if (templateHeader == null && child.getClass() == headerTextViewClass)
                templateHeader = (TextView)child;
        }
    }

    private static void hideKeyboard(View view)
    {
        InputMethodManager imm = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private static int dp(Context context, int value)
    {
        return (int)(value * context.getResources().getDisplayMetrics().density);
    }
}
