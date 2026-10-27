package io.github.electroheavenvn.zaloxposed.hooks.tracking;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class DisableFirebaseLoggingHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<Method> methods = DexKitCache.GetMethods("DisableFirebaseLogging_1", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("void")
                .paramCount(2)
                .addUsingString("Null status", StringMatchType.Equals)
                .addUsingString("Uploader", StringMatchType.Equals)
                .addUsingString("Unknown backend for %s, deleting event batch for it...", StringMatchType.Equals)
                .addUsingString("GDT_CLIENT_METRICS", StringMatchType.Equals)
                .addUsingString("sdk-version", StringMatchType.Equals)
                .addUsingString("CctTransportBackend", StringMatchType.Equals)
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
                if (Config.getDisableFirebaseLogging())
                    return null;
                return chain.proceed();
            });
        }
    }
}