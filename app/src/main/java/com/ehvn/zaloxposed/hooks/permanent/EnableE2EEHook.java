package com.ehvn.zaloxposed.hooks.permanent;

import com.ehvn.zaloxposed.hooks.BaseHook;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

@SuppressWarnings("unused")
public class EnableE2EEHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("boolean")
                .paramCount(2)
                .addUsingString("ENABLE_ENTRY_POINT_E2EE_GROUP_", StringMatchType.Equals)
            ));
        for (MethodData md : methods)
        {
            Method method = md.getMethodInstance(lpparam.classLoader);
            log("Hooking: " + method);
            XposedBridge.hookMethod(method, XC_MethodReplacement.returnConstant(true));
        }
    }
}