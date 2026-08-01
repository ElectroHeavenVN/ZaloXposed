package com.ehvn.zaloxposed.hooks;

import de.robv.android.xposed.*;
import org.luckypray.dexkit.result.MethodData;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.io.*;
import org.luckypray.dexkit.query.enums.StringMatchType;

@SuppressWarnings("unused")
public class EnableE2EEHook extends BaseHook {
    @Override
    public void hook() throws Throwable {
        List<MethodData> methods = bridge.findMethod(
        FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("boolean")
                .paramCount(2)
                .addUsingString("ENABLE_ENTRY_POINT_E2EE_GROUP_", StringMatchType.Equals)
            )
        );
        for (MethodData md : methods) {
            Method method = md.getMethodInstance(lpparam.classLoader);
            log("Hooking: " + method);
            XposedBridge.hookMethod(method, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) {
                    return true;
                }
            });
        }
    }
}