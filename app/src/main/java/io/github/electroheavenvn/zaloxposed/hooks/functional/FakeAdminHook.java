package io.github.electroheavenvn.zaloxposed.hooks.functional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.ClassMatcher;

import java.lang.reflect.Method;
import java.util.List;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.CoreUtilityHelper;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;
import io.github.electroheavenvn.zaloxposed.utilities.Utils;

public class FakeAdminHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        Class<?> groupDAO = DexKitCache.GetClass("FakeAdmin_GroupDAO", () -> bridge.findClass(FindClass.create()
            .matcher(ClassMatcher.create()
                .addUsingString("isNoised", StringMatchType.Equals)
                .addUsingString("data", StringMatchType.Equals)
                .addUsingString("getGroupListFromServer decrypt data fail.", StringMatchType.Equals)
                .addUsingString("timestampGetGroupList", StringMatchType.Equals)
                .addUsingString("groupMemberInfo", StringMatchType.Equals)
                .addUsingString("group_", StringMatchType.Equals)
                .addUsingString("group_list_sync_failed", StringMatchType.Equals)
                .addUsingString("adminIds", StringMatchType.Equals)
                .addUsingString("memberIds", StringMatchType.Equals)
                .addUsingString("currentMems", StringMatchType.Equals)
            )), classLoader);
        if (groupDAO == null)
            return;
        Method method = JSONObject.class.getMethod("optJSONArray", String.class);
        Logger.i("Hooking: " + method);
        module.hook(method).intercept(chain ->
        {
            if (!Config.getEnableFakeGroupRole())
                return chain.proceed();
            if (Config.getFakeGroupRoleLevel() != 0)
                return chain.proceed();
            String key = (String) chain.getArg(0);
            if (!"adminIds".equals(key) && !"admins".equals(key))
                return chain.proceed();
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            boolean calledFromClazz = false;
            for (StackTraceElement element : stackTrace)
            {
                String className = element.getClassName();
                if (className.equals(groupDAO.getName()))
                {
                    calledFromClazz = true;
                    break;
                }
            }
            if (!calledFromClazz)
                return chain.proceed();
            String userId = CoreUtilityHelper.GetCurrentUserID();
            if (userId.isEmpty() || "0".equals(userId))
                return chain.proceed();
            JSONArray arr = (JSONArray)chain.proceed();
            if (arr == null)
                return null;
            if ("adminIds".equals(key))
            {
                for (int i = 0; i < arr.length(); i++)
                {
                    if (String.valueOf(arr.optLong(i)).equals(userId))
                        return arr;
                }
                arr.put(Long.parseLong(userId));
            }
            else
            {
                for (int i = 0; i < arr.length(); i++)
                {
                    JSONObject o = arr.optJSONObject(i);
                    if (o != null && String.valueOf(o.optLong("id")).equals(userId))
                        return arr;
                }
                JSONObject admin = buildAdminObject();
                if (admin != null)
                    arr.put(admin);
            }
            return arr;
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
            Logger.e("buildAdminObject error", e);
            return null;
        }
    }
}