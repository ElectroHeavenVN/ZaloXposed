package io.github.electroheavenvn.zaloxposed.hooks.functional;

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

public class RestoreProfileMusicHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<Method> methods = DexKitCache.GetMethods("RestoreProfileMusic_1", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("boolean")
                .paramCount(4)
                .paramTypes("java.lang.String", "boolean", "boolean", "boolean")
                .addUsingString("get XML preference -> force to db, need clean code: %s", StringMatchType.Equals)
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
                if (!Config.getRestoreProfileMusic())
                    return chain.proceed();
                String key = (String) chain.getArg(0);
                if (key.startsWith("ENABLE_PROFILE_MUSIC_"))
                    return true;
                if (key.startsWith("ENABLE_PROFILE_RBT_MUSIC_"))
                    return true;
                if (key.startsWith("ENABLE_FORCE_MOVE_MP3_TO_RBT_"))
                    return false;
                return chain.proceed();
            });
        }
    }
}