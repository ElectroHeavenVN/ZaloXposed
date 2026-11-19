package io.github.electroheavenvn.zaloxposed.hooks.permanent;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;
import android.view.View;

import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.libxposed.api.XposedInterface;

public class AmoledThemeHook extends BaseHook
{
    private static final int AMOLED_BLACK = 0xFF000000;
    private static final int AMOLED_RAISED = 0xFF0A0A0A;
    private static final int AMOLED_SEPARATOR = 0xFF343434;
    private static final int AMOLED_BAR = 0xFF0A0A0A;
    private static final int NOT_SURFACE = -1;

    private static final Set<String> BLACK_ATTRS = new HashSet<>(List.of(
        "PrimaryBackgroundColor", "SecondaryBackgroundColor", "BottomNav",
        "TabBackgroundColor", "ChatBarBackgroundColor", "FooterBackgroundColor",
        "ProfilePrimaryBackgroundColor", "ProfileSecondaryBackgroundColor",
        "TimelinePrimaryBackgroundColor", "TimelineSecondaryBackgroundColor",
        "PopupBackgroundColor", "list_item_background", "HeaderStartColor", "HeaderEndColor",
        "ProfileBackgroundColor", "ProfileUserInfoBackgroundColor",
        "ProfileSectionPhotoPrimaryBackgroundColor", "StoryPrimaryBackgroundColor",
        "SearchBackgroundColor", "GalleryPickerViewBackgroundColor",
        "ChatFooterBackgroundNormal", "social_timeline_background_color_content_centric",
        "header_blue_container", "header_white_container", "layer_background",
        "layer_background_subtle", "bgCSC_Background", "CSCPrimaryBackground",
        "bottom_navigation_bg", "bottom_navigation_container"
    ));

    private static final Set<String> RAISED_ATTRS = new HashSet<>(List.of(
        "layer_container", "layer_container_selected", "popover_container", "popover_container_alt",
        "layer_background_pressed", "layer_background_selected", "layer_background_selected_alt",
        "CSCSecondaryBackground"
    ));

    private static final Set<String> SEPARATOR_ATTRS = new HashSet<>(List.of(
        "ItemSeparatorColor", "ItemSeparatorColorCall", "ItemBackgroundPress",
        "ItemBackgroundSelectedColor", "PopupBackgroundShadowColor", "FooterTopLineColor",
        "TabBottomLineColor", "ProfileLineColor", "HeaderFormBottomLineColor"
    ));

    private static final Set<String> BAR_ATTRS = new HashSet<>(List.of("hightlight_setting"));

    private static final Set<String> DARK_SURFACE_COLORS = new HashSet<>(List.of(
        "Dark_PrimaryBackgroundColor", "Dark_SecondaryBackgroundColor", "Dark_BottomNav",
        "Dark_TabBackgroundColor", "Dark_ChatBarBackgroundColor", "Dark_FooterBackgroundColor",
        "Dark_ProfilePrimaryBackgroundColor", "Dark_ProfileSecondaryBackgroundColor",
        "Dark_TimelinePrimaryBackgroundColor", "Dark_TimelineSecondaryBackgroundColor",
        "Dark_PopupBackgroundColor", "Dark_HeaderStartColor", "Dark_HeaderEndColor",
        "Dark_StoryPrimaryBackgroundColor", "Dark_SearchBackgroundColor",
        "Dark_GalleryPickerViewBackgroundColor", "Dark_ChatFooterBackgroundNormal",
        "Dark_CSCPrimaryBackground"
    ));

    private static final Set<String> DARK_SEPARATOR_COLORS = new HashSet<>(List.of(
        "Dark_ItemSeparatorColor", "Dark_ItemSeparatorColorAlpha65", "Dark_ItemSeparatorColorCall",
        "Dark_PopupBackgroundShadowColor", "Dark_ItemBackgroundPress", "Dark_FooterTopLineColor",
        "Dark_TabBottomLineColor", "Dark_ProfileLineColor", "Dark_HeaderFormBottomLineColor"
    ));

    private static final Map<Integer, Integer> SURFACE_COLOR_VALUES = new HashMap<>();
    private static final WeakHashMap<TypedArray, int[]> styledAttrs = new WeakHashMap<>();
    private static final Map<Integer, Integer> attrColorCache = new HashMap<>();
    private static Method darkModeMethod;
    private static Method cslGetColors;
    private static Method cslGetStates;

    static
    {
        map(0xFF171717, AMOLED_BLACK);   // ng95 = layer_background
        map(0xFF0D0D0D, AMOLED_BLACK);   // ng100 = layer_background_subtle
        map(0xFF212121, AMOLED_BLACK);   // ng90 = layer_container
        map(0xFF111111, AMOLED_BLACK);   // Dark_CSCPrimaryBackground
        map(0xFF1A1A1A, AMOLED_BLACK);   // section bg
        map(0xB8000000, AMOLED_BAR);     // Dark_hightlight_setting
        map(0xFF1C1C1C, AMOLED_BLACK);   // footer / tab bg
        map(0xFF1C1C1D, AMOLED_BLACK);   // Dark_FooterBackgroundColor
        map(0xFF222222, AMOLED_BLACK);   // Dark_ChatBarBackgroundColor
        map(0xFF232323, AMOLED_BLACK);   // Dark_BTIcon2Background
        map(0xFF242424, AMOLED_BLACK);   // header start/end
        map(0xFF252627, AMOLED_BLACK);   // Dark_TimelinePrimaryBackgroundColor
        map(0xFF262626, AMOLED_BLACK);   // tab discovery loading
        map(0xFF141415, AMOLED_BLACK);   // Dark_BgEmoji / StoryPrimaryBackgroundColor
        map(0xFF2E2E2E, AMOLED_RAISED);  // ng85 = popover_container
        map(0xFF3D3D3D, AMOLED_RAISED);  // ng80 = layer_background_selected_alt
        map(0xFF2F2F2F, AMOLED_RAISED);  // Dark_NormalIcon2Background
        map(0xFF292D31, AMOLED_RAISED);  // Dark_InfoBannerBackground
        map(0xFF333333, AMOLED_RAISED);  // BottomNav / SearchBackgroundColor / ImagePlaceHolder
        map(0xFF383838, AMOLED_SEPARATOR);
        map(0xFF3A3A3A, AMOLED_SEPARATOR);
        map(0xFF444444, AMOLED_SEPARATOR);
        // #2C2C2C: Dark_ReceiverBubbleChatNormal
    }

    private static void map(int from, int to) { SURFACE_COLOR_VALUES.put(from, to); }
    private static Integer remap(int color) { return SURFACE_COLOR_VALUES.get(color); }

    private static String safeName(Resources res, int id)
    {
        try
        {
            return res.getResourceEntryName(id);
        }
        catch (Throwable t)
        {
            return "?";
        }
    }

    private static boolean isContentBarView(String name)
    {
        if (name == null)
            return false;
        return name.contains("SettingItemNewView") || name.contains("ListItemSetting") || name.contains("ZinstantTabMeItem");
    }

    private static boolean isNavBarView(String name)
    {
        if (name == null)
            return false;
        return name.contains("TabBar") || name.contains("NavigationBar") || name.contains("NavBar") || name.contains("BottomNav") || name.contains("ZiaTab");
    }

    private static boolean keepOriginal(String name)
    {
        if (name == null)
            return false;
        return name.contains("BubbleChat") || name.contains("ChatBubble");
    }

    private static boolean keepOriginal(TypedArray ta, int index)
    {
        int[] attrs;
        synchronized (styledAttrs)
        {
            attrs = styledAttrs.get(ta);
        }
        if (attrs == null || index < 0 || index >= attrs.length)
            return false;
        return keepOriginal(safeName(ta.getResources(), attrs[index]));
    }

    private static Integer namedColor(TypedArray ta, int index)
    {
        int[] attrs;
        synchronized (styledAttrs)
        {
            attrs = styledAttrs.get(ta);
        }
        if (attrs == null || index < 0 || index >= attrs.length)
            return null;
        return amoledColor(ta.getResources(), attrs[index]);
    }

    private static boolean isDark()
    {
        if (darkModeMethod == null)
            return false;
        try
        {
            Object value = darkModeMethod.invoke(null);
            return value instanceof Number && ((Number)value).intValue() != 0;
        }
        catch (Throwable ignored) { }
        return false;
    }

    private static Integer amoledColor(Resources res, int attrId)
    {
        if (attrId == 0)
            return null;
        Integer cached;
        synchronized (attrColorCache)
        {
            cached = attrColorCache.get(attrId);
        }
        if (cached != null)
            return cached == NOT_SURFACE ? null : cached;
        Integer color = null;
        try
        {
            if ("attr".equals(res.getResourceTypeName(attrId)))
            {
                String name = res.getResourceEntryName(attrId);
                if (BLACK_ATTRS.contains(name))
                    color = AMOLED_BLACK;
                else if (RAISED_ATTRS.contains(name))
                    color = AMOLED_RAISED;
                else if (SEPARATOR_ATTRS.contains(name))
                    color = AMOLED_SEPARATOR;
                else if (BAR_ATTRS.contains(name))
                    color = AMOLED_BAR;
            }
        }
        catch (Throwable ignored) { }
        synchronized (attrColorCache)
        {
            attrColorCache.put(attrId, color == null ? NOT_SURFACE : color);
        }
        return color;
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    @SuppressLint("DiscouragedPrivateApi")
    private static ColorStateList remapColorStateList(ColorStateList csl)
    {
        try
        {
            if (cslGetColors == null)
            {
                cslGetColors = ColorStateList.class.getDeclaredMethod("getColors");
                cslGetStates = ColorStateList.class.getDeclaredMethod("getStates");
                cslGetColors.setAccessible(true);
                cslGetStates.setAccessible(true);
            }
            int[] colors = ((int[])Objects.requireNonNull(cslGetColors.invoke(csl))).clone();
            boolean changed = false;
            for (int i = 0; i < colors.length; i++)
            {
                Integer mapped = remap(colors[i]);
                if (mapped == null)
                    continue;
                colors[i] = mapped;
                changed = true;
            }
            return changed ? new ColorStateList((int[][])cslGetStates.invoke(csl), colors) : csl;
        }
        catch (Throwable ignored) { }
        return csl;
    }

    @Override
    public void installHooks() throws Throwable
    {
        Class<?> themeController = DexKitCache.GetClass("AmoledTheme_ThemeController", () -> bridge.findClass(FindClass.create()
            .matcher(ClassMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.ABSTRACT)
                .addUsingString("SAVE_SETTING_THEME_SELECTED_POSITION", StringMatchType.Equals)
                .addMethod(MethodMatcher.create()
                    .modifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)
                    .returnType("void")
                    .paramCount(4)
                    .paramTypes(int.class, int.class, ArrayList.class, boolean.class)
                    .addUsingField("Landroid/os/Build$VERSION;->SDK_INT:I")
                    .addUsingField("Landroid/content/res/Configuration;->uiMode:I")
                    .addInvoke("Lcom/zing/zalocore/CoreUtility;->getAppContext()Landroid/content/Context;")
                    .addInvoke("Landroid/content/Context;->getResources()Landroid/content/res/Resources;")
                )
            )), classLoader);
        if (themeController == null)
            return;
        darkModeMethod = DexKitCache.GetMethod("AmoledTheme_IsDark", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(themeController)
                .modifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)
                .returnType("int")
                .paramCount(0)
            )), classLoader);
        Method themedColor = DexKitCache.GetMethod("AmoledTheme_ThemedColor", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(themeController)
                .modifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)
                .returnType("int")
                .paramCount(2)
                .paramTypes(Context.class, int.class)
            )), classLoader);
        if (darkModeMethod == null || themedColor == null)
            return;
        doHook(themedColor, chain ->
        {
            if (!isDark())
                return chain.proceed();
            Resources res = ((Context)chain.getArg(0)).getResources();
            int attrId = (int)chain.getArg(1);
            if (keepOriginal(safeName(res, attrId)))
                return chain.proceed();
            Integer byName = amoledColor(res, attrId);
            if (byName != null)
                return byName;
            Object result = chain.proceed();
            if (result instanceof Integer)
            {
                Integer mapped = remap((Integer)result);
                if (mapped != null)
                    return mapped;
            }
            return result;
        });
        doHook(TypedArray.class.getMethod("getColor", int.class, int.class), chain ->
        {
            if (!isDark())
                return chain.proceed();
            TypedArray ta = (TypedArray)chain.getThisObject();
            int index = (int)chain.getArg(0);
            if (keepOriginal(ta, index))
                return chain.proceed();
            Integer byName = namedColor(ta, index);
            if (byName != null)
                return byName;
            Object result = chain.proceed();
            if (result instanceof Integer)
            {
                Integer mapped = remap((Integer)result);
                if (mapped != null)
                    return mapped;
            }
            return result;
        });
        doHook(TypedArray.class.getMethod("getColorStateList", int.class), chain ->
        {
            Object result = chain.proceed();
            if (!isDark() || !(result instanceof ColorStateList colorStateList))
                return result;
            return remapColorStateList(colorStateList);
        });
        doHook(View.class.getMethod("setBackgroundColor", int.class), chain ->
        {
            if (!isDark())
                return chain.proceed();
            String viewName = chain.getThisObject().getClass().getName();
            if (isNavBarView(viewName))
                return chain.proceed(new Object[]{ AMOLED_BLACK });
            Integer mapped = remap((int)chain.getArg(0));
            if (mapped == null)
                return chain.proceed();
            return chain.proceed(new Object[]{ mapped });
        });
        doHook(View.class.getMethod("setBackground", Drawable.class), chain ->
        {
            Object result = chain.proceed();
            if (!isDark())
                return result;
            View view = (View)chain.getThisObject();
            String viewName = view.getClass().getName();
            Drawable drawable = (Drawable)chain.getArg(0);
            if (isNavBarView(viewName))
                view.setBackgroundColor(AMOLED_BLACK);
            else if (isContentBarView(viewName))
                view.setBackgroundColor(AMOLED_BAR);
            else if (drawable instanceof ColorDrawable
                && ((ColorDrawable)drawable).getColor() == 0xB8000000)
                view.setBackgroundColor(AMOLED_BAR);
            else if (viewName.endsWith("RobotoTextView")
                && (drawable instanceof StateListDrawable || drawable instanceof GradientDrawable))
                view.setBackgroundColor(AMOLED_BAR);
            return result;
        });
        doHook(View.class.getMethod("setBackgroundResource", int.class), chain ->
        {
            if (!isDark())
                return chain.proceed();
            View view = (View)chain.getThisObject();
            String viewName = view.getClass().getName();
            int id = (int)chain.getArg(0);
            String resName = id == 0 ? "0" : safeName(view.getResources(), id);
            Object result = chain.proceed();
            if (isNavBarView(viewName) || resName.contains("nav_bar") || resName.contains("bottom_navigation"))
                view.setBackgroundColor(AMOLED_BLACK);
            return result;
        });
        doHook(Resources.Theme.class.getMethod("resolveAttribute", int.class, TypedValue.class, boolean.class), chain ->
        {
            if (!isDark())
                return chain.proceed();
            Resources.Theme theme = (Resources.Theme)chain.getThisObject();
            Integer color = amoledColor(theme.getResources(), (int)chain.getArg(0));
            if (color == null)
                return chain.proceed();
            TypedValue out = (TypedValue)chain.getArg(1);
            out.type = TypedValue.TYPE_INT_COLOR_ARGB8;
            out.data = color;
            return true;
        });
        RemapGetColorNameBasedHooker getColorHooker = new RemapGetColorNameBasedHooker();
        doHook(Resources.class.getMethod("getColor", int.class), getColorHooker);
        doHook(Resources.class.getMethod("getColor", int.class, Resources.Theme.class), getColorHooker);
        doHook(Resources.Theme.class.getMethod("obtainStyledAttributes", int[].class), new ObtainStyledAttributesHooker(0));
        doHook(Resources.Theme.class.getMethod("obtainStyledAttributes", int.class, int[].class), new ObtainStyledAttributesHooker(1));
        RemapGetColorHooker hooker = new RemapGetColorHooker();
        doHook(GradientDrawable.class.getMethod("setColor", int.class), hooker);
        doHook(ColorDrawable.class.getMethod("setColor", int.class), hooker);
        doHook(ColorDrawable.class.getConstructor(int.class), hooker);
    }

    private static class ObtainStyledAttributesHooker implements XposedInterface.Hooker
    {
        private final int argIndex;

        private ObtainStyledAttributesHooker(int argIndex)
        {
            this.argIndex = argIndex;
        }

        @Override
        public Object intercept(XposedInterface.Chain chain) throws Throwable
        {
            TypedArray ta = (TypedArray)chain.proceed();
            if (ta != null)
            {
                synchronized (styledAttrs)
                {
                    styledAttrs.put(ta, (int[])chain.getArg(argIndex));
                }
            }
            return ta;
        }
    }

    private static class RemapGetColorNameBasedHooker implements XposedInterface.Hooker
    {
        @Override
        public Object intercept(XposedInterface.Chain chain) throws Throwable
        {
            if (!isDark())
                return chain.proceed();
            try
            {
                Resources res = (Resources)chain.getThisObject();
                String name = res.getResourceEntryName((int)chain.getArg(0));
                if (keepOriginal(name))
                    return chain.proceed();
                if ("Dark_hightlight_setting".equals(name))
                    return AMOLED_BAR;
                if (DARK_SEPARATOR_COLORS.contains(name))
                    return AMOLED_SEPARATOR;
                if (DARK_SURFACE_COLORS.contains(name))
                    return AMOLED_BLACK;
            }
            catch (Throwable ignored) { }
            return chain.proceed();
        }
    }

    private static class RemapGetColorHooker implements XposedInterface.Hooker
    {
        @Override
        public Object intercept(XposedInterface.Chain chain) throws Throwable
        {
            if (!isDark())
                return chain.proceed();
            Integer mapped = remap((int)chain.getArg(0));
            if (mapped == null)
                return chain.proceed();
            return chain.proceed(new Object[]{ mapped });
        }
    }
}