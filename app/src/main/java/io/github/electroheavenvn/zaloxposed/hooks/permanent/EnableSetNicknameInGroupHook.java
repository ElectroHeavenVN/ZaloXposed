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
import io.github.electroheavenvn.zaloxposed.utilities.Utils;

public class EnableSetNicknameInGroupHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<Method> methods = DexKitCache.GetMethods("EnableSetNicknameInGroup_1", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(Utils.GetConfigClass())
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("int")
                .paramCount(0)
                .addUsingString("ENABLE_NICKNAME_IN_GROUP", StringMatchType.Equals)
            )), classLoader);
        for (Method method : methods)
        {
            Logger.i("Hooking: " + method);
            module.hook(method).intercept(chain -> 1);
        }
    }
}