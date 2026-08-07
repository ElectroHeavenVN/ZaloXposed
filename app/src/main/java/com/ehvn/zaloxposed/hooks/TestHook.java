package com.ehvn.zaloxposed.hooks;

import com.ehvn.zaloxposed.utilities.Logger;
import com.ehvn.zaloxposed.utilities.Utils;

import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class TestHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
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