package com.ehvn.zaloxposed.hooks;

import com.ehvn.zaloxposed.utilities.Config;
import com.ehvn.zaloxposed.utilities.Utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

@SuppressWarnings("unused")
public class FakeAdminHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .paramCount(2)
                .paramTypes("org.json.JSONObject", "java.lang.String")
                .addUsingString("isNoised", StringMatchType.Equals)
                .addUsingString("data", StringMatchType.Equals)
                .addUsingString("getGroupListFromServer decrypt data fail.", StringMatchType.Equals)));
        if (methods.isEmpty())
        {
            log("Target method not found");
            return;
        }
        Class<?> clazz = methods.get(0).getMethodInstance(lpparam.classLoader).getDeclaringClass();
        log("Found target class: " + clazz.getName());
        Method method = JSONObject.class.getMethod("optJSONArray", String.class);
        log("Hooking: " + method);
        XposedBridge.hookMethod(method, new XC_MethodHook()
        {
            @Override
            protected void afterHookedMethod(MethodHookParam param)
            {
                if (!Config.getEnableFakeGroupRole())
                    return;
                if (Config.getFakeGroupRoleLevel() != 0)
                    return;
                String key = (String) param.args[0];
                if (!"adminIds".equals(key) && !"admins".equals(key))
                    return;
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
                String userId = Utils.GetCurrentUserID();
                if (userId.isEmpty() || "0".equals(userId))
                    return;
                JSONArray arr = (JSONArray) param.getResult();
                if (arr == null)
                    return;
                if ("adminIds".equals(key))
                {
                    for (int i = 0; i < arr.length(); i++)
                    {
                        if (String.valueOf(arr.optLong(i)).equals(userId))
                            return;
                    }
                    arr.put(Long.parseLong(userId));
                }
                else
                {
                    for (int i = 0; i < arr.length(); i++)
                    {
                        JSONObject o = arr.optJSONObject(i);
                        if (o != null && String.valueOf(o.optLong("id")).equals(userId))
                            return;
                    }
                    JSONObject admin = buildAdminObject();
                    if (admin != null)
                        arr.put(admin);
                }
            }
        });
    }

    private JSONObject buildAdminObject()
    {
        try
        {
            String info = Utils.GetCurrentUserInfo();
            if (info.isEmpty())
                return null;
            JSONObject u = new JSONObject(info);
            JSONObject admin = new JSONObject();
            admin.put("id", u.optLong("uid", 0));
            admin.put("dName", u.optString("dpn", ""));
            admin.put("avatar", u.optString("avt", ""));
            admin.put("status", u.optString("stt", ""));
            admin.put("typeContact", 0);
            admin.put("proposalOwner", 1);
            admin.put("inviteInfo", JSONObject.NULL);
            admin.put("globalId", "");
            return admin;
        }
        catch (Exception e)
        {
            log("buildAdminObject error: " + e.getMessage());
            return null;
        }
    }
}