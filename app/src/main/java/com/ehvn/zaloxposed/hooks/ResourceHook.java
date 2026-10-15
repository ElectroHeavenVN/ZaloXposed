package com.ehvn.zaloxposed.hooks;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.ehvn.zaloxposed.utilities.Logger;
import com.ehvn.zaloxposed.utilities.Utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import io.github.libxposed.api.XposedInterface;

public class ResourceHook extends BaseHook
{
    private static final Map<Integer, String> sDrawableMap = new HashMap<>();
    private static final Map<Integer, Integer> sResolvedIds = new HashMap<>();
    private static final Map<Integer, Integer> sFallbackIds = new HashMap<>();
    private static final Map<Integer, String> sFallbackNames = new HashMap<>();
    private static Resources sModResources = null;
    private static AssetManager sAssetManager = null;
    private static boolean sInstalled = false;

    public static void map(int customId, String moduleDrawableName, int fallbackId)
    {
        synchronized (sDrawableMap)
        {
            sDrawableMap.put(customId, moduleDrawableName);
        }
        if (fallbackId != 0)
        {
            synchronized (sFallbackIds)
            {
                sFallbackIds.put(customId, fallbackId);
            }
        }
    }

    public static void map(int customId, String moduleDrawableName, String fallbackName)
    {
        synchronized (sDrawableMap)
        {
            sDrawableMap.put(customId, moduleDrawableName);
        }
        if (fallbackName != null && !fallbackName.isEmpty())
        {
            synchronized (sFallbackNames)
            {
                sFallbackNames.put(customId, fallbackName);
            }
        }
    }

    public static void replace(int targetResId, String moduleDrawableName)
    {
        synchronized (sDrawableMap)
        {
            sDrawableMap.put(targetResId, moduleDrawableName);
        }
    }

    public static void replace(String targetDrawableName, String moduleDrawableName)
    {
        if (targetDrawableName == null || targetDrawableName.isEmpty())
            return;
        int targetId = Utils.GetDrawableResourceIdByName(targetDrawableName);
        if (targetId != 0)
        {
            replace(targetId, moduleDrawableName);
        }
        else
        {
            Logger.e("Cannot resolve target drawable ID for name: " + targetDrawableName);
        }
    }

    public static void unmap(int customId)
    {
        synchronized (sDrawableMap)
        {
            sDrawableMap.remove(customId);
        }
        synchronized (sResolvedIds)
        {
            sResolvedIds.remove(customId);
        }
        synchronized (sFallbackIds)
        {
            sFallbackIds.remove(customId);
        }
        synchronized (sFallbackNames)
        {
            sFallbackNames.remove(customId);
        }
    }

    private static boolean isMapped(int resId)
    {
        synchronized (sDrawableMap)
        {
            return sDrawableMap.containsKey(resId);
        }
    }

    @Override
    public void hook() throws Throwable
    {
        if (sInstalled)
            return;
        sInstalled = true;
        sAssetManager = assetManager;
        hookImageView();
        hookResources();
        hookResourcesImpl();
        hookAssetManager();
    }

    private void hookImageView() throws Throwable
    {
        Method method = ImageView.class.getDeclaredMethod("setImageResource", int.class);
        Logger.i("Hooking: " + method);
        module.hook(method).setPriority(5).intercept(chain ->
        {
            int resId = (int) chain.getArg(0);
            if (!isMapped(resId))
                return chain.proceed();
            try
            {
                ImageView iv = (ImageView) chain.getThisObject();
                Drawable d = getMappedDrawable(iv.getContext().getResources(), iv.getContext().getTheme(), resId);
                if (d != null)
                {
                    iv.setImageDrawable(d);
                    return null;
                }
            }
            catch (Throwable t)
            {
                Logger.e(t);
            }
            return redirectImageViewToFallback(chain, resId);
        });
    }

    private void hookResources()
    {
        for (Method method : Resources.class.getDeclaredMethods())
        {
            String name = method.getName();
            boolean isDrawable = name.equals("getDrawable") || name.equals("getDrawableForDensity");
            boolean isValue = name.equals("getValue") || name.equals("getValueForDensity");
            if (!isDrawable && !isValue)
                continue;
            Class<?>[] params = method.getParameterTypes();
            if (params.length == 0 || params[0] != int.class)
                continue;
            Logger.i("Hooking: " + method);
            module.hook(method).setPriority(5).intercept(chain ->
            {
                int resId = (int) chain.getArg(0);
                if (!isMapped(resId))
                    return chain.proceed();
                if (!isDrawable)
                    return redirectGetValue(chain, resId);
                try
                {
                    Resources zaloRes = (Resources) chain.getThisObject();
                    Resources.Theme theme = findTheme(chain);
                    Drawable d = getMappedDrawable(zaloRes, theme, resId);
                    if (d != null)
                        return d;
                }
                catch (Throwable t)
                {
                    Logger.e(t);
                }
                return redirectToFallbackDrawable(chain, resId);
            });
        }
    }

    @SuppressLint("PrivateApi")
    private void hookResourcesImpl() throws Throwable
    {
        Class<?> implCls = Class.forName("android.content.res.ResourcesImpl");
        for (Method method : implCls.getDeclaredMethods())
        {
            String n = method.getName();
            boolean isLoad = n.equals("loadDrawable");
            boolean isGetValue = n.equals("getValue");
            if (!isLoad && !isGetValue)
                continue;
            Class<?>[] ps = method.getParameterTypes();
            if (isGetValue && (ps.length != 3 || ps[0] != int.class))
                continue;
            Logger.i("Hooking: " + method);
            module.hook(method).setPriority(5).intercept(chain ->
            {
                Object[] arr = chain.getArgs().toArray();
                int idIndex = findCustomIdIndex(arr);
                if (idIndex < 0)
                    return chain.proceed();
                int resId = (Integer) arr[idIndex];
                if (!isLoad)
                    return redirectWithRewrittenId(chain, arr, 0, resId);
                try
                {
                    Resources wrapper = findResources(arr, chain);
                    Resources.Theme theme = findTheme(chain);
                    if (wrapper != null)
                    {
                        Drawable d = getMappedDrawable(wrapper, theme, resId);
                        if (d != null)
                            return d;
                    }
                }
                catch (Throwable t)
                {
                    Logger.e(t);
                }
                return redirectWithRewrittenId(chain, arr, idIndex, resId);

            });
        }
    }

    private void hookAssetManager()
    {
        for (Method method : AssetManager.class.getDeclaredMethods())
        {
            if (!method.getName().equals("getResourceValue"))
                continue;
            Class<?>[] ps = method.getParameterTypes();
            if (ps.length != 4 || ps[0] != int.class)
                continue;
            Logger.i("Hooking: " + method);
            module.hook(method).setPriority(5).intercept(chain ->
            {
                int resId = (int) chain.getArg(0);
                if (!isMapped(resId))
                    return chain.proceed();
                Object[] arr = chain.getArgs().toArray();
                return redirectWithRewrittenId(chain, arr, 0, resId);
            });
        }
    }

    private static int findCustomIdIndex(Object[] args)
    {
        for (int i = 0; i < args.length; i++)
        {
            if (args[i] instanceof Integer && isMapped((Integer) args[i]))
                return i;
        }
        return -1;
    }

    private static Resources.Theme findTheme(XposedInterface.Chain chain)
    {
        for (Object arg : chain.getArgs().toArray())
        {
            if (arg instanceof Resources.Theme)
                return (Resources.Theme) arg;
        }
        return null;
    }

    private static Resources findResources(Object[] args, XposedInterface.Chain chain)
    {
        for (Object arg : args)
        {
            if (arg instanceof Resources)
                return (Resources) arg;
        }
        Object a0 = chain.getArg(0);
        if (a0 instanceof Resources)
            return (Resources) a0;
        return null;
    }

    private static Object redirectWithRewrittenId(XposedInterface.Chain chain, Object[] args, int idIndex, int customId) throws Throwable
    {
        try
        {
            int fallbackId = getFallbackId(customId);
            if (fallbackId == 0)
                return chain.proceed();
            Object[] fixed = args.clone();
            fixed[idIndex] = fallbackId;
            return chain.proceed(fixed);
        }
        catch (Throwable t)
        {
            Logger.e(t);
        }
        return chain.proceed();
    }

    private static Object redirectGetValue(XposedInterface.Chain chain, int customId) throws Throwable
    {
        Object[] arr = chain.getArgs().toArray();
        return redirectWithRewrittenId(chain, arr, 0, customId);
    }

    private static Object redirectToFallbackDrawable(XposedInterface.Chain chain, int customId) throws Throwable
    {
        try
        {
            int fallbackId = getFallbackId(customId);
            if (fallbackId == 0)
                return chain.proceed();
            Resources zaloRes = (Resources) chain.getThisObject();
            Resources.Theme theme = findTheme(chain);
            return zaloRes.getDrawable(fallbackId, theme);
        }
        catch (Throwable t)
        {
            Logger.e(t);
        }
        return chain.proceed();
    }

    private static Object redirectImageViewToFallback(XposedInterface.Chain chain, int resId) throws Throwable
    {
        try
        {
            int fallbackId = getFallbackId(resId);
            if (fallbackId != 0)
            {
                ImageView iv = (ImageView) chain.getThisObject();
                iv.setImageResource(fallbackId);
                return null;
            }
        }
        catch (Throwable t)
        {
            Logger.e(t);
        }
        return chain.proceed();
    }

    private static int getFallbackId(int customId)
    {
        synchronized (sFallbackIds)
        {
            Integer cached = sFallbackIds.get(customId);
            if (cached != null && cached != 0)
                return cached;
        }
        String name;
        synchronized (sFallbackNames)
        {
            name = sFallbackNames.get(customId);
        }
        if (name != null && !name.isEmpty())
        {
            try
            {
                int id = Utils.GetDrawableResourceIdByName(name);
                if (id != 0)
                {
                    synchronized (sFallbackIds)
                    {
                        sFallbackIds.put(customId, id);
                    }
                    return id;
                }
            }
            catch (Throwable t)
            {
                Logger.e(t);
            }
        }
        return 0;
    }

    @SuppressWarnings("deprecation")
    private static synchronized Resources getModResources(Resources zaloRes)
    {
        if (sModResources == null)
            sModResources = new Resources(sAssetManager, zaloRes.getDisplayMetrics(), zaloRes.getConfiguration());
        return sModResources;
    }

    @SuppressLint("DiscouragedApi")
    private static Drawable getMappedDrawable(Resources zaloRes, Resources.Theme theme, int customId)
    {
        String name;
        synchronized (sDrawableMap)
        {
            name = sDrawableMap.get(customId);
        }
        if (name == null || name.isEmpty())
            return null;
        try
        {
            Resources modRes = getModResources(zaloRes);
            int modId;
            synchronized (sResolvedIds)
            {
                Integer cached = sResolvedIds.get(customId);
                if (cached == null)
                {
                    cached = modRes.getIdentifier(name, "drawable", "com.ehvn.zaloxposed");
                    sResolvedIds.put(customId, cached);
                }
                modId = cached;
            }
            if (modId == 0)
                return null;
            return modRes.getDrawable(modId, theme);
        }
        catch (Throwable t)
        {
            Logger.e(t);
            return null;
        }
    }
}
