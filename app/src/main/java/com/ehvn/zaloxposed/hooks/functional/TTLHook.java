package com.ehvn.zaloxposed.hooks.functional;

import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Config;
import com.ehvn.zaloxposed.utilities.Logger;

import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindField;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.ClassData;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TTLHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<MethodData> ttlMethods = bridge.findMethod(FindMethod.create()
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
            ));
        if (ttlMethods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
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