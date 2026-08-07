package com.ehvn.zaloxposed.hooks;

import com.ehvn.zaloxposed.utilities.Config;
import com.ehvn.zaloxposed.utilities.Logger;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.libxposed.api.XposedInterface;

@SuppressWarnings("unused")
public class TTLHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("void")
                .paramCount(0)
                .addUsingString("LAST_TIME_LOAD_TTL_CONFIG_", StringMatchType.Equals)
            ));
        List<MethodData> formatTimeMethods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("java.lang.String")
                .paramCount(2)
                .addUsingNumber(86400000)
                .addUsingNumber(3600000)
                .addUsingNumber(60000)
                .addUsingNumber(1000)
            ));
        Set<String> classes = new HashSet<>();
        for (MethodData m : methods)
            classes.add(m.getClassName());
        String targetClassName = null;
        for (MethodData m : formatTimeMethods)
        {
            if (classes.contains(m.getClassName()))
            {
                targetClassName = m.getClassName();
                break;
            }
        }
        if (targetClassName == null)
        {
            Logger.e("Target class not found");
            return;
        }
        List<MethodData> ttlMethods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(targetClassName)
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("long")
                .paramTypes("java.lang.String")
            ));
        for (MethodData md : ttlMethods)
        {
            Method method = md.getMethodInstance(classLoader);
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