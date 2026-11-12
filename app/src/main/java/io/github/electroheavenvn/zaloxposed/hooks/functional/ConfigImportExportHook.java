package io.github.electroheavenvn.zaloxposed.hooks.functional;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Method;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;
import io.github.electroheavenvn.zaloxposed.utilities.S;
import io.github.electroheavenvn.zaloxposed.utilities.Utils;

public class ConfigImportExportHook extends BaseHook
{
    private static final int REQUEST_CODE_IMPORT_CONFIG = 3636;

    @Override
    public void installHooks() throws Throwable
    {
        Method onActivityResult = Activity.class.getDeclaredMethod("onActivityResult", int.class, int.class, Intent.class);
        doHook(onActivityResult, 10, chain ->
        {
            int requestCode = (int)chain.getArg(0);
            if (requestCode != REQUEST_CODE_IMPORT_CONFIG)
                return chain.proceed();
            int resultCode = (int)chain.getArg(1);
            Intent data = (Intent)chain.getArg(2);
            Activity activity = (Activity)chain.getThisObject();
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null)
                handleImportUri(activity, data.getData());
            return null;
        });
    }

    public static void ExportToDownloadFolder(Context context)
    {
        try
        {
            String jsonString = Config.getConfigJsonString();
            boolean success = false;
            String exportedPath = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                ContentResolver resolver = context.getContentResolver();
                Uri collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                String fileName = "zaloxposed_config.json";
                String relativePath = android.os.Environment.DIRECTORY_DOWNLOADS + "/ZaloXposed/";
                try
                {
                    String selection = MediaStore.MediaColumns.DISPLAY_NAME + " = ?";
                    String[] selectionArgs = new String[]{fileName};
                    resolver.delete(collection, selection, selectionArgs);
                }
                catch (Exception ignored) { }
                android.content.ContentValues contentValues = new android.content.ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/json");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);
                Uri uri = resolver.insert(collection, contentValues);
                if (uri != null)
                {
                    try (java.io.OutputStream os = resolver.openOutputStream(uri, "rwt"))
                    {
                        if (os != null)
                        {
                            os.write(jsonString.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                            os.flush();
                            success = true;
                            exportedPath = "Download/ZaloXposed/" + fileName;
                        }
                    }
                }
            }
            if (!success)
            {
                File downloadDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
                File exportDir = new File(downloadDir, "ZaloXposed");
                if (!exportDir.exists())
                    exportDir.mkdirs();
                File targetFile = new File(exportDir, "zaloxposed_config.json");
                success = Config.exportToFile(targetFile);
                if (success)
                    exportedPath = targetFile.getAbsolutePath();
            }
            boolean finalSuccess = success;
            String finalPath = exportedPath;
            new Handler(Looper.getMainLooper()).post(() ->
            {
                if (finalSuccess)
                    Toast.makeText(context, S.tr("exportSuccess") + finalPath, Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(context, S.tr("exportFailed"), Toast.LENGTH_SHORT).show();
            });
        }
        catch (Exception e) { Logger.e(e); }
    }

    public static void ImportUsingFilePicker(Context context)
    {
        try
        {
            Activity activity = null;
            Context ctx = context;
            while (ctx instanceof ContextWrapper)
            {
                if (ctx instanceof Activity)
                {
                    activity = (Activity)ctx;
                    break;
                }
                ctx = ((ContextWrapper)ctx).getBaseContext();
            }
            if (activity == null)
            {
                new Handler(Looper.getMainLooper()).post(() ->
                {
                    Toast.makeText(context, S.tr("openFilePickerFailed"), Toast.LENGTH_SHORT).show();
                });
                return;
            }
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            String[] extraMimeTypes = new String[]{"application/json", "text/*"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, extraMimeTypes);
            activity.startActivityForResult(intent, REQUEST_CODE_IMPORT_CONFIG);
        }
        catch (Exception e)
        {
            Logger.e(e);
            try
            {
                // Fallback to direct ACTION_GET_CONTENT
                if (context instanceof Activity)
                {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    ((Activity)context).startActivityForResult(intent, REQUEST_CODE_IMPORT_CONFIG);
                }
            }
            catch (Exception ex) { Logger.e(ex); }
        }
    }

    private static void handleImportUri(Context context, Uri uri)
    {
        try
        {
            boolean success = false;
            try (java.io.InputStream inputStream = context.getContentResolver().openInputStream(uri))
            {
                if (inputStream != null)
                    success = Config.importFromStream(inputStream);
            }
            boolean finalSuccess = success;
            new Handler(Looper.getMainLooper()).post(() ->
            {
                if (finalSuccess)
                    Toast.makeText(context, S.tr("importSuccess"), Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(context, S.tr("importFailed"), Toast.LENGTH_SHORT).show();
            });
        }
        catch (Exception e)
        {
            Logger.e(e);
            new Handler(Looper.getMainLooper()).post(() ->
            {
                Toast.makeText(context, S.tr("importErrorFormatted", e.getMessage()), Toast.LENGTH_SHORT).show();
            });
        }
    }
}