package io.github.electroheavenvn.zaloxposed.hooks.functional;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;

public class TTLHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<Method> methods = DexKitCache.GetMethods("TTL_1", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(ClassMatcher.create()
                    .addMethod(MethodMatcher.create()
                        .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                        .returnType("void")
                        .paramCount(0)
                        .addUsingString("LAST_TIME_LOAD_TTL_CONFIG_", StringMatchType.Equals)
                        //.addUsingString("SELF_DELETE_CONVERSATIONS_TTL_CONFIG_", StringMatchType.Contains)
                    )
                    .addMethod(MethodMatcher.create()
                        .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                        .returnType("java.lang.String")
                        .addUsingNumber(86400000)
                        .addUsingNumber(3600000)
                        .addUsingNumber(60000)
                        .addUsingNumber(1000)
                    )
                    //.addUsingString("SELF_DELETE_CONVERSATION_TIME_FRAMES", StringMatchType.Equals)
                )
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("long")
                .paramTypes("java.lang.String")
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
                if (Config.getEnableTTLOverride())
                    return Config.getTTL();
                return chain.proceed();
            });
        }
    }
}