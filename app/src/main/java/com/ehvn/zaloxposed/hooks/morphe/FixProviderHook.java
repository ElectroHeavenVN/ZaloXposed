package com.ehvn.zaloxposed.hooks.morphe;

import com.ehvn.zaloxposed.Common;
import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.*;

import org.json.*;
import org.luckypray.dexkit.query.*;
import org.luckypray.dexkit.query.enums.*;
import org.luckypray.dexkit.query.matchers.*;
import org.luckypray.dexkit.result.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;

import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;

import io.github.libxposed.api.*;

public class FixProviderHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        if (Common.getPackageName().equals("com.zing.zalo"))
            return;
        Class<?> clazz = Class.forName("com.zing.zalo.provider.InternalProvider", false, classLoader);
        Logger.i("Hooking <clinit>: " + clazz);
        Class<?> finalClazz = clazz;
        module.hookClassInitializer(clazz).setPriority(2).intercept(chain ->
        {
            Object result = chain.proceed();
            Field uriField = null;
            Field uriMatcherField = null;
            for (Field field : finalClazz.getDeclaredFields())
            {
                if (field.getType().getName().equals("android.net.Uri"))
                    uriField = field;
                else if (field.getType().getName().equals("android.content.UriMatcher"))
                    uriMatcherField = field;
            }
            if (uriField != null)
            {
                uriField.setAccessible(true);
                uriField.set(null, Uri.parse("content://" + Common.getPackageName() + ".provider.InternalProvider"));
            }
            if (uriMatcherField != null)
            {
                uriMatcherField.setAccessible(true);
                UriMatcher uriMatcher = new UriMatcher(-1);
                uriMatcher.addURI(Common.getPackageName() + ".provider.InternalProvider", "main-pid", 1);
                uriMatcher.addURI(Common.getPackageName() + ".provider.InternalProvider", "current-uid", 2);
                uriMatcherField.set(null, uriMatcher);
            }
            return result;
        });
        clazz = Class.forName("com.zing.zalo.db.PreferencesProvider", false, classLoader);
        Logger.i("Hooking <clinit>: " + clazz);
        Class<?> finalClazz1 = clazz;
        module.hookClassInitializer(clazz).setPriority(2).intercept(chain ->
        {
            Object result = chain.proceed();
            Field uriField1 = null;
            Field uriField2 = null;
            Field uriMatcherField = null;
            for (Field field : finalClazz1.getDeclaredFields())
            {
                if (field.getType().getName().equals("android.net.Uri"))
                {
                    Uri uri = (Uri) field.get(null);
                    if (uri != null && uri.toString().equals("content://com.zing.zalo.db.preferencesprovider"))
                        uriField1 = field;
                    else if (uri != null && uri.toString().equals("content://com.zing.zalo.db.preferencesprovider/key"))
                        uriField2 = field;
                }
                else if (field.getType().getName().equals("android.content.UriMatcher"))
                    uriMatcherField = field;
            }
            if (uriField1 != null)
            {
                uriField1.setAccessible(true);
                uriField1.set(null, Uri.parse("content://" + Common.getPackageName() + ".db.preferencesprovider"));
            }
            if (uriField2 != null)
            {
                uriField2.setAccessible(true);
                uriField2.set(null, Uri.parse("content://" + Common.getPackageName() + ".db.preferencesprovider/key"));
            }
            if (uriMatcherField != null)
            {
                uriMatcherField.setAccessible(true);
                UriMatcher uriMatcher = new UriMatcher(-1);
                uriMatcher.addURI(Common.getPackageName() + ".db.preferencesprovider", null, 0);
                uriMatcher.addURI(Common.getPackageName() + ".db.preferencesprovider", "key/*", 1);
                uriMatcherField.set(null, uriMatcher);
            }
            return result;
        });
    }
}