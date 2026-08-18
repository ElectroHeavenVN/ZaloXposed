package com.ehvn.zaloxposed.hooks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ehvn.zaloxposed.utilities.Config;
import com.ehvn.zaloxposed.utilities.Logger;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Random;

public class CustomBackgroundHook extends BaseHook
{
    private static final String BG_TAG = "zalo_bg_msg_root";

    private static final Random random = new Random();

    @Override
    public void hook() throws Throwable
    {
        Class<?> msgViewCls = Class.forName("com.zing.zalo.ui.maintab.MainTabView", false, classLoader);
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(msgViewCls)
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("android.view.View")
                .paramCount(3)
                .paramTypes("android.view.LayoutInflater", "android.view.ViewGroup", "android.os.Bundle")
                .addUsingString("onCreateView", StringMatchType.Equals)
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        for (MethodData method : methods)
        {
            Method m = method.getMethodInstance(classLoader);
            Logger.i("Hooking: " + m);
            module.hook(m).intercept(chain ->
            {
                Object result = chain.proceed();
                if (!Config.getEnableBackground())
                    return result;
                if (!(result instanceof FrameLayout root))
                {
                    Logger.e("Not a FrameLayout: " + result);
                    return result;
                }
                if (root.findViewWithTag(BG_TAG) != null)
                {
                    Logger.i("Background already added");
                    return result;
                }
                Bitmap bg = loadBg();
                if (bg == null)
                {
                    Logger.e("Failed to load background");
                    return result;
                }
                ImageView bgView = new ImageView(root.getContext());
                bgView.setTag(BG_TAG);
                bgView.setImageDrawable(new BitmapDrawable(root.getContext().getResources(), bg));
                bgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                bgView.setAlpha(0.15f);
                root.addView(bgView, 1, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                return result;
            });
        }
    }

    private Bitmap loadBg()
    {
        String[] bgImgs = new String[0];
        try
        {
            bgImgs = assetManager.list("bg");
        }
        catch (Exception e)
        {
            Logger.e("Error listing wallpapers");
            Logger.e(e);
        }
        if (bgImgs == null || bgImgs.length == 0)
        {
            Logger.e("No wallpapers found");
            return null;
        }
        String bgImg = bgImgs[random.nextInt(bgImgs.length)];
        try (InputStream stream = assetManager.open("bg/" + bgImg))
        {
            return BitmapFactory.decodeStream(stream);
        }
        catch (Exception e)
        {
            Logger.e("Error loading background");
            Logger.e(e);
        }
        return null;
    }
}