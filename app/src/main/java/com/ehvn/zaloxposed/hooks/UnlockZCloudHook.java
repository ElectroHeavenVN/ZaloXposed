package com.ehvn.zaloxposed.hooks;

import com.ehvn.zaloxposed.utilities.Config;
import com.ehvn.zaloxposed.utilities.Logger;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.FieldMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

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
        Method method = methods.get(0).getMethodInstance(classLoader);
        Class<?> clazz = method.getDeclaringClass();
        Logger.i("Hooking: " + method);
        module.hook(method).intercept(chain -> 
        {
            if (!Config.getUnlockZCloud())
                return chain.proceed();
            return 0;
        });
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("int")
                .paramCount(0)
                .addUsingField(FieldMatcher.create().name("MILLISECONDS"))
                .addInvoke(MethodMatcher.create().name("toDays"))
                .declaredClass(clazz)
            ));
        method = methods.get(0).getMethodInstance(classLoader);
        Logger.i("Hooking: " + method);
        module.hook(method).intercept(chain -> 
        {
            if (!Config.getUnlockZCloud())
                return chain.proceed();
            return Integer.MAX_VALUE;
        });
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
        method = methods.get(0).getMethodInstance(classLoader);
        Logger.i("Hooking: " + method);
        module.hook(method).intercept(chain -> 
        {
            if (!Config.getUnlockZCloud())
                return chain.proceed();
            return Integer.MAX_VALUE;
        });
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("boolean")
                .paramCount(0)
                .addUsingNumber(-1)
                .declaredClass(clazz)
            ));
        MethodData methodData = methods.get(0);
        method = methodData.getMethodInstance(classLoader);
        Logger.i("Hooking: " + method);
        module.hook(method).intercept(chain -> 
        {
            if (!Config.getUnlockZCloud())
                return chain.proceed();
            return true;
        });
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("boolean")
                .paramCount(0)
                .addCaller(methodData.getDescriptor())
                .declaredClass(clazz)
            ));
        method = methods.get(0).getMethodInstance(classLoader);
        Logger.i("Hooking: " + method);
        module.hook(method).intercept(chain -> 
        {
            if (!Config.getUnlockZCloud())
                return chain.proceed();
            return true;
        });
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("void")
                .paramCount(3)
                .paramTypes(null, "int", "int")
                .addUsingString("ZALO_CLOUD_SUBSCRIPTION_PLAN_", StringMatchType.Equals)
                .declaredClass(clazz)
            ));
        method = methods.get(0).getMethodInstance(classLoader);
        Logger.i("Hooking: " + method);
        module.hook(method).intercept(chain -> 
        {
            if (!Config.getUnlockZCloud())
                return chain.proceed();
            return 0;
        });

        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("java.lang.Object")
                .paramCount(1)
                .declaredClass(clazz)
            ));
        for (MethodData methodData2 : methods)
        {
            Method method2 = methodData2.getMethodInstance(classLoader);
            Logger.i("Hooking: " + method2);
            module.hook(method2).intercept(chain ->
            {
                if (!Config.getUnlockZCloud())
                    return chain.proceed();
                return Integer.MAX_VALUE;
            });
        }
    }
}