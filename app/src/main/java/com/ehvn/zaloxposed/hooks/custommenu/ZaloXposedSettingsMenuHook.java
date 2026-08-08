package com.ehvn.zaloxposed.hooks.custommenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.InputType;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Config;
import com.ehvn.zaloxposed.utilities.Logger;
import com.ehvn.zaloxposed.utilities.Utils;

import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.FieldMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.ClassData;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressLint("SetTextI18n")
public class ZaloXposedSettingsMenuHook extends BaseHook
{
    private static final String CUSTOM_ITEM_MARKER = "zalo_xposed_settings";

    private static Class<?> resourceClass = null;
    private static Class<?> tabMeItemClass = null;
    private static Field tabMeItemTrackingField = null;
    private static Field tabMeItemTitleField = null;
    private static Field tabMeItemDescriptionField = null;
    private static Field tabMeItemIconField = null;
    private static boolean tabMeItemInfoLoaded = false;
    private static boolean isOpenZaloXposedSettings = false;
    private static LinearLayout rootLayout = null;
    private static boolean isEnglish = true;
    private static Class<?> headerTextViewClass = null;
    private TextView templateHeader = null;
    private View templateSeparator = null;

    private void createCustomMenu() throws Exception
    {
        Context context = rootLayout.getContext();
        View separator;
        TextView headerTitle;
        RelativeLayout listItemSetting;

        separator = createSeparator(context);
        rootLayout.addView(separator);
        headerTitle = createHeaderTitle(context);
        headerTitle.setText(isEnglish ? "Ads" : "Quảng cáo");
        rootLayout.addView(headerTitle);
        listItemSetting = ListItemSettingHelper.CreateNew(context);
        rootLayout.addView(listItemSetting);
        ListItemSettingHelper.SetIDTracking(listItemSetting, "");
        ListItemSettingHelper.ShowDivider(listItemSetting);
        ListItemSettingHelper.SetTitle(listItemSetting, isEnglish ? "Hide Media Box" : "Ẩn Media Box");
        ListItemSettingHelper.SetSwitch(listItemSetting, Config.getHideMediaBox());
        ListItemSettingHelper.SetCheckedChangeListener(listItemSetting, Config::setHideMediaBox);
        listItemSetting = ListItemSettingHelper.CreateNew(context);
        rootLayout.addView(listItemSetting);
        ListItemSettingHelper.SetIDTracking(listItemSetting, "");
        ListItemSettingHelper.HideDivider(listItemSetting);
        ListItemSettingHelper.SetTitle(listItemSetting, isEnglish ? "Hide promoted ads" : "Ẩn quảng cáo được tài trợ");
        ListItemSettingHelper.SetSubtitle(listItemSetting, isEnglish ? "Hide ads in Messages tab" : "Ẩn quảng cáo trong tab danh sách tin nhắn");
        ListItemSettingHelper.SetSwitch(listItemSetting, Config.getHidePromotedAds());
        ListItemSettingHelper.SetCheckedChangeListener(listItemSetting, Config::setHidePromotedAds);

        separator = createSeparator(context);
        rootLayout.addView(separator);
        headerTitle = createHeaderTitle(context);
        headerTitle.setText("Mini Chat (Chat Head)");
        rootLayout.addView(headerTitle);
        listItemSetting = ListItemSettingHelper.CreateNew(context);
        rootLayout.addView(listItemSetting);
        ListItemSettingHelper.SetIDTracking(listItemSetting, "");
        ListItemSettingHelper.HideDivider(listItemSetting);
        ListItemSettingHelper.SetTitle(listItemSetting, isEnglish ? "Enable" : "Kích hoạt");
        ListItemSettingHelper.SetSwitch(listItemSetting, Config.getEnableChatHead());
        ListItemSettingHelper.SetCheckedChangeListener(listItemSetting, Config::setEnableChatHead);
        listItemSetting.setEnabled(Build.VERSION.SDK_INT > Build.VERSION_CODES.Q);

        separator = createSeparator(context);
        rootLayout.addView(separator);
        headerTitle = createHeaderTitle(context);
        headerTitle.setText(isEnglish ? "Extended grid menu" : "Chat menu mở rộng");
        rootLayout.addView(headerTitle);
        listItemSetting = ListItemSettingHelper.CreateNew(context);
        rootLayout.addView(listItemSetting);
        ListItemSettingHelper.SetIDTracking(listItemSetting, "");
        ListItemSettingHelper.HideDivider(listItemSetting);
        ListItemSettingHelper.SetTitle(listItemSetting, isEnglish ? "Enable" : "Kích hoạt");
        ListItemSettingHelper.SetSwitch(listItemSetting, Config.getEnableExtendedGridMenu());
        ListItemSettingHelper.SetCheckedChangeListener(listItemSetting, Config::setEnableExtendedGridMenu);

        separator = createSeparator(context);
        rootLayout.addView(separator);
        headerTitle = createHeaderTitle(context);
        headerTitle.setText("ZCloud");
        rootLayout.addView(headerTitle);
        listItemSetting = ListItemSettingHelper.CreateNew(context);
        rootLayout.addView(listItemSetting);
        ListItemSettingHelper.SetIDTracking(listItemSetting, "");
        ListItemSettingHelper.HideDivider(listItemSetting);
        ListItemSettingHelper.SetTitle(listItemSetting, isEnglish ? "Unlock ZCloud" : "Mở khoá ZCloud");
        ListItemSettingHelper.SetSubtitle(listItemSetting, isEnglish ? "Does not increase My Documents capacity" :
        "Không tăng dung lượng bộ nhớ My Documents");
        ListItemSettingHelper.SetSwitch(listItemSetting, Config.getUnlockZCloud());
        ListItemSettingHelper.SetCheckedChangeListener(listItemSetting, Config::setUnlockZCloud);

        separator = createSeparator(context);
        rootLayout.addView(separator);
        headerTitle = createHeaderTitle(context);
        headerTitle.setText(isEnglish ? "Group role" : "Vai trò trong nhóm");
        rootLayout.addView(headerTitle);
        listItemSetting = ListItemSettingHelper.CreateNew(context);
        rootLayout.addView(listItemSetting);
        ListItemSettingHelper.SetIDTracking(listItemSetting, "");
        ListItemSettingHelper.ShowDivider(listItemSetting);
        ListItemSettingHelper.SetTitle(listItemSetting, isEnglish ? "Fake my role in groups" : "Giả mạo vai trò trong nhóm");
        ListItemSettingHelper.SetSwitch(listItemSetting, Config.getEnableFakeGroupRole());
        ListItemSettingHelper.SetCheckedChangeListener(listItemSetting, Config::setEnableFakeGroupRole);
        RelativeLayout listItemSettingFakeRole = ListItemSettingHelper.CreateNew(context);
        rootLayout.addView(listItemSettingFakeRole);
        ListItemSettingHelper.SetIDTracking(listItemSettingFakeRole, "");
        ListItemSettingHelper.HideDivider(listItemSettingFakeRole);
        ListItemSettingHelper.SetTitle(listItemSettingFakeRole, isEnglish ? "Fake role" : "Vai trò giả mạo");
        String fakeRole = getFakeRoleName(Config.getFakeGroupRoleLevel());
        ListItemSettingHelper.SetStateSetting(listItemSettingFakeRole, fakeRole);
        ListItemSettingHelper.SetOnClickListener(listItemSettingFakeRole, v ->
        {
            int roleLevel = Config.getFakeGroupRoleLevel();
            roleLevel = (roleLevel + 1) % 2;
            Config.setFakeGroupRoleLevel(roleLevel);
            String newFakeRole = getFakeRoleName(roleLevel);
            try
            {
                ListItemSettingHelper.SetStateSetting(listItemSettingFakeRole, newFakeRole);
            }
            catch (Exception e)
            {
                Logger.e(e);
            }
        });

        separator = createSeparator(context);
        rootLayout.addView(separator);
        headerTitle = createHeaderTitle(context);
        headerTitle.setText(isEnglish ? "Disappearing messages" : "Tin nhắn tự xoá");
        rootLayout.addView(headerTitle);
        listItemSetting = ListItemSettingHelper.CreateNew(context);
        rootLayout.addView(listItemSetting);
        ListItemSettingHelper.SetIDTracking(listItemSetting, "");
        ListItemSettingHelper.ShowDivider(listItemSetting);
        ListItemSettingHelper.SetTitle(listItemSetting, isEnglish ? "Override disappearing messages config" : "Ghi đè cấu hình tin nhắn tự xoá");
        ListItemSettingHelper.SetSwitch(listItemSetting, Config.getEnableTTLOverride());
        ListItemSettingHelper.SetCheckedChangeListener(listItemSetting, Config::setEnableTTLOverride);
        headerTitle = createHeaderTitle(context);
        headerTitle.setText(isEnglish ? "Disappearing message time" : "Thời gian tin nhắn tự xoá");
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
        input.setText(Config.getTTL() + "");
        input.setHint(isEnglish ? "Enter value (milliseconds)" : "Nhập giá trị (mili giây)");
        styleEditText(input, context, listItemSetting);
        input.setOnEditorActionListener((textView, actionId, keyEvent) ->
        {
            try
            {
                if (actionId != EditorInfo.IME_ACTION_DONE)
                    return false;
                long ttlValue = Long.parseLong(textView.getText().toString());
                Config.setTTL(ttlValue);
            }
            catch (Exception e)
            {
                Logger.e(e);
            }
            return false;
        });
        input.setOnFocusChangeListener((view, hasFocus) ->
        {
            if (!hasFocus)
            {
                try
                {
                    EditText editText = (EditText)view;
                    long ttlValue = Long.parseLong(editText.getText().toString());
                    Config.setTTL(ttlValue);
                }
                catch (Exception e)
                {
                    Logger.e(e);
                }
            }
        });

        separator = createSeparator(context);
        rootLayout.addView(separator);
        headerTitle = createHeaderTitle(context);
        headerTitle.setText(isEnglish ? "Sticker pack" : "Bộ sticker");
        rootLayout.addView(headerTitle);
        listItemSetting = ListItemSettingHelper.CreateNew(context);
        rootLayout.addView(listItemSetting);
        ListItemSettingHelper.SetIDTracking(listItemSetting, "");
        ListItemSettingHelper.HideDivider(listItemSetting);
        ListItemSettingHelper.SetTitle(listItemSetting, isEnglish ? "Allow sharing hidden sticker packs" : "Cho phép chia sẻ bộ sticker ẩn");
        ListItemSettingHelper.SetSwitch(listItemSetting, Config.getEnableShareHiddenStickerPack());
        ListItemSettingHelper.SetCheckedChangeListener(listItemSetting, Config::setEnableShareHiddenStickerPack);
    }

    @Override
    public void hook() throws Throwable
    {
        ListItemSettingHelper.Init(classLoader);
        hookTabMeView();
        hookSettingPrivateView();
    }

    @SuppressLint("PrivateApi")
    @SuppressWarnings("unchecked")
    private void hookTabMeView() throws Exception
    {
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.maintab.me.TabMeView")
                .modifiers(Modifier.STATIC)
                .returnType("java.util.ArrayList")
                .addUsingString("tab_me_privacy", StringMatchType.Equals)
                .addUsingString("tab_me_tool_storage", StringMatchType.Equals)
                .addUsingString("tab_me_account_and_security", StringMatchType.Equals)
                .addUsingString("tab_me_business_tools", StringMatchType.Equals)));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        Method method = methods.get(0).getMethodInstance(classLoader);
        Logger.i("Hooking: " + method);
        module.hook(method).intercept(chain ->
        {
            Object result = chain.proceed();
            if (!(result instanceof ArrayList))
                return result;
            try
            {
                ArrayList<Object> items = (ArrayList<Object>) result;
                loadTabMeItemInfo(items);
                Object customItem = buildCustomTabMeMenuItem(items);
                if (customItem == null)
                    return result;
                Object separatorItem = null;
                for (int i = items.size() - 1; i >= 0; i--)
                {
                    Object item = items.get(i);
                    if (!item.toString().contains("SettingData(id="))
                    {
                        separatorItem = item;
                        break;
                    }
                }
                items.add(2, customItem);
                items.add(3, separatorItem);
                return items;
            }
            catch (Throwable t)
            {
                Logger.e(t);
            }
            return result;
        });

        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC)
                .paramCount(1)
                .returnType("void")
                .addUsingString("settingData", StringMatchType.Equals)
                .addUsingString("STR_SOURCE_START_VIEW", StringMatchType.Equals)
                .addUsingString("EXTRA_FEATURE_ID", StringMatchType.Equals)
                .addUsingString("features@business_account@zinstant@business_tool@enable", StringMatchType.Equals)
                .addUsingString("EXTRA_SOURCE_OPEN_MA", StringMatchType.Equals)
                .addUsingString("features@business_account@item_promotion@open_action@action_name", StringMatchType.Equals)
                .addUsingString("tab_me_open_item", StringMatchType.Equals)
                // .addUsingString("https://h5.zdn.vn/zapps/220259427665569271/", StringMatchType.Equals)
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target onClick method not found");
            return;
        }
        method = methods.get(0).getMethodInstance(classLoader);
        Logger.i("Hooking: " + method);
        module.hook(method).intercept(chain ->
        {
            Object tabMeItem = chain.getArg(0);
            if (tabMeItem == null)
                return chain.proceed();
            if (tabMeItem.getClass() != tabMeItemClass)
                return chain.proceed();
            try
            {
                tabMeItemTrackingField.setAccessible(true);
                String trackingValue = (String) tabMeItemTrackingField.get(tabMeItem);
                isOpenZaloXposedSettings = CUSTOM_ITEM_MARKER.equals(trackingValue);
            }
            catch (Throwable t)
            {
                Logger.e(t);
            }
            return chain.proceed();
        });
    }

    private synchronized void loadTabMeItemInfo(ArrayList<Object> tabMeItems)
    {
        if (tabMeItemInfoLoaded)
            return;
        int iconValue = getResourceIdByName("zds_ic_storage_line_24");
        for (Object item : tabMeItems)
        {
            Field field = Utils.FindFieldByValue(item, "tab_me_tool_storage");
            if (field == null)
                continue;
            tabMeItemClass = item.getClass();
            tabMeItemTrackingField = field;
            for (Field f : tabMeItemClass.getDeclaredFields())
            {
                try
                {
                    if (f.getType() == String.class)
                    {
                        f.setAccessible(true);
                        String value = (String) f.get(item);
                        if (value == null)
                            continue;
                        if (value.equals("Dữ liệu trên máy") || value.equals("Data on device"))
                            tabMeItemTitleField = f;
                        else if (value.equals("Quản lý dữ liệu Zalo của bạn") || value.equals("Manage your Zalo data"))
                            tabMeItemDescriptionField = f;
                    }
                    else if (f.getType() == int.class && tabMeItemIconField == null)
                    {
                        int value = f.getInt(item);
                        if (value == iconValue)
                            tabMeItemIconField = f;
                    }
                }
                catch (Throwable ignored) { }
            }
            break;
        }
        tabMeItemInfoLoaded = true;
    }

    private Object buildCustomTabMeMenuItem(ArrayList<Object> items)
    {
        try
        {
            Object template = null;
            for (Object item : items)
            {
                if (!item.toString().contains("SettingData(id="))
                    continue;
                String title = (String) tabMeItemTitleField.get(item);
                if (title == null)
                    continue;
                if (title.equals("Privacy") || title.equals("Quyền riêng tư"))
                {
                    template = item;
                    isEnglish = title.equals("Privacy");
                    break;
                }
            }
            if (template == null)
            {
                Logger.e("Template item not found, cannot create custom menu item");
                return null;
            }
            Object newItem = Utils.UnsafeAllocate(tabMeItemClass);
            for (Field field : Utils.GetAllFields(tabMeItemClass))
            {
                field.setAccessible(true);
                field.set(newItem, field.get(template));
            }
            tabMeItemTrackingField.setAccessible(true);
            tabMeItemTrackingField.set(newItem, CUSTOM_ITEM_MARKER);
            tabMeItemTitleField.setAccessible(true);
            tabMeItemTitleField.set(newItem, "ZaloXposed");
            tabMeItemDescriptionField.setAccessible(true);
            if (isEnglish)
                tabMeItemDescriptionField.set(newItem, "ZaloXposed settings");
            else
                tabMeItemDescriptionField.set(newItem, "Cài đặt ZaloXposed");
            tabMeItemIconField.setAccessible(true);
            tabMeItemIconField.setInt(newItem, getResourceIdByName("zds_oic_premium_crown_color_24"));
            return newItem;
        }
        catch (Throwable t)
        {
            Logger.e(t);
            return null;
        }
    }

    private int getResourceIdByName(String resourceName)
    {
        try
        {
            if (resourceClass == null)
            {
                List<ClassData> classes = bridge.findClass(FindClass.create()
                    .matcher(ClassMatcher.create()
                        .addFieldForName("zds_ic_storage_line_24")
                    ));
                for (ClassData classData : classes)
                {
                    try
                    {
                        Class<?> clazz = classData.getInstance(classLoader);
                        if (clazz.getName().equals("com.zing.zalo.R.drawable"))
                            continue;
                        resourceClass = clazz;
                        break;
                    }
                    catch (Throwable ignored) { }
                }
            }
            if (resourceClass == null)
            {
                Logger.e("Resource class not found");
                return 0;
            }
            Field field = resourceClass.getField(resourceName);
            return field.getInt(null);
        }
        catch (Exception e)
        {
            Logger.e("Error getting resource ID for " + resourceName);
            Logger.e(e);
        }
        return 0;
    }

    private void hookSettingPrivateView() throws Exception
    {
        headerTextViewClass = Class.forName("com.zing.zalo.ui.widget.RobotoTextView", false, classLoader);

        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.settings.SettingPrivateV2View")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("android.view.View")
                .paramCount(3)
                .paramTypes("android.view.LayoutInflater", "android.widget.LinearLayout", "android.os.Bundle")
                .addUsingString("inflater", StringMatchType.Equals)
                .addUsingString("EXTRA_CURRENT_SEEN_SETTING", StringMatchType.Equals)));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        Method method = methods.get(0).getMethodInstance(classLoader);
        Logger.i("Hooking: " + method);
        module.hook(method).intercept(chain ->
        {
            Object result = chain.proceed();
            if (!isOpenZaloXposedSettings)
            {
                rootLayout = null;
                return result;
            }
            rootLayout = (LinearLayout)chain.getArg(1);
            return result;
        });
        methods = bridge.findMethod(FindMethod.create()
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
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        method = methods.get(0).getMethodInstance(classLoader);
        Logger.i("Hooking: " + method);
        module.hook(method).intercept(chain ->  
        {
            Object result = chain.proceed();
            if (!isOpenZaloXposedSettings)
                return result;
            if (rootLayout == null)
                return result;
            templateHeader = null;
            templateSeparator = null;
            try
            {
                loadTemplates();
                createCustomMenu();
            }
            catch (Exception e)
            {
                Logger.e(e);
            }
            return result;
        });
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.settings.SettingPrivateV2View")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("void")
                .paramCount(0)
                .addUsingString("getString(...)", StringMatchType.Equals)
                .addUsingField(FieldMatcher.create().name("str_title_setting_private"))
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        method = methods.get(0).getMethodInstance(classLoader);
        Field actionBarField = Utils.FindFieldByType(Class.forName("com.zing.zalo.ui.settings.SettingPrivateV2View", false, classLoader), "com.zing.zalo.zdesign.component.header.ZdsActionBar");
        Logger.i("Hooking: " + method);
        module.hook(method).intercept(chain ->  
        {
            Object result = chain.proceed();
            if (!isOpenZaloXposedSettings)
                return result;
            if (actionBarField == null)
                return result;
            try
            {
                Object actionBar = actionBarField.get(chain.getThisObject());
                if (actionBar == null)
                    return result;
                Method getMiddleTitle = actionBar.getClass().getMethod("getMiddleTitle");
                String title = (String) getMiddleTitle.invoke(actionBar);
                if ("Privacy".equals(title))
                    isEnglish = true;
                else if ("Quyền riêng tư".equals(title))
                    isEnglish = false;
                Method setMiddleTitle = actionBar.getClass().getMethod("setMiddleTitle", String.class);
                setMiddleTitle.invoke(actionBar, isEnglish ? "ZaloXposed Settings" : "Cài đặt ZaloXposed");
            }
            catch (Exception t)
            {
                Logger.e(t);
            }
            return result;
        });
    }

    @NonNull
    private static String getFakeRoleName(int roleLevel)
    {
        return switch (roleLevel)
        {
            case 0 -> isEnglish ? "Admin" : "Phó nhóm";
            case 1 -> isEnglish ? "Owner" : "Trưởng nhóm";
            default -> "";
        };
    }

    @NonNull
    private TextView createHeaderTitle(Context context) throws Exception
    {
        TextView headerTitle = (TextView) headerTextViewClass.getConstructor(Context.class).newInstance(context);
        headerTitle.setVisibility(View.VISIBLE);
        if (templateHeader != null)
        {
            headerTitle.setTextColor(templateHeader.getTextColors());
            headerTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, templateHeader.getTextSize());
            headerTitle.setTypeface(templateHeader.getTypeface());
            headerTitle.setPadding(templateHeader.getPaddingLeft(), templateHeader.getPaddingTop(), templateHeader.getPaddingRight(), templateHeader.getPaddingBottom());
            try
            {
                headerTitle.setBackground(Objects.requireNonNull(templateHeader.getBackground()
                    .getConstantState())
                    .newDrawable()
                    .mutate()
                );
            }
            catch (Exception ignored)
            {
                headerTitle.setBackgroundColor(Color.TRANSPARENT);
            }
            if (templateHeader.getLayoutParams() instanceof LinearLayout.LayoutParams)
            {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((LinearLayout.LayoutParams) templateHeader.getLayoutParams());
                headerTitle.setLayoutParams(lp);
            }
        }
        else
        {
            headerTitle.setTextColor(0xFF808080);
            headerTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            headerTitle.setBackgroundColor(Color.TRANSPARENT);
            int pad16 = dp(context, 16);
            int pad8 = dp(context, 8);
            headerTitle.setPadding(pad16, pad16, pad16, pad8);
        }
        return headerTitle;
    }

    @NonNull
    private View createSeparator(Context context)
    {
        View separator = new View(context);
        if (templateSeparator != null)
        {
            try
            {
                separator.setBackground(Objects.requireNonNull(templateSeparator.getBackground()
                    .getConstantState())
                    .newDrawable()
                    .mutate()
                );
            }
            catch (Exception ignored) { }
            if (templateSeparator.getLayoutParams() instanceof LinearLayout.LayoutParams)
            {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((LinearLayout.LayoutParams) templateSeparator.getLayoutParams());
                separator.setLayoutParams(lp);
            }
        }
        else
        {
            separator.setBackgroundColor(Color.TRANSPARENT);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(context, 8));
            separator.setLayoutParams(lp);
        }
        return separator;
    }

    private void loadTemplates()
    {
        // Find templates, then hide all existing children
        int count = rootLayout.getChildCount();
        for (int i = 0; i < count; i++)
        {
            View child = rootLayout.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE)
                continue;
            if (templateHeader == null && child.getClass() == headerTextViewClass)
                templateHeader = (TextView) child;
            else if (templateSeparator == null && child.getClass() == View.class)
                templateSeparator = child;
        }
        for (int i = 0; i < count; i++)
        {
            View child = rootLayout.getChildAt(i);
            child.setVisibility(View.GONE);
        }
    }

    private int dp(Context context, int value)
    {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }

    private void styleEditText(EditText editText, Context context, RelativeLayout templateItem)
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
            editText.setBackground(Objects.requireNonNull(templateItem.getBackground().getConstantState())
                .newDrawable()
                .mutate());
        }
        catch (Exception ignored) { } 
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(lp);
    }

    private TextView findTitleTextView(View view)
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
}