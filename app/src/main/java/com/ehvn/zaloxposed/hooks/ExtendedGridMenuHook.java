package com.ehvn.zaloxposed.hooks;

import android.util.SparseArray;

import com.ehvn.zaloxposed.utilities.Config;

import org.json.JSONArray;
import org.json.JSONObject;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

@SuppressWarnings("unused")
public class ExtendedGridMenuHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(8)
                .addUsingString("CHAT_TYPO_FEATURE_ENABLE", StringMatchType.Equals)
            ));
        Class<?> clazz = methods.get(0).getMethodInstance(lpparam.classLoader).getDeclaringClass();
        List<MethodData> sparseArrayMethods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(0)
                .returnType("android.util.SparseArray")
                .declaredClass(clazz)
            ));
        for (MethodData md : methods)
        {
            Method method = md.getMethodInstance(lpparam.classLoader);
            log("Hooking: " + method);
            XposedBridge.hookMethod(method, new XC_MethodHook()
            {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable
                {
                    if (!Config.getEnableExtendedGridMenu())
                        return;
                    if (param.getResult() != null)
                        return;
                    ArrayList<SparseArray<?>> arrays = new ArrayList<>();
                    for (MethodData sparseArr : sparseArrayMethods)
                    {
                        Method spareArrM = sparseArr.getMethodInstance(lpparam.classLoader);
                        SparseArray<?> arr = (SparseArray<?>)spareArrM.invoke(param.thisObject);
                        arrays.add(arr);
                    }
                    int num = (int) param.args[0];
                    for (SparseArray<?> arr : arrays)
                    {
                        Object elem = arr.get(num, null);
                        if (elem != null)
                            param.setResult(elem);
                    }
                }
            });
        }
        Method method = JSONObject.class.getMethod("optJSONObject", String.class);
        log("Hooking: " + method);
        XposedBridge.hookMethod(method, new XC_MethodHook()
        {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable
            {
                if (!Config.getEnableExtendedGridMenu())
                    return;
                String key = (String) param.args[0];
                if (key.equals("chat_1_1") || key.equals("chat_group") || key.equals("community"))
                {
                    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                    boolean calledFromClazz = false;
                    for (StackTraceElement element : stackTrace)
                    {
                        String className = element.getClassName();
                        if (className.equals(clazz.getName()))
                        {
                            calledFromClazz = true;
                            break;
                        }
                    }
                    if (!calledFromClazz)
                        return;
                    JSONObject obj = (JSONObject) param.getResult();
                    if (obj == null)
                        obj = new JSONObject();
                    JSONObject attach = obj.optJSONObject("attach");
                    if (attach == null)
                        attach = new JSONObject();
                    JSONArray sectionMore = attach.optJSONArray("section_more");
                    if (sectionMore == null)
                        sectionMore = new JSONArray();
                    for (int i = 1; i <= 50; i++)
                        sectionMore.put(i);
                    boolean isChatDirect = key.equals("chat_1_1");
                    boolean isChatGroup = key.equals("chat_group");
                    boolean isChatCommunityGroup = key.equals("community");
                    sectionMore.put(101);
                    if (isChatDirect)
                        sectionMore.put(102);
                    else
                    {
                        sectionMore.put(104);
                        sectionMore.put(105);
                    }
                    attach.put("section_more", sectionMore);
                    obj.put("attach", attach);
                    param.setResult(obj);
                }
            }
        });
    }
}