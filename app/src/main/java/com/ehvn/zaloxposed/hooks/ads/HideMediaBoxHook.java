package com.ehvn.zaloxposed.hooks.ads;

import android.view.View;

import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Config;
import com.ehvn.zaloxposed.utilities.Logger;
import com.ehvn.zaloxposed.utilities.Utils;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.FieldMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class HideMediaBoxHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("void")
                .addUsingString("ProfileManager: getContactProfileOrPutDefault uid invalid", StringMatchType.Equals)
                .addUsingString("MessageManager", StringMatchType.Equals)
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        Class<?> clazz = methods.get(0).getMethodInstance(classLoader).getDeclaringClass();
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(clazz)
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("void")
                .paramCount(6)
                .paramTypes("com.zing.zalo.data.chat.model.tabmessage.Conversation", "java.util.ArrayList", "int", "boolean", "int",  null)
                .addUsingField(FieldMatcher.create().name("MediaBox"))
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        for (MethodData methodData : methods)
        {
            Method method = methodData.getMethodInstance(classLoader);
            Logger.i("Hooking: " + method);
            module.hook(method).intercept(chain ->
            {
                if (!Config.getHideMediaBox())
                    return chain.proceed();
                int type = (Integer)chain.getArg(4);
                if (type == 3)
                    return null;
                return chain.proceed();
            });
        }
    }
}
