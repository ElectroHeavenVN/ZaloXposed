package io.github.electroheavenvn.zaloxposed.hooks.permanent;

import android.app.Activity;
import android.os.Bundle;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

import java.lang.reflect.Method;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.ZaloViewLauncher;

public class ZaloViewLauncherHook extends BaseHook
{
    @Override
    public void installHooks() throws Throwable
    {
        Class<?> zaloActivity = Class.forName("com.zing.zalo.activity.ZaloActivity", false, classLoader);
        Method openView = DexKitCache.GetMethod("ZaloViewLauncher_ZaloActivity_OpenView", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(zaloActivity)
                .returnType("void")
                .paramCount(3)
                .paramTypes(Class.class, Bundle.class, int.class)
            )), classLoader);
        if (openView == null)
            return;
        ZaloViewLauncher.get().Init(classLoader, openView);
        Method onCreate = zaloActivity.getDeclaredMethod("onCreate", Bundle.class);
        doHook(onCreate, 10, chain -> {
            Object result = chain.proceed();
            ZaloViewLauncher.get().Attach((Activity)chain.getThisObject());
            return result;
        });
        Method onResume = zaloActivity.getDeclaredMethod("onResume");
        doHook(onResume, 10, chain -> {
            Object result = chain.proceed();
            ZaloViewLauncher.get().Attach((Activity)chain.getThisObject());
            return result;
        });
        Method onDestroy = zaloActivity.getDeclaredMethod("onDestroy");
        doHook(onDestroy, 10, chain -> {
            ZaloViewLauncher.get().Detach((Activity)chain.getThisObject());
            return chain.proceed();
        });
    }
}