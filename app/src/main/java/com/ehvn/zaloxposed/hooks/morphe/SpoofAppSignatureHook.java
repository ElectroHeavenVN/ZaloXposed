package com.ehvn.zaloxposed.hooks.morphe;

import android.content.pm.PackageInfo;
import android.content.pm.Signature;

import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Logger;
import com.ehvn.zaloxposed.utilities.Utils;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.util.List;

public class SpoofAppSignatureHook extends BaseHook
{
    private static final byte[] SHA1_SIG = new byte[]
    {
        -108, -121, -70, 118, -77, 46, -98, 54, 120, 95,
        -76, -61, 84, 0, 33, -8, 90, -8, -41, -73
    };

    @Override
    public void hook() throws Throwable
    {
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("java.lang.String")
                .paramCount(1)
                .paramTypes("android.content.Context")
                .addUsingString("SHA1", StringMatchType.Equals)
                .addUsingString("MainApplication", StringMatchType.Equals)
                .addUsingNumber(0)
                .addUsingNumber(64)
                .addUsingNumber(255)
                .addInvoke(Utils.GetDescriptor(MessageDigest.class.getMethod("getInstance", String.class)))
                .addInvoke(Utils.GetDescriptor(Signature.class.getMethod("toByteArray")))
                .addUsingField(Utils.GetDescriptor(PackageInfo.class.getField("signatures")))
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found 1");
            return;
        }
        for (MethodData methodData : methods)
        {
            Method method = methodData.getMethodInstance(classLoader);
            Logger.i("Hooking: " + method);
            module.hook(method).setPriority(1).intercept(chain ->
            {
                StringBuilder sb = new StringBuilder();
                for (byte b : SHA1_SIG)
                {
                    String hexString = Integer.toHexString(b & 255);
                    if (hexString.length() == 1)
                        sb.append("0");
                    sb.append(hexString);
                }
                return sb.toString();
            });
        }

        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("byte[]")
                .paramCount(2)
                .paramTypes("android.content.Context", "java.lang.String")
                .addUsingString("SHA1", StringMatchType.Equals)
                .addUsingNumber(0)
                .addUsingNumber(1)
                .addUsingNumber(2)
                .addUsingNumber(64)
                .addInvoke(Utils.GetDescriptor(MessageDigest.class.getMethod("getInstance", String.class)))
                .addInvoke(Utils.GetDescriptor(Signature.class.getMethod("toByteArray")))
                .addUsingField(Utils.GetDescriptor(PackageInfo.class.getField("signatures")))
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found 2");
            return;
        }
        for (MethodData methodData : methods)
        {
            Method method = methodData.getMethodInstance(classLoader);
            Logger.i("Hooking: " + method);
            module.hook(method).setPriority(1).intercept(chain -> SHA1_SIG);
        }
        // Method method = methods.get(0).getMethodInstance(classLoader);
        // Method getAppContext = Class.forName("com.zing.zalo.MainApplication", false, classLoader).getMethod("getAppContext");
        // new Thread(() ->
        // {
        //     while (true)
        //     {
        //         try
        //         {
        //             Thread.sleep(10000);
        //             Object value = method.invoke(null, getAppContext.invoke(null));
        //             Logger.i("SHA1 signature value: " + value);
        //         }
        //         catch (Exception e)
        //         {
        //             Logger.e(e);
        //         }
        //     }
        // }).start();
    }
}