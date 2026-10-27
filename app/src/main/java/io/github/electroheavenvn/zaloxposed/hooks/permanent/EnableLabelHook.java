package io.github.electroheavenvn.zaloxposed.hooks.permanent;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;

public class EnableLabelHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<Method> methods = DexKitCache.GetMethods("EnableLabel_1", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(ClassMatcher.create()
                    .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                    .addUsingString("observer", StringMatchType.Equals)
                    .addUsingString("key", StringMatchType.Equals)
                    .addUsingString("fallback", StringMatchType.Equals)
                    .addUsingString("uid", StringMatchType.Equals)
                    .addUsingString("networkProvider", StringMatchType.Equals)
                    .addUsingString("init with uid=", StringMatchType.Equals))
                .modifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)
                .returnType("int")
                .paramCount(2)
                .paramTypes("java.lang.String", "int")
                .addUsingString("key", StringMatchType.Equals)
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
