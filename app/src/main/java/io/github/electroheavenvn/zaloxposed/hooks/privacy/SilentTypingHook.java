package io.github.electroheavenvn.zaloxposed.hooks.privacy;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;

public class SilentTypingHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<Method> methods = DexKitCache.GetMethods("SilentTyping_1", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("void")
                .paramCount(4)
                .paramTypes("java.lang.String", "int", "boolean", "boolean")
                .addUsingString("uid", StringMatchType.Equals)
                .addUsingString("MessageRepository", StringMatchType.Equals)
                .addUsingNumber(10000)
                .addUsingNumber(3000)
                .addUsingNumber(3600001)
            )), classLoader);
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        for (Method method : methods)
        {
            Logger.i("Hooking: " + method);
            module.hook(method).intercept(chain ->
            {
                if (Config.getEnableSilentTyping())
                    return null;
                return chain.proceed();
            });
        }
    }
}
