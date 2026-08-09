package com.ehvn.zaloxposed.hooks;

import com.ehvn.zaloxposed.utilities.Config;
import com.ehvn.zaloxposed.utilities.Logger;
import com.ehvn.zaloxposed.utilities.Utils;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class ExtendedGroupSettingMenuHook extends BaseHook
{
    Method runMethod = null;

    @Override
    public void hook() throws Throwable
    {
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
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
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found 1");
            return;
        }
        runMethod = methods.get(0).getMethodInstance(classLoader);
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("boolean")
                .paramCount(0)
                .addUsingString("community@grouptype_threshold", StringMatchType.Equals)
                .addUsingNumber(0)
                .addUsingNumber(2)
                .addUsingNumber(100)
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found 2");
            return;
        }
        for (MethodData md : methods)
        {
            Method method = md.getMethodInstance(classLoader);
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