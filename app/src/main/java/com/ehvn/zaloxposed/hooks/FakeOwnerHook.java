package com.ehvn.zaloxposed.hooks;

import com.ehvn.zaloxposed.utilities.Config;
import com.ehvn.zaloxposed.utilities.Logger;
import com.ehvn.zaloxposed.utilities.Utils;

import org.json.JSONObject;

import java.lang.reflect.Constructor;

public class FakeOwnerHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        Constructor<?> constructor = JSONObject.class.getConstructor(String.class);
        Logger.i("Hooking: " + constructor);
        module.hook(constructor).intercept(chain ->
        {
            if (!Config.getEnableFakeGroupRole())
                return chain.proceed();
            if (Config.getFakeGroupRoleLevel() != 1)
                return chain.proceed();
            String jsonString = (String) chain.getArg(0);
            if (!jsonString.contains("\"creatorId\":"))
                return chain.proceed();
            if (!jsonString.contains(",\"ts\":"))
                return chain.proceed();
            String userId = Utils.GetCurrentUserID();
            if (userId.isEmpty() || "0".equals(userId))
                return chain.proceed();
            String modifiedJsonString = jsonString.replaceAll("\"creatorId\":\\d+", "\"creatorId\":" + userId);
            return chain.proceed(new Object[] { modifiedJsonString });
        });
    }
}