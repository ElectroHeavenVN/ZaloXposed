package io.github.electroheavenvn.zaloxposed.hooks.ui;

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

public class ExtendedGroupSettingMenuHook extends BaseHook
{
    Method runMethod = null;

    @Override
    public void hook() throws Throwable
    {
        runMethod = DexKitCache.GetMethod("ExtendedGroupSettingMenu_1", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .name("run")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("void")
                .paramCount(0)
                .addUsingString("highlight_admin_message", StringMatchType.Equals)
                .addUsingString("enable_message_history", StringMatchType.Equals)
                .addUsingString("manage_member", StringMatchType.Equals)
                .addUsingString("approve_new_member", StringMatchType.Equals)
                .addUsingString("change_group_owner", StringMatchType.Equals)
            )), classLoader);
        if (runMethod == null)
            return;
        List<Method> methods = DexKitCache.GetMethods("ExtendedGroupSettingMenu_2", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("boolean")
                .paramCount(0)
                .addUsingString("community@grouptype_threshold", StringMatchType.Equals)
                .addUsingNumber(0)
                .addUsingNumber(2)
                .addUsingNumber(100)
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
                if (!Config.getEnableExtendedGroupSettingMenu())
                    return chain.proceed();
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                boolean calledFromRun = false;
                for (StackTraceElement element : stackTrace)
                {
                    if (element.getClassName().equals(runMethod.getDeclaringClass().getName()) &&
                        element.getMethodName().equals(runMethod.getName()))
                    {
                        calledFromRun = true;
                        break;
                    }
                }
                if (!calledFromRun)
                    return chain.proceed();
                return true;
            });
        }
    }
}