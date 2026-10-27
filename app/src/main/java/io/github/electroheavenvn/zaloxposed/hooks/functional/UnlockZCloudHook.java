package io.github.electroheavenvn.zaloxposed.hooks.functional;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.FieldMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;

public class UnlockZCloudHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        Method targetMethod = DexKitCache.GetMethod("UnlockZCloud_SMLZCloudSubscription_GetZCloudPlan", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("int")
                .paramCount(0)
                .addUsingString("ZALO_CLOUD_SUBSCRIPTION_PLAN_", StringMatchType.Equals)
            )), classLoader);
        if (targetMethod == null)
            return;
        Class<?> smlZCloudSubscription = targetMethod.getDeclaringClass();
        Logger.i("Hooking: " + targetMethod);
        module.hook(targetMethod).intercept(chain ->
        {
            if (!Config.getUnlockZCloud())
                return chain.proceed();
            return 0;
        });
        targetMethod = DexKitCache.GetMethod("UnlockZCloud_SMLZCloudSubscription_GetSubscriptionTime_1", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("int")
                .paramCount(0)
                .addUsingField(FieldMatcher.create().name("MILLISECONDS"))
                .addInvoke(MethodMatcher.create().name("toDays"))
                .declaredClass(smlZCloudSubscription)
            )), classLoader);
        if (targetMethod == null)
            return;
        Logger.i("Hooking: " + targetMethod);
        module.hook(targetMethod).intercept(chain ->
        {
            if (!Config.getUnlockZCloud())
                return chain.proceed();
            return Integer.MAX_VALUE;
        });
        targetMethod = DexKitCache.GetMethod("UnlockZCloud_SMLZCloudSubscription_GetSubscriptionTime_2", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("long")
                .paramCount(1)
                .addUsingField(FieldMatcher.create().name("MILLISECONDS"))
                .addUsingField(FieldMatcher.create().name("DAYS"))
                .addInvoke(MethodMatcher.create().name("toDays"))
                .declaredClass(smlZCloudSubscription)
            )), classLoader);
        if (targetMethod == null)
            return;
        Logger.i("Hooking: " + targetMethod);
        module.hook(targetMethod).intercept(chain ->
        {
            if (!Config.getUnlockZCloud())
                return chain.proceed();
            return Long.MAX_VALUE;
        });
        targetMethod = DexKitCache.GetMethod("UnlockZCloud_SMLZCloudSubscription_CheckPlanValid_1", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("void")
                .paramCount(3)
                .paramTypes(null, "int", "int")
                .addUsingString("ZALO_CLOUD_SUBSCRIPTION_PLAN_", StringMatchType.Equals)
                .declaredClass(smlZCloudSubscription)
            )), classLoader);
        if (targetMethod == null)
            return;
        Logger.i("Hooking: " + targetMethod);
        module.hook(targetMethod).intercept(chain ->
        {
            if (!Config.getUnlockZCloud())
                return chain.proceed();
            return null;
        });
        List<Method> methods = DexKitCache.GetMethods("UnlockZCloud_SMLZCloudSubscription_GetIsSubscriptionWorking", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("boolean")
                .paramCount(0)
                .declaredClass(smlZCloudSubscription)
            )), classLoader);
        if (methods.isEmpty())
        {
            Logger.e("Target method not found 1");
            return;
        }
        for (Method method : methods)
        {
            Logger.i("Hooking: " + method);
            module.hook(method).intercept(chain ->
            {
                if (!Config.getUnlockZCloud())
                    return chain.proceed();
                return true;
            });
        }
        methods = DexKitCache.GetMethods("UnlockZCloud_SMLZCloudSubscription_CheckPlanValid_2", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("java.lang.Object")
                .paramCount(1)
                .declaredClass(smlZCloudSubscription)
                .not(MethodMatcher.create()
                    .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                    .returnType("java.lang.Object")
                    .paramCount(1)
                    .declaredClass(smlZCloudSubscription)
                    .addUsingString("createFamily", StringMatchType.Contains))
            )), classLoader);
        if (methods.isEmpty())
        {
            Logger.e("Target method not found 2");
            return;
        }
        for (Method method : methods)
        {
            Logger.i("Hooking: " + method);
            module.hook(method).intercept(chain ->
            {
                if (!Config.getUnlockZCloud())
                    return chain.proceed();
                return Boolean.TRUE;
            });
        }
    }
}