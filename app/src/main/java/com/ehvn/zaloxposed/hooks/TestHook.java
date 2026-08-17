package com.ehvn.zaloxposed.hooks;

import com.ehvn.zaloxposed.utilities.Logger;
import com.ehvn.zaloxposed.utilities.Utils;

import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TestHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
    //    Class<?> clazz = Class.forName("com.zing.zalo.zinstant.utils.ZinstantCommunicatorHelper", false, classLoader);
    //    Method method = null;
    //    for (Method m : clazz.getDeclaredMethods())
    //    {
    //        if (m.getName().equals("sendRequest"))
    //        {
    //            method = m;
    //            break;
    //        }
    //    }
    //    if (method == null)
    //    {
    //        Logger.e("Target method not found");
    //        return;
    //    }
    //    Logger.i("Hooking: " + method);
    //    module.hook(method).intercept(chain ->
    //    {
    //         String result = (String)chain.proceed();
    //         String arg1 = new String((byte[])chain.getArg(1), StandardCharsets.UTF_8);
    //         Logger.i("Send request, arg1: " + arg1 + ", result: " + result);
    //         return result;
    //    });

//        Class<?> clazz = Class.forName("com.zing.zalo.ui.chat.rightmenu.ChatInfoView", false, classLoader);
//        Method method = null;
//        for (Method m : clazz.getDeclaredMethods())
//        {
//            if (m.getName().equals("J6"))
//            {
//                method = m;
//                break;
//            }
//        }
//        if (method == null)
//        {
//            Logger.e("Target method not found");
//            return;
//        }
//        module.hook(method).intercept(chain ->
//        {
//            Object result = chain.proceed();
//            Object arg0 = chain.getArg(0);
//            Logger.i("arg0: " + arg0);
//            Logger.i("stack trace: " + Utils.GetStackTrace());
//            return result;
//        });
//        Constructor<?> ctor = Class.forName("wy.e", false, classLoader).getConstructor(JSONObject.class);
//        Logger.i("Hooking: " + ctor);
//        module.hook(ctor).intercept(chain ->
//        {
//            Object result = chain.proceed();
//            Logger.i("Ctor called with string: " + chain.getArg(0));
//            return result;
//        });
//        Constructor<?> ctor = JSONObject.class.getConstructor(String.class);
//        Logger.i("Hooking: " + ctor);
//        module.hook(ctor).intercept(chain ->
//        {
//            Object result = chain.proceed();
//            Logger.i("JSONObject constructor called with string: " + chain.getArg(0));
//            Logger.i("stack trace: " + Utils.GetStackTrace());
//            return result;
//        });
//        Method printStackTrace = Throwable.class.getDeclaredMethod("printStackTrace");
//        module.hook(printStackTrace).intercept(chain ->
//        {
//            Object result = chain.proceed();
//            Logger.e("Exception:");
//            Logger.e((Throwable)chain.getThisObject());
//            Logger.i("stack trace: " + Utils.GetStackTrace());
//            return result;
//        });
        // XposedHelpers.findAndHookMethod("g30.n", classLoader, "a", "g30.n", int.class, "java.lang.String", "java.lang.String", int.class, "m52.l", "m52.p", "m52.p", int.class, new XC_MethodHook() {
        //     @Override
        //     protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        //         log("path: " + param.args[3]);
        //         log(Utils.GetStackTrace());
        //     }
        // });
    }
}