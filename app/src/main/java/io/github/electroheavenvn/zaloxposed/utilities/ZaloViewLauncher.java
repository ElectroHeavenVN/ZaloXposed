package io.github.electroheavenvn.zaloxposed.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class ZaloViewLauncher
{
    private static final ZaloViewLauncher INSTANCE = new ZaloViewLauncher();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private volatile WeakReference<Activity> currentActivity = new WeakReference<>(null);
    private volatile Method openViewMethod;
    private volatile ClassLoader appClassLoader;

    public static ZaloViewLauncher get() { return INSTANCE; }

    private ZaloViewLauncher() { }

    public void Init(ClassLoader classLoader, Method openViewMethod)
    {
        this.appClassLoader = classLoader;
        this.openViewMethod = openViewMethod;
    }

    public void Attach(Activity activity)
    {
        if (activity != null)
            this.currentActivity = new WeakReference<>(activity);
    }

    public void Detach(Activity activity)
    {
        Activity current = this.currentActivity.get();
        if (current == activity)
            this.currentActivity = new WeakReference<>(null);
    }

    public static void Open(Class<?> viewClass, Bundle args)
    {
        get().openInternal(viewClass, args, 1, null);
    }

    public static void Open(Class<?> viewClass, Bundle args, int animation)
    {
        get().openInternal(viewClass, args, animation, null);
    }

    public static void Open(View anchor, Class<?> viewClass, Bundle args)
    {
        get().openInternal(viewClass, args, 1, findActivity(anchor));
    }

    public static void Open(Activity host, Class<?> viewClass, Bundle args)
    {
        get().openInternal(viewClass, args, 1, host);
    }

    private void openInternal(Class<?> viewClass, Bundle args, int animation, Activity explicitHost)
    {
        Runnable task = () ->
        {
            Activity host = explicitHost != null ? explicitHost : resolveHost();
            if (host == null)
            {
                Logger.e("No live ZaloActivity");
                return;
            }
            if (host.isFinishing() || host.isDestroyed())
            {
                Logger.e("Activity is finishing/destroyed");
                return;
            }
            if (openViewMethod == null)
            {
                Logger.e("openView null");
                return;
            }
            if (!openViewMethod.getDeclaringClass().isInstance(host))
            {
                Logger.e("host is not a ZaloActivity");
                return;
            }
            try
            {
                openViewMethod.invoke(host, viewClass, args, animation);
            }
            catch (InvocationTargetException e) { Logger.e(e.getCause() != null ? e.getCause() : e); }
            catch (Throwable t) { Logger.e(t); }
        };
        if (Looper.myLooper() == Looper.getMainLooper())
            task.run();
        else
            mainHandler.post(task);
    }

    private Activity resolveHost()
    {
        Activity activity = currentActivity.get();
        if (isUsable(activity))
            return activity;
        return findLauncherActivity();
    }

    private boolean isUsable(Activity activity)
    {
        if (activity == null || activity.isFinishing() || activity.isDestroyed())
            return false;
        Method m = openViewMethod;
        return m == null || m.getDeclaringClass().isInstance(activity);
    }

    /**
     * Find WeakReference to ZaloLauncherActivity inside its static fields
     */
    private Activity findLauncherActivity()
    {
        if (appClassLoader == null)
            return null;
        try
        {
            Class<?> launcher = Class.forName("com.zing.zalo.ui.ZaloLauncherActivity", false, appClassLoader);
            for (Field field : launcher.getDeclaredFields())
            {
                if (!Modifier.isStatic(field.getModifiers()))
                    continue;
                if (!WeakReference.class.isAssignableFrom(field.getType()))
                    continue;
                field.setAccessible(true);
                Object value = field.get(null);
                if (!(value instanceof WeakReference))
                    continue;
                Object activity = ((WeakReference<?>)value).get();
                if (activity instanceof Activity && isUsable((Activity)activity))
                    return (Activity)activity;
            }
        }
        catch (Throwable ignored) { }
        return null;
    }

    private static Activity findActivity(View view)
    {
        return view == null ? null : findActivity(view.getContext());
    }

    private static Activity findActivity(Context context)
    {
        Context current = context;
        while (current instanceof ContextWrapper)
        {
            if (current instanceof Activity)
                return (Activity)current;
            Context base = ((ContextWrapper)current).getBaseContext();
            if (base == current)
                break;
            current = base;
        }
        return current instanceof Activity ? (Activity)current : null;
    }
}
