package com.ehvn.zaloxposed.hooks;

import com.ehvn.zaloxposed.utilities.Utils;

import org.json.JSONObject;

import java.lang.reflect.Constructor;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

@SuppressWarnings("unused")
public class FakeOwnerHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        Constructor<?> constructor = JSONObject.class.getConstructor(String.class);
        log("Hooking: " + constructor);
        XposedBridge.hookMethod(constructor, new XC_MethodHook()
        {
            @Override
            protected void beforeHookedMethod(MethodHookParam param)
            {
                String jsonString = (String) param.args[0];
                if (!jsonString.contains("\"creatorId\":"))
                    return;
                if (!jsonString.contains(",\"ts\":"))
                    return;
                String userId = Utils.GetCurrentUserID();
                if (userId.isEmpty() || "0".equals(userId))
                    return;
                String modifiedJsonString = jsonString.replaceAll("\"creatorId\":\\d+", "\"creatorId\":" + userId);
                param.args[0] = modifiedJsonString;
            }
        });
    }
}