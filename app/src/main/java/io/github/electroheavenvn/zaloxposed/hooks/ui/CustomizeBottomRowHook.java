package io.github.electroheavenvn.zaloxposed.hooks.ui;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;

import org.json.JSONObject;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class CustomizeBottomRowHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<Method> methods = DexKitCache.GetMethods("CustomizeBottomRow_LoadEnabledTabs", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("int")
                .paramCount(3)
                .paramTypes("int", "java.lang.String", "boolean")
                .addUsingString("get XML preference -> force to db, need clean code: %s", StringMatchType.Equals)
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
                if (!Config.getEnableCustomizeBottomRow())
                    return chain.proceed();
                String key = (String)chain.getArg(1);
                if (key.equals("ENABLE_GROUP_TAB"))
                    return Config.getShowGroupsTab() ? 1 : 0;
                if (key.equals("ENABLE_TIMELINE_TAB")) 
                    return Config.getHideNewsFeedTab() ? 0 : 1;
                return chain.proceed();
            });
        }
        methods = DexKitCache.GetMethods("CustomizeBottomRow_LoadTabMoreFromJson", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("void")
                .paramCount(1)
                .paramTypes("org.json.JSONObject")
                .addUsingString("enable_tabmore", StringMatchType.Equals)
                .addUsingString("CONFIG_TABMORE", StringMatchType.Equals)
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
                if (!Config.getEnableCustomizeBottomRow())
                    return chain.proceed();
                JSONObject jsonObject = (JSONObject)chain.getArg(0);
                if (jsonObject == null)
                    return chain.proceed();
                jsonObject.put("enable_tabmore", Config.getShowMoreTab() ? 1 : 0);
                return chain.proceed(new Object[] {jsonObject});
            });
        }
        methods = DexKitCache.GetMethods("CustomizeBottomRow_LoadEnabledTabsFromJson", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("void")
                .paramCount(2)
                .paramTypes("org.json.JSONObject", "boolean")
                .addUsingString("enable_tabdiscovery", StringMatchType.Equals)
                .addUsingString("enable_streamline", StringMatchType.Equals)
                .addUsingString("enable_global_search", StringMatchType.Equals)
                .addUsingString("CONFIG_TABDISCOVERY", StringMatchType.Equals)
            )), classLoader);
        if (methods.isEmpty())
        {
            Logger.e("Target method not found 3");
            return;
        }
        for (Method method : methods)
        {
            Logger.i("Hooking: " + method);
            module.hook(method).intercept(chain ->
            {
                if (!Config.getEnableCustomizeBottomRow())
                    return chain.proceed();
                JSONObject jsonObject = (JSONObject)chain.getArg(0);
                if (jsonObject == null)
                    return chain.proceed();
                jsonObject.put("enable_tabdiscovery", Config.getHideDiscoveryTab() ? 0 : 1);
                return chain.proceed(new Object[] {jsonObject, chain.getArg(1)});
            });
        }
    }
}