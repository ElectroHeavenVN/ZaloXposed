package com.ehvn.zaloxposed.hooks.permanent;

import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Logger;

import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.ClassData;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class EnableLabelHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<ClassData> classes = bridge.findClass(FindClass.create()
            .matcher(ClassMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .addUsingString("observer", StringMatchType.Equals)
                .addUsingString("key", StringMatchType.Equals)
                .addUsingString("fallback", StringMatchType.Equals)
                .addUsingString("uid", StringMatchType.Equals)
                .addUsingString("networkProvider", StringMatchType.Equals)
                .addUsingString("init with uid=", StringMatchType.Equals)
            ));
        if (classes.isEmpty())
        {
            Logger.e("Target class not found");
            return;
        }
        Class<?> targetClass = classes.get(0).getInstance(classLoader);
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(targetClass)
                .modifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)
                .returnType("int")
                .paramCount(2)
                .paramTypes("java.lang.String", "int")
                .addUsingString("key", StringMatchType.Equals)
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        for (MethodData methodData : methods)
        {
            Method cfgMethod = methodData.getMethodInstance(classLoader);
            Logger.i("Hooking: " + cfgMethod);
            module.hook(cfgMethod).intercept(chain ->
            {
                String key = (String)chain.getArg(0);
                if (key.equals("features@comm4work@chat_label@enable_chat_filter"))
                    return 2;
                if (key.equals("features@comm4work@chat_label@enable_manage_tag"))
                    return 1;
                return chain.proceed();
            });
        }
    }
}
