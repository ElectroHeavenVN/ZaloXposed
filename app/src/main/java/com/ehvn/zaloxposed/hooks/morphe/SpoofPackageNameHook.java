package com.ehvn.zaloxposed.hooks.morphe;

import android.annotation.SuppressLint;

import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Logger;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class SpoofPackageNameHook extends BaseHook
{
    @SuppressLint("PrivateApi")
    @Override
    public void hook() throws Throwable
    { 
        Method getPackageName = Class.forName("android.app.ContextImpl", false, classLoader).getDeclaredMethod("getPackageName");
        Logger.i("Hooking: " + getPackageName);
        module.hook(getPackageName).intercept(chain ->
        {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            boolean calledFromNative = false;
            for (StackTraceElement element : stackTrace)
            {
                if (element.isNativeMethod())
                {
                    if (element.getMethodName().equals("getStaticValue"))
                    {
                        calledFromNative = true;
                        break;
                    }
                }
                if (element.getClassName().equals(Runtime.class.getName()))
                {
                    if (element.getMethodName().equals("nativeLoad") || element.getMethodName().equals("loadLibrary0"))
                    {
                        calledFromNative = true;
                        break;
                    }
                }
            }
            if (!calledFromNative)
                return chain.proceed();
            return "com.zing.zalo";
        });
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)
                .returnType("java.lang.String")
                .paramCount(2)
                .paramTypes("android.content.Context", "int")
                .addUsingString("activity", StringMatchType.Equals)
                .addUsingString("processName", StringMatchType.Equals)
                .addUsingString("/proc/", StringMatchType.Equals)
                .addUsingString("/cmdline", StringMatchType.Equals)
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
                int pid = (int)chain.getArg(1);
                if (pid == android.os.Process.myPid())
                    return "com.zing.zalo";
                return chain.proceed();
            });
        }
    }
}
