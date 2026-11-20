package io.github.electroheavenvn.zaloxposed.hooks.functional;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.widget.Toast;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.ChecksumFileHelper;
import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;
import io.github.electroheavenvn.zaloxposed.utilities.S;
import io.github.electroheavenvn.zaloxposed.utilities.Utils;

public class CustomCallRingtoneHook extends BaseHook
{
    private static final int REQUEST_CODE_PICK_RINGTONE = 1109;
    private static final String[] EXT = {"mp3", "wav", "ogg", "m4a", "aac", "flac"};
    private static final Random random = new Random();

    @Override
    public void installHooks() throws Throwable
    {
        Method onActivityResult = Activity.class.getDeclaredMethod("onActivityResult", int.class, int.class, Intent.class);
        doHook(onActivityResult, 10, chain ->
        {
            int requestCode = (int)chain.getArg(0);
            if (requestCode != REQUEST_CODE_PICK_RINGTONE)
                return chain.proceed();
            int resultCode = (int)chain.getArg(1);
            Intent data = (Intent)chain.getArg(2);
            Activity activity = (Activity)chain.getThisObject();
            if (resultCode == Activity.RESULT_OK && data != null)
                handlePickedFiles(activity, data);
            return null;
        });
        Method ring = DexKitCache.GetMethod("CustomRingtone_Ring", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC)
                .returnType("void")
                .paramCount(1)
                .paramTypes("java.lang.String")
                .addUsingString("audio", StringMatchType.Equals)
                .addUsingString("vibrate_when_ringing", StringMatchType.Equals)
                .addInvoke("Landroid/media/AudioManager;->getRingerMode()I")
                .addInvoke("Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;")
                .addUsingField("Landroid/os/Build$VERSION;->SDK_INT:I")
                .addUsingNumber(33)
            )), classLoader);
        if (ring == null)
            return;
        doHook(ring, chain ->
        {
            if (!Config.getEnableCustomRingtone())
                return chain.proceed();
            File ringtone = randomRingtone();
            if (ringtone == null)
                return chain.proceed();
            return chain.proceed(new Object[] { Uri.fromFile(ringtone).toString() });
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static File getCustomRingtoneDir()
    {
        String base = Utils.GetZaloXposedDir();
        if (base.isEmpty())
            return null;
        File dir = new File(base, "ringtone");
        if (!dir.exists())
            dir.mkdirs();
        return dir.isDirectory() ? dir : null;
    }

    private static File randomRingtone()
    {
        File dir = getCustomRingtoneDir();
        if (dir == null)
            return null;
        File[] files = dir.listFiles((d, name) -> isAudioFile(name));
        if (files == null || files.length == 0)
            return null;
        List<File> candidates = new ArrayList<>();
        Collections.addAll(candidates, files);
        Collections.shuffle(candidates, random);
        return candidates.get(0);
    }

    public static void PickFiles(Context context)
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
            intent.setType("audio/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivityForResult(intent, REQUEST_CODE_PICK_RINGTONE);
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
                    intent.setType("audio/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    activity.startActivityForResult(intent, REQUEST_CODE_PICK_RINGTONE);
                }
            }
            catch (Exception ex) { Logger.e(ex); }
        }
    }

    public static int getCustomFileCount()
    {
        File dir = getCustomRingtoneDir();
        File[] files = null;
        if (dir != null)
            files = dir.listFiles((d, name) -> isAudioFile(name));
        return files == null ? 0 : files.length;
    }

    public static void ClearFiles(Context context)
    {
        int deleted = 0;
        try
        {
            File dir = getCustomRingtoneDir();
            File[] files = dir == null ? null : dir.listFiles();
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
        toast(context, S.tr("ringtoneFilesCleared", deleted));
    }

    private static void handlePickedFiles(Context context, Intent data)
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
        ContentResolver resolver = context.getContentResolver();
        int added = 0;
        int skipped = 0;
        File dir = getCustomRingtoneDir();
        if (dir != null)
        {
            for (Uri uri : uris)
            {
                String ext = getExtension(resolver, uri);
                ChecksumFileHelper.Result result = ChecksumFileHelper.CopyWithChecksumChecks(resolver, uri, dir, ext);
                if (result == null)
                    continue;
                if (result.duplicate())
                    skipped++;
                else
                    added++;
            }
        }
        toast(context, uris.isEmpty() ? S.tr("ringtoneFilesFailed") : S.tr("ringtoneFilesAdded", added, skipped));
    }

    private static String getExtension(ContentResolver resolver, Uri uri)
    {
        String name = null;
        try
        {
            android.database.Cursor cursor = resolver.query(uri, new String[] { OpenableColumns.DISPLAY_NAME }, null, null, null);
            if (cursor != null)
            {
                try (cursor)
                {
                    if (cursor.moveToFirst())
                        name = cursor.getString(0);
                }
            }
        }
        catch (Exception e) { Logger.e(e); }
        if (name != null)
        {
            int dot = name.lastIndexOf('.');
            if (dot >= 0 && dot < name.length() - 1)
            {
                String ext = name.substring(dot + 1).toLowerCase(Locale.ROOT);
                for (String allowed : EXT)
                    if (allowed.equals(ext))
                        return ext;
            }
        }
        String mime = resolver.getType(uri);
        if (mime != null)
        {
            mime = mime.toLowerCase(Locale.ROOT);
            if (mime.contains("mpeg") || mime.contains("mp3"))
                return "mp3";
            if (mime.contains("wav"))
                return "wav";
            if (mime.contains("ogg"))
                return "ogg";
            if (mime.contains("mp4") || mime.contains("m4a"))
                return "m4a";
            if (mime.contains("aac"))
                return "aac";
            if (mime.contains("flac"))
                return "flac";
        }
        return "mp3";
    }


    private static boolean isAudioFile(String name)
    {
        if (name == null)
            return false;
        String lower = name.toLowerCase(Locale.ROOT);
        for (String ext : EXT)
            if (lower.endsWith("." + ext))
                return true;
        return false;
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
}