package io.github.electroheavenvn.zaloxposed.hooks.custommenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import io.github.electroheavenvn.zaloxposed.settingsmenu.Button;
import io.github.electroheavenvn.zaloxposed.settingsmenu.FeatureConfigType;
import io.github.electroheavenvn.zaloxposed.settingsmenu.ISettingItem;
import io.github.electroheavenvn.zaloxposed.settingsmenu.SettingCategory;
import io.github.electroheavenvn.zaloxposed.settingsmenu.SettingFeature;
import io.github.electroheavenvn.zaloxposed.settingsmenu.SettingSubCategory;
import io.github.electroheavenvn.zaloxposed.settingsmenu.SettingsMenu;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;
import io.github.electroheavenvn.zaloxposed.utilities.Utils;

public class SettingPrivateV2ViewDefacer extends SettingViewsDefacerBase
{
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout rootLayout = null;
    private static Class<?> headerTextViewClass = null;
    @SuppressLint("StaticFieldLeak")
    private static TextView templateHeader = null;
    private static boolean deface = false;
    private static int categoryIndex = -1;

    private static void createCustomMenu() throws Exception
    {
        Context context = rootLayout.getContext();
        TextView subCategoryTitle;
        SettingCategory settingCategory = (SettingCategory)SettingsMenu.getSettingItems().get(categoryIndex);
        List<SettingSubCategory> subCategories = settingCategory.getSubCategories();
        for (int i = 0; i < subCategories.size(); i++)
        {
            try 
            {
                SettingSubCategory subCategory = subCategories.get(i);
                String title = subCategory.getTitle();
                if (title != null && !title.isEmpty())
                {
                    subCategoryTitle = createSubCategoryTitle(context);
                    subCategoryTitle.setText(title);
                    rootLayout.addView(subCategoryTitle);
                }
                List<SettingFeature> features = subCategory.getFeatures();
                for (int j = 0; j < features.size(); j++)
                {
                    SettingFeature feature = features.get(j);
                    feature.onLoad(context);
                    if (j > 0 && feature instanceof Button)
                        rootLayout.addView(createSeparator(context));
                    boolean hideDivider = true;
                    if (j + 1 < features.size())
                    {
                        ISettingItem item = features.get(j + 1);
                        if (!(item instanceof Button))
                            hideDivider = false;
                    }
                    View item = ProcessSettingFeature(rootLayout, feature, hideDivider);
                    if (feature.getConfigType() != FeatureConfigType.NUMBER)
                    {
                        rootLayout.addView(item);
                        continue;
                    }
                    TextView headerTitle = createHeaderTitle(context);
                    headerTitle.setText(feature.getTitle());
                    TextView titleView = findTitleTextView(headerTitle);
                    if (titleView != null)
                    {
                        TextView listItemSettingTitle = findTitleTextView(item);
                        if (listItemSettingTitle != null)
                            textColor = listItemSettingTitle.getTextColors();
                        titleView.setTextColor(textColor);
                    }
                    EditText input = new EditText(context);
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    input.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    input.setHint(feature.getDescription());
                    styleEditText(input, context, item);
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
                            catch (Exception e) {Logger.e(e);}
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
                    rootLayout.addView(headerTitle);
                    rootLayout.addView(input);
                }
                rootLayout.addView(createSeparator(context));
            }
            catch (Exception e) { Logger.e(e); }
        }
        for (int i = 0; i < 5; i++)
            rootLayout.addView(createSeparator(context));
    }

    @Override
    public void installHooks() throws Throwable
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
        doHook(inflate, 10, chain ->
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
        doHook(initUI, 10, chain ->
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
        doHook(setActionBarTitle, 10, chain ->
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

    private static void styleEditText(EditText editText, Context context, View templateItem)
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

}