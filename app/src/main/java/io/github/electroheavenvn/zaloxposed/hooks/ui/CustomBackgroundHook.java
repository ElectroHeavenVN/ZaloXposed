package io.github.electroheavenvn.zaloxposed.hooks.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.ChecksumFileHelper;
import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;
import io.github.electroheavenvn.zaloxposed.utilities.S;
import io.github.electroheavenvn.zaloxposed.utilities.Utils;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CustomBackgroundHook extends BaseHook
{
    private static final String BG_TAG = "zalo_bg_msg_root";
    private static final int REQUEST_CODE_PICK_BACKGROUND = 6767;
    private static final int MAX_BG_SIZE = 1080;

    private static final Random random = new Random();

    @Override
    public void installHooks() throws Throwable
    {
        Method onActivityResult = Activity.class.getDeclaredMethod("onActivityResult", int.class, int.class, Intent.class);
        doHook(onActivityResult, 10, chain ->
        {
            int requestCode = (int)chain.getArg(0);
            if (requestCode != REQUEST_CODE_PICK_BACKGROUND)
                return chain.proceed();
            int resultCode = (int)chain.getArg(1);
            Intent data = (Intent)chain.getArg(2);
            Activity activity = (Activity)chain.getThisObject();
            if (resultCode == Activity.RESULT_OK && data != null)
                handlePickedImages(activity, data);
            return null;
        });
        Class<?> msgViewCls = Class.forName("com.zing.zalo.ui.maintab.MainTabView", false, classLoader);
        List<Method> methods = DexKitCache.GetMethods("CustomBackground_1", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(msgViewCls)
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("android.view.View")
                .paramCount(3)
                .paramTypes("android.view.LayoutInflater", "android.view.ViewGroup", "android.os.Bundle")
                .addUsingString("onCreateView", StringMatchType.Equals)
            )), classLoader);
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        for (Method method : methods)
        {
            doHook(method, chain ->
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static File getCustomBgDir()
    {
        String base = Utils.GetZaloXposedDir();
        if (base.isEmpty())
            return null;
        File dir = new File(base, "bg");
        if (!dir.exists())
            dir.mkdirs();
        return dir.isDirectory() ? dir : null;
    }

    public static void PickImages(Context context)
    {
        try
        {
            Activity activity = findActivity(context);
            if (activity == null)
            {
                toast(context, S.tr("openFilePickerFailed"));
                return;
            }
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivityForResult(intent, REQUEST_CODE_PICK_BACKGROUND);
        }
        catch (Exception e)
        {
            Logger.e(e);
            try
            {
                Activity activity = findActivity(context);
                if (activity != null)
                {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    activity.startActivityForResult(intent, REQUEST_CODE_PICK_BACKGROUND);
                }
            }
            catch (Exception ex) { Logger.e(ex); }
        }
    }

    public static int getCustomImageCount()
    {
        File dir = getCustomBgDir();
        File[] files = null;
        if (dir != null)
            files = dir.listFiles((d, name) -> isImageFile(name));
        if (files == null)
            return 0;
        return files.length;
    }

    private static boolean isImageFile(String name)
    {
        if (name == null)
            return false;
        String lower = name.toLowerCase();
        return lower.endsWith(".img") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp");
    }

    public static void ClearImages(Context context)
    {
        int deleted = 0;
        try
        {
            File dir = getCustomBgDir();
            File[] files = null;
            if (dir != null)
                files = dir.listFiles();
            if (files != null)
            {
                for (File file : files)
                {
                    if (file.isFile() && file.delete())
                        deleted++;
                }
            }
        }
        catch (Exception e) { Logger.e(e); }
        toast(context, S.tr("bgImagesCleared", deleted));
    }

    private static void handlePickedImages(Context context, Intent data)
    {
        List<Uri> uris = new ArrayList<>();
        try
        {
            android.content.ClipData clip = data.getClipData();
            if (clip != null)
            {
                for (int i = 0; i < clip.getItemCount(); i++)
                    uris.add(clip.getItemAt(i).getUri());
            }
            else if (data.getData() != null)
                uris.add(data.getData());
        }
        catch (Exception e) { Logger.e(e); }

        int added = 0;
        int skipped = 0;
        File dir = getCustomBgDir();
        if (dir != null)
        {
            ContentResolver resolver = context.getContentResolver();
            for (Uri uri : uris)
            {
                ChecksumFileHelper.Result result = ChecksumFileHelper.CopyWithChecksumChecks(resolver, uri, dir, "img");
                if (result == null)
                    continue;
                if (result.duplicate())
                    skipped++;
                else
                    added++;
            }
        }
        toast(context, uris.isEmpty() ? S.tr("bgImagesFailed") : S.tr("bgImagesAdded", added, skipped));
    }


    private static Activity findActivity(Context context)
    {
        Context ctx = context;
        while (ctx instanceof ContextWrapper contextWrapper)
        {
            if (ctx instanceof Activity activity)
                return activity;
            ctx = contextWrapper.getBaseContext();
        }
        return null;
    }

    private static void toast(Context context, String message)
    {
        try
        {
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
        }
        catch (Exception e) { Logger.e(e); }
    }

    private Bitmap loadBg()
    {
        File dir = getCustomBgDir();
        if (dir != null)
        {
            File[] files = dir.listFiles((d, name) -> isImageFile(name));
            if (files != null && files.length > 0)
            {
                List<File> candidates = new ArrayList<>();
                Collections.addAll(candidates, files);
                Collections.shuffle(candidates, random);
                for (File file : candidates)
                {
                    Bitmap bitmap = decodeBitmap(file);
                    if (bitmap != null)
                        return bitmap;
                }
            }
        }
        String[] bgImgs;
        try
        {
            bgImgs = assetManager.list("bg");
        }
        catch (Exception e)
        {
            Logger.e(e);
            bgImgs = null;
        }
        if (bgImgs == null || bgImgs.length == 0)
            return null;
        String bgImg = bgImgs[random.nextInt(bgImgs.length)];
        try (InputStream stream = assetManager.open("bg/" + bgImg))
        {
            return BitmapFactory.decodeStream(stream);
        }
        catch (Exception e) { Logger.e(e); }
        return null;
    }

    private static Bitmap decodeBitmap(File file)
    {
        try
        {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);
            int width = bounds.outWidth;
            int height = bounds.outHeight;
            if (width <= 0 || height <= 0)
                return null;
            int sampleSize = 1;
            while (Math.max(width, height) / sampleSize > MAX_BG_SIZE)
                sampleSize *= 2;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        }
        catch (Exception e) { Logger.e(e); }
        return null;
    }
}