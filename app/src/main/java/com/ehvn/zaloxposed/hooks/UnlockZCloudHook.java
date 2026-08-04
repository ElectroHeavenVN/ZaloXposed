package com.ehvn.zaloxposed.hooks;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.FieldMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

@SuppressWarnings("unused")
public class UnlockZCloudHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("int")
                .paramCount(0)
                .addUsingString("ZALO_CLOUD_SUBSCRIPTION_PLAN_", StringMatchType.Equals)
            ));
        Method method = methods.get(0).getMethodInstance(lpparam.classLoader);
        Class<?> clazz = method.getDeclaringClass();
        log("Hooking: " + method);
        XposedBridge.hookMethod(method, XC_MethodReplacement.returnConstant(0));
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("int")
                .paramCount(0)
                .addUsingField(FieldMatcher.create().name("MILLISECONDS"))
                .addInvoke(MethodMatcher.create().name("toDays"))
                .declaredClass(clazz)
            ));
        method = methods.get(0).getMethodInstance(lpparam.classLoader);
        log("Hooking: " + method);
        XposedBridge.hookMethod(method, XC_MethodReplacement.returnConstant(Integer.MAX_VALUE));
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("long")
                .paramCount(1)
                .addUsingField(FieldMatcher.create().name("MILLISECONDS"))
                .addUsingField(FieldMatcher.create().name("DAYS"))
                .addInvoke(MethodMatcher.create().name("toDays"))
                .declaredClass(clazz)
            ));
        method = methods.get(0).getMethodInstance(lpparam.classLoader);
        log("Hooking: " + method);
        XposedBridge.hookMethod(method, XC_MethodReplacement.returnConstant(Integer.MAX_VALUE));
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("boolean")
                .paramCount(0)
                .addUsingNumber(-1)
                .declaredClass(clazz)
            ));
        MethodData methodData = methods.get(0);
        method = methodData.getMethodInstance(lpparam.classLoader);
        log("Hooking: " + method);
        XposedBridge.hookMethod(method, XC_MethodReplacement.returnConstant(true));
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("boolean")
                .paramCount(0)
                .addCaller(methodData.getDescriptor())
                .declaredClass(clazz)
            ));
        method = methods.get(0).getMethodInstance(lpparam.classLoader);
        log("Hooking: " + method);
        XposedBridge.hookMethod(method, XC_MethodReplacement.returnConstant(true));
    }
}