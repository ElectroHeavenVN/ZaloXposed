package com.ehvn.zaloxposed.hooks;

import com.ehvn.zaloxposed.utilities.*;

import org.json.*;
import org.luckypray.dexkit.query.*;
import org.luckypray.dexkit.query.enums.*;
import org.luckypray.dexkit.query.matchers.*;
import org.luckypray.dexkit.result.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;

import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;

import io.github.libxposed.api.*;

public class TestHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        // module.hook(System.class.getDeclaredMethod("exit", int.class)).intercept(chain ->
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
        //     module.hook(method).intercept(chain -> 3);
        // }

        // module.hook(URL.class.getDeclaredMethod("openConnection")).intercept(chain ->
        // {
        //     Object result = chain.proceed();
        //     Logger.i("openConnection: " + chain.getThisObject());
        //     Logger.i("stack trace: " + Utils.GetStackTrace());
        //     return result;
        // });

        // Constructor<?> ctor = JSONObject.class.getConstructor(String.class);
        // Logger.i("Hooking: " + ctor);
        // module.hook(ctor).intercept(chain ->
        // {
        //     Object result = chain.proceed();
        //     Logger.i("JSONObject constructor called with string: " + chain.getArg(0));
        //     Logger.i("stack trace: " + Utils.GetStackTrace());
        //     return result;
        // });
        // Method printStackTrace = Throwable.class.getDeclaredMethod("printStackTrace");
        // module.hook(printStackTrace).intercept(chain ->
        // {
        //     Object result = chain.proceed();
        //     Logger.e("Exception:");
        //     Logger.e((Throwable)chain.getThisObject());
        //     Logger.i("stack trace: " + Utils.GetStackTrace());
        //     return result;
        // });
    }
}