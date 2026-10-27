package io.github.electroheavenvn.zaloxposed.hooks.permanent;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;

@SuppressWarnings("unused")
public class EnableE2EEHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<Method> methods = DexKitCache.GetMethods("EnableE2EE_1", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("boolean")
                .paramCount(2)
                .addUsingString("ENABLE_ENTRY_POINT_E2EE_GROUP_", StringMatchType.Equals)
            )), classLoader);
        for (Method method : methods)
        {
            Logger.i("Hooking: " + method);
            module.hook(method).intercept(chain -> true);
        }
        methods = DexKitCache.GetMethods("EnableE2EE_2", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("boolean")
                .paramCount(3)
                .paramTypes("java.lang.String", null, null)
                .addUsingString("threadId", StringMatchType.Equals)
                .addUsingString("chatKind", StringMatchType.Equals)
                .addUsingString("E2EE_ALLOW_STRANGER", StringMatchType.Equals)
            )), classLoader);
        for (Method method : methods)
        {
            Logger.i("Hooking: " + method);
            module.hook(method).intercept(chain -> true);
        }
    }
}