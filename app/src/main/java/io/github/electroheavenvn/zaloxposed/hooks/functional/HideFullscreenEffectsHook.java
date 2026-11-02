package io.github.electroheavenvn.zaloxposed.hooks.functional;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.wrap.DexMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;
import io.github.electroheavenvn.zaloxposed.utilities.Utils;

public class HideFullscreenEffectsHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<Method> methods = DexKitCache.GetMethods("HideFullscreenEffects_1", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL | Modifier.SYNCHRONIZED)
                .returnType("void")
                .paramCount(5)
                .paramTypes("java.lang.String", "java.lang.String", null, "boolean", "byte")
                .addUsingString("/metadata", StringMatchType.Equals)
                .addUsingNumber(-1)
                .addUsingNumber(0)
                .addUsingNumber(14)
                .addUsingNumber(40)

            )), classLoader);
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        for (Method method : methods)
        {
            Logger.h(method);
            module.hook(method).intercept(chain ->
            {
                if (!Config.getHideFullscreenEffects())
                    return chain.proceed();
                String id = chain.getArg(0).toString();
                if ("12030".equals(id)) // rain / troi mua / mua lon qua ...
                    return null;
                if ("12022".equals(id)) // nhậu thôi
                    return null;
                return chain.proceed();
            });
        }

        //TODO: hide sticker effects
    }
}
