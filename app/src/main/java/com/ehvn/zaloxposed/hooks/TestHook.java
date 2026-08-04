package com.ehvn.zaloxposed.hooks;

import com.ehvn.zaloxposed.utilities.Utils;

import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

@SuppressWarnings("unused")
public class TestHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        // Constructor<?> ctor = JSONObject.class.getConstructor(String.class);
        // log("Hooking: " + ctor);
        // XposedBridge.hookMethod(ctor, new XC_MethodHook()
        // {
        //     @Override
        //     protected void afterHookedMethod(MethodHookParam param)
        //     {
        //         String jsonString = (String) param.args[0];
        //         log("JSONObject constructor called with string: " + jsonString);
        //         log("stack trace: " + Utils.GetStackTrace());
        //     }
        // });
        // Method printStackTrace = Throwable.class.getDeclaredMethod("printStackTrace");
        // XposedBridge.hookMethod(printStackTrace, new XC_MethodHook()
        // {
        //     @Override
        //     protected void beforeHookedMethod(MethodHookParam param) throws Throwable
        //     {
        //         log("Exception:\n" + Utils.GetStackTrace((Throwable) param.thisObject));
        //         log("Stack trace:\n" + Utils.GetStackTrace());
        //     }
        // });
    }
}