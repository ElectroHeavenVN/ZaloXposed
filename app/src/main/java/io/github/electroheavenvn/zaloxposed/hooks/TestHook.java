package io.github.electroheavenvn.zaloxposed.hooks;

import io.github.electroheavenvn.zaloxposed.utilities.*;

import org.json.*;
import org.luckypray.dexkit.query.*;
import org.luckypray.dexkit.query.enums.*;
import org.luckypray.dexkit.query.matchers.*;
import org.luckypray.dexkit.result.*;
import org.luckypray.dexkit.wrap.DexMethod;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;

import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import io.github.libxposed.api.*;

public class TestHook extends BaseHook
{
    @Override
    public void installHooks() throws Throwable
    {
        // new Handler(Looper.getMainLooper()).postDelayed(() ->
        // {
        // }, 30000);

        // doHook(System.class.getDeclaredMethod("exit", int.class), chain ->
        // {
            // Logger.i("Exit stack trace: " + Utils.GetStackTrace());
            // return chain.proceed();
        // });

        // Fake zBusiness plan
        // List<ClassData> classes = bridge.findClass(FindClass.create()
        //     .matcher(ClassMatcher.create()
        //         .modifiers(Modifier.PUBLIC | Modifier.ABSTRACT)
        //         .addUsingString("enable_business_tools_activation_ba", StringMatchType.Equals)
        //         .addUsingString("CONTENT_TIP_ZBUSINESS_${UserID}", StringMatchType.Equals)
        //         .addUsingString("enable_tip_tab_me_activation_business_account", StringMatchType.Equals)
        //         .addUsingString("CONFIG_POPUP_BA_PURCHASE_SUCCESS_${UserID}", StringMatchType.Equals)
        //         .addUsingString("description_vi", StringMatchType.Equals)
        //         .addUsingString("SETTING_SHOW_LABEL_BA_${UserID}", StringMatchType.Equals)
        //     ));
        // if (classes.isEmpty())
        // {
        //     Logger.e("Target class not found");
        //     return;
        // }
        // Class<?> zBusinessManagerClass = classes.get(0).getInstance(classLoader);
        // List<MethodData> methods = bridge.findMethod(FindMethod.create()
        //     .matcher(MethodMatcher.create()
        //         .declaredClass(zBusinessManagerClass)
        //         .modifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)
        //         .returnType("int")
        //         .paramCount(0)
        //         .addUsingNumber(0)
        //     ));
        // if (methods.isEmpty())
        // {
        //     Logger.e("Target method not found");
        //     return;
        // }
        // for (MethodData methodData : methods)
        // {
        //     Method method = methodData.getMethodInstance(classLoader);
        //     doHook(method, chain -> 3);
        // }

        // doHook(URL.class.getDeclaredMethod("openConnection"), chain ->
        // {
        //     Object result = chain.proceed();
        //     Logger.i("openConnection: " + chain.getThisObject());
        //     Logger.i("stack trace: " + Utils.GetStackTrace());
        //     return result;
        // });

        // Constructor<?> ctor = JSONObject.class.getConstructor(String.class);
        // doHook(ctor, chain ->
        // {
        //     Object result = chain.proceed();
        //     Logger.i("JSONObject constructor called with string: " + chain.getArg(0));
        //     Logger.i("stack trace: " + Utils.GetStackTrace());
        //     return result;
        // });
        // Method printStackTrace = Throwable.class.getDeclaredMethod("printStackTrace");
        // doHook(printStackTrace, chain ->
        // {
        //     Object result = chain.proceed();
        //     Logger.e("Exception:");
        //     Logger.e((Throwable)chain.getThisObject());
        //     Logger.i("stack trace: " + Utils.GetStackTrace());
        //     return result;
        // });
    }
}