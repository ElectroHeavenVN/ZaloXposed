package io.github.electroheavenvn.zaloxposed.hooks.custommenu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import io.github.electroheavenvn.zaloxposed.settingsmenu.ListSelectionSettingFeature;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;
import io.github.electroheavenvn.zaloxposed.utilities.ResourceHelper;
import io.github.electroheavenvn.zaloxposed.utilities.Utils;

public final class BottomSheetListSelectionHelper
{
    private static final int DISMISS_ANIM_MS = 250;
    private static Class<?> sheetClass;
    private static Method setMaxTranslationY;
    private static Method setMinTranslationY;
    private static Method setMinimizedWithAnimation;
    private static Method setEnableScrollY;
    private static Method setEnableToggleWhenClickNoConsume;
    @SuppressLint("StaticFieldLeak")
    private static ViewGroup container;
    @SuppressLint("StaticFieldLeak")
    private static FrameLayout activeOverlay;

    private BottomSheetListSelectionHelper() { }

    public static void Init(ClassLoader loader) throws Exception
    {
        sheetClass = Class.forName("com.zing.zalo.ui.widget.BottomSheetLayoutNestedParent", false, loader);
        Class<?> base = Class.forName("com.zing.zalo.ui.widget.BottomSheetLayout", false, loader);
        setMaxTranslationY = base.getMethod("setMaxTranslationY", float.class);
        setMinTranslationY = base.getMethod("setMinTranslationY", float.class);
        setMinimizedWithAnimation = base.getMethod("setMinimizedWithAnimation", boolean.class);
        setEnableScrollY = base.getMethod("setEnableScrollY", boolean.class);
        setEnableToggleWhenClickNoConsume = base.getMethod("setEnableToggleWHenClickNoConsume", boolean.class);
    }

    public static void Show(Context context, ListSelectionSettingFeature feature, Runnable onPicked)
    {
        Activity activity = findActivity(context);
        if (activity == null)
        {
            Logger.e("No live Activity");
            return;
        }
        try
        {
            build(activity, feature, onPicked);
        }
        catch (Throwable t) { Logger.e(t); }
    }

    @SuppressLint("DiscouragedApi")
    private static void build(Activity activity, ListSelectionSettingFeature feature, Runnable onPicked) throws Exception
    {
        if (activeOverlay != null && activeOverlay.getParent() != null)
            return;
        if (sheetClass == null)
            Init(Utils.GetClassLoader());
        final int matchParent = FrameLayout.LayoutParams.MATCH_PARENT;
        final int wrapContent = LinearLayout.LayoutParams.WRAP_CONTENT;
        Constructor<?> constructor = sheetClass.getConstructor(Context.class);
        ViewGroup sheet = (ViewGroup)constructor.newInstance(activity);
        int layoutId = ResourceHelper.GetLayout("bottom_sheet_layout");
        if (layoutId != 0)
            LayoutInflater.from(activity).inflate(layoutId, sheet, true);
        int containerId = ResourceHelper.GetId("bottom_sheet_container");
        container = containerId != 0 ? sheet.findViewById(containerId) : null;
        if (container == null)
        {
            LinearLayout fallback = new LinearLayout(activity);
            fallback.setOrientation(LinearLayout.VERTICAL);
            sheet.addView(fallback, new FrameLayout.LayoutParams(matchParent, matchParent));
            container = fallback;
        }
        FrameLayout overlay = new FrameLayout(activity);
        View dim = new View(activity);
        dim.setBackgroundColor(0x99000000);
        overlay.addView(dim, new FrameLayout.LayoutParams(matchParent, matchParent));
        TextView title = new TextView(activity);
        title.setLayoutParams(new LinearLayout.LayoutParams(matchParent, wrapContent));
        title.setText(feature.getTitle());
        title.setTextSize(16);
        title.setGravity(Gravity.CENTER);
        int pad16 = dp(activity, 16);
        title.setPadding(pad16, dp(activity, 12), pad16, dp(activity, 8));
        if (SettingViewsDefacerBase.textColor != null)
            title.setTextColor(SettingViewsDefacerBase.textColor);
        container.addView(title, container.getChildCount() > 0 ? 1 : 0);
        int selectedIndex = feature.getSelectedIndex();
        List<ListSelectionSettingFeature.Selection> selections = feature.getSelections();
        for (int i = 0; i < selections.size(); i++)
        {
            final int index = i;
            RelativeLayout row = ListItemSettingHelper.CreateNew(activity);
            ListItemSettingHelper.SetTitle(row, selections.get(i).getTitle());
            ListItemSettingHelper.SetShowChevronRight(row, false);
            ListItemSettingHelper.SetTick(row, i == selectedIndex);
            ListItemSettingHelper.SetOnClickListener(row, v ->
            {
                feature.select(index);
                if (onPicked != null)
                    onPicked.run();
                dismiss(sheet, overlay);
            });
            container.addView(row, new LinearLayout.LayoutParams(matchParent, wrapContent));
        }
        View bottomSpacer = new View(activity);
        bottomSpacer.setLayoutParams(new LinearLayout.LayoutParams(matchParent, androidNavBarHeight(activity) + dp(activity, 8)));
        container.addView(bottomSpacer);
        overlay.addView(sheet, new FrameLayout.LayoutParams(matchParent, matchParent));
        dim.setOnClickListener(v -> dismiss(sheet, overlay));
        overlay.setFocusable(true);
        overlay.setFocusableInTouchMode(true);
        overlay.setOnKeyListener((v, keyCode, event) ->
        {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
            {
                dismiss(sheet, overlay);
                return true;
            }
            return false;
        });
        ViewGroup decor = (ViewGroup)activity.getWindow().getDecorView();
        decor.addView(overlay, new FrameLayout.LayoutParams(matchParent, matchParent));
        activeOverlay = overlay;
        overlay.requestFocus();
        int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        setEnableScrollY.invoke(sheet, false);
        setEnableToggleWhenClickNoConsume.invoke(sheet, false);
        setMaxTranslationY.invoke(sheet, (float)screenHeight);
        setMinTranslationY.invoke(sheet, (float)screenHeight);
        sheet.setTranslationY(screenHeight);
        overlay.post(() ->
        {
            try
            {
                int contentHeight = 0;
                for (int i = 0; i < container.getChildCount(); i++)
                    contentHeight += container.getChildAt(i).getMeasuredHeight();
                if (contentHeight <= 0)
                    contentHeight = (int)(screenHeight * 0.6f);
                float shownTranslation = Math.max(0, screenHeight - contentHeight);
                setMinTranslationY.invoke(sheet, shownTranslation);
                setMinimizedWithAnimation.invoke(sheet, false);
            }
            catch (Throwable t) { Logger.e(t); }
        });
    }

    private static void dismiss(ViewGroup sheet, FrameLayout overlay)
    {
        try
        {
            setMinimizedWithAnimation.invoke(sheet, true);
            sheet.postDelayed(() ->
            {
                ViewGroup parent = (ViewGroup)overlay.getParent();
                if (parent != null)
                    parent.removeView(overlay);
                if (activeOverlay == overlay)
                    activeOverlay = null;
            }, DISMISS_ANIM_MS);
        }
        catch (Throwable t) { Logger.e(t); }
    }

    @SuppressWarnings("deprecation")
    private static int androidNavBarHeight(Activity activity)
    {
        WindowInsets insets = activity.getWindow().getDecorView().getRootWindowInsets();
        if (insets != null)
        {
            int bottom = insets.getSystemWindowInsetBottom();
            if (bottom > 0)
                return bottom;
        }
        // Resources res = activity.getResources();
        // int id = res.getIdentifier("navigation_bar_height", "dimen", "android");
        // return id > 0 ? res.getDimensionPixelSize(id) : 0;
        return 0;
    }

    private static int dp(Context context, int value)
    {
        return (int)(value * context.getResources().getDisplayMetrics().density);
    }

    private static Activity findActivity(Context context)
    {
        Context current = context;
        while (current instanceof ContextWrapper)
        {
            if (current instanceof Activity activity)
                return activity;
            Context base = ((ContextWrapper)current).getBaseContext();
            if (base == current)
                break;
            current = base;
        }
        return null;
    }
}