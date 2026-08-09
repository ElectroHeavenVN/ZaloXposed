package com.ehvn.zaloxposed.hooks.permanent;

import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Logger;
import com.ehvn.zaloxposed.utilities.Utils;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class EnableSetNicknameInGroupHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(Utils.GetConfigClass())
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("int")
                .paramCount(0)
                .addUsingString("ENABLE_NICKNAME_IN_GROUP", StringMatchType.Equals)
            ));
        for (MethodData md : methods)
        {
            Method method = md.getMethodInstance(classLoader);
            Logger.i("Hooking: " + method);
            module.hook(method).intercept(chain -> 1);
        }
    }
}