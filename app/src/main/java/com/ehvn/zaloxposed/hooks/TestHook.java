package com.ehvn.zaloxposed.hooks;

import de.robv.android.xposed.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.*;
import org.json.*;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;
import android.view.View;
import android.view.ViewGroup;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import com.ehvn.zaloxposed.utilities.Utils;

@SuppressWarnings("unused")
public class TestHook extends BaseHook {

    @Override
    public void hook() throws Throwable {
        // Constructor ctor = JSONObject.class.getConstructor(String.class);
        // log("Hooking: " + ctor.toString());
        // XposedBridge.hookMethod(ctor, new XC_MethodHook() {
        //     @Override
        //     protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        //         String jsonString = (String)param.args[0];
        //         log("JSONObject constructor called with string: " + jsonString);
        //         log("stack trace: " + Utils.GetStackTrace());
        //     }
        // });
    }
}