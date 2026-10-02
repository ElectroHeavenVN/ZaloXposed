package com.ehvn.zaloxposed.hooks.functional;

import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Config;
import com.ehvn.zaloxposed.utilities.Logger;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class RestoreProfileMusicHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("boolean")
                .paramCount(4)
                .paramTypes("java.lang.String", "boolean", "boolean", "boolean")
                .addUsingString("get XML preference -> force to db, need clean code: %s", StringMatchType.Equals)
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        for (MethodData methodData : methods)
        {
            Method method = methodData.getMethodInstance(classLoader);
            Logger.i("Hooking: " + method);
            module.hook(method).intercept(chain ->
            {
                if (!Config.getRestoreProfileMusic())
                    return chain.proceed();
                String key = (String) chain.getArg(0);
                if (key.startsWith("ENABLE_PROFILE_MUSIC_"))
                    return true;
                if (key.startsWith("ENABLE_PROFILE_RBT_MUSIC_"))
                    return true;
                if (key.startsWith("ENABLE_FORCE_MOVE_MP3_TO_RBT_"))
                    return false;
                return chain.proceed();
            });
        }
    }
}