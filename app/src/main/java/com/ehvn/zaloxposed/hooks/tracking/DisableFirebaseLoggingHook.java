package com.ehvn.zaloxposed.hooks.tracking;

import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Config;
import com.ehvn.zaloxposed.utilities.Logger;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Modifier;
import java.util.List;

public class DisableFirebaseLoggingHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
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
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        for (MethodData md : methods)
        {
            Logger.i("Hooking: " + md.getMethodInstance(classLoader));
            module.hook(md.getMethodInstance(classLoader)).intercept(chain ->
            {
                if (Config.getDisableFirebaseLogging())
                    return null;
                return chain.proceed();
            });
        }
    }
}