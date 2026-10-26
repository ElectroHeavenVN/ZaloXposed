package io.github.electroheavenvn.zaloxposed.hooks.functional;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;

public class UnlockZStyleHook extends BaseHook
{
    private static Field zStylePackageIdField = null;

    @Override
    public void hook() throws Throwable
    {
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .paramCount(0)
                .addCaller(MethodMatcher.create()
                    .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                    .returnType("boolean")
                    .paramCount(0)
                    .addUsingString("subscribePackageIdConfig", StringMatchType.Equals))
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        for (MethodData methodData : methods)
        {
            Method method = methodData.getMethodInstance(classLoader);
            module.hook(method).intercept(chain ->
            {
                Object result = chain.proceed();
                if (!Config.getUnlockZStyle())
                    return result;
                if (zStylePackageIdField != null)
                {
                    zStylePackageIdField.setInt(result, 1);
                    return result;
                }
                Object newInstance = ((Method)chain.getExecutable()).getReturnType().getDeclaredConstructor().newInstance();
                for (Field field : newInstance.getClass().getDeclaredFields())
                {
                    if (field.getType() != int.class)
                        continue;
                    boolean isAccessible = field.isAccessible();
                    field.setAccessible(true);
                    if (field.getInt(newInstance) == 0)
                    {
                        field.setInt(newInstance, 6767);
                        String toStr = newInstance.toString();
                        if (toStr.contains("zStylePackageId=6767"))
                        {
                            zStylePackageIdField = field;
                            zStylePackageIdField.setInt(result, 1);
                            break;
                        }
                    }
                    field.setAccessible(isAccessible);
                }
                return result;
            });
        }
    }
}
