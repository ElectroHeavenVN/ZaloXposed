package com.ehvn.zaloxposed.hooks.morphe;

import android.content.pm.Signature;

import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Logger;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import io.github.libxposed.api.XposedInterface;

public class SpoofAppSignatureHook extends BaseHook
{
    private static final String SHA1_SIG = "9487ba76b32e9e36785fb4c3540021f85af8d7b7";

    // fuck java signed byte!
    private static final byte[] SIG = new byte[]
    {
        48, -126, 1, -99, 48, -126, 1, 6, -96, 3, 2,
        1, 2, 2, 4, 79, 23, -119, 113, 48, 13,
        6, 9, 42, -122, 72, -122, -9, 13, 1, 1,
        5, 5, 0, 48, 19, 49, 17, 48, 15, 6,
        3, 85, 4, 3, 19, 8, 122, 105, 110, 103,
        116, 97, 108, 107, 48, 30, 23, 13, 49, 50,
        48, 49, 49, 57, 48, 51, 48, 57, 51, 55,
        90, 23, 13, 51, 55, 48, 49, 49, 50, 48,
        51, 48, 57, 51, 55, 90, 48, 19, 49, 17,
        48, 15, 6, 3, 85, 4, 3, 19, 8, 122,
        105, 110, 103, 116, 97, 108, 107, 48, -127, -97,
        48, 13, 6, 9, 42, -122, 72, -122, -9, 13,
        1, 1, 1, 5, 0, 3, -127, -115, 0, 48,
        -127, -119, 2, -127, -127, 0, -40, -36, -122, -18,
        -84, -51, -115, 127, -25, 34, 57, 26, 58, 26,
        -32, 52, 8, 43, 36, -81, 12, -90, 50, 68,
        -46, -1, 18, -52, -97, -38, 77, 106, -100, 27,
        -33, -11, -59, -121, -58, 72, -84, 62, -103, -27,
        72, 82, -54, 82, -50, -32, 18, 3, -53, -103,
        -11, -108, 89, 58, -79, -32, 35, -68, -40, -90,
        -66, -101, 30, 5, 108, 61, -25, 54, 49, -59,
        111, -123, -11, -19, -123, 118, -24, 80, -10, 125,
        -37, -54, 0, 11, 83, 56, 72, 29, -14, 56,
        -96, -46, 124, 41, 59, -98, 40, -74, -102, -50,
        36, -55, -55, 38, 48, 99, 34, 56, 50, 9,
        76, -123, 32, 16, 1, -73, -66, 127, 33, 7,
        -92, 82, -125, 95, 2, 3, 1, 0, 1, 48,
        13, 6, 9, 42, -122, 72, -122, -9, 13, 1,
        1, 5, 5, 0, 3, -127, -127, 0, -82, -67,
        -118, -14, 127, -61, 23, -117, 96, -126, -47, -37,
        122, 95, 102, -86, -47, -37, 85, -56, 35, 20,
        92, 93, -46, 31, -25, 33, -30, 41, -7, 11,
        119, 2, 115, -122, 84, 67, 43, 92, 94, -122,
        103, -12, -103, 94, -99, 32, 106, -37, 125, 38,
        -61, -37, 112, -14, -55, 113, 99, -115, 68, -73,
        98, 65, 109, -11, -43, 16, -96, -123, 38, -19,
        -111, -3, -47, -63, -24, -26, 117, 29, -120, 50,
        -20, 50, 21, 76, 17, 104, 14, 100, 126, 96,
        92, 14, -122, 18, 112, 44, 112, 82, 67, 36,
        -90, 17, 66, 76, 105, -60, -45, -28, 58, 39,
        86, 85, 30, -20, 95, 78, 77, -23, 102, 51,
        17, -108, -57, 68, -124, -95,
    };

    @Override
    public void hook() throws Throwable
    {
        Method toByteArray = Signature.class.getDeclaredMethod("toByteArray");
        Logger.i("Hooking: " + toByteArray);
        module.hook(toByteArray).setPriority(1).intercept(chain ->
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
            return SIG;
        });
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