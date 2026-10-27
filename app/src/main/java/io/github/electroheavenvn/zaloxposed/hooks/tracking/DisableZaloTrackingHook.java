package io.github.electroheavenvn.zaloxposed.hooks.tracking;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;

public class DisableZaloTrackingHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<Method> methods = DexKitCache.GetMethods("DisableZaloTracking_1", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("java.lang.String")
                .paramCount(1)
                .addUsingString("service_map", StringMatchType.Equals)
                .addUsingString("type=?", StringMatchType.Equals)
                .addUsingString("last_used ASC", StringMatchType.Equals)
                .addUsingString("http://", StringMatchType.Equals)
                .addUsingString("_id=?", StringMatchType.Equals)
            )), classLoader);
        if (methods.isEmpty())
        {
            Logger.e("Target method not found 1");
            return;
        }
        for (Method method : methods)
        {
            Logger.i("Hooking: " + method);
            module.hook(method).intercept(chain ->
            {
                if (!Config.getDisableZaloTracking())
                    return chain.proceed();
                Object enumValue = chain.getArg(0);
                if (enumValue != null)
                {
                    String enumName = ((Enum<?>)enumValue).name();
                    if (enumName.equals("ZALO_LOG"))
                        return "";
                }
                return chain.proceed();
            });
        }
        methods = DexKitCache.GetMethods("DisableZaloTracking_2", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("void")
                .name("run")
                .paramCount(0)
                .addUsingString("\r\n--ZiNgMeEmAiL--\r\n", StringMatchType.Equals)
                .addUsingString("MIME-version", StringMatchType.Equals)
                .addUsingString("multipart/form-data; boundary=ZiNgMeEmAiL", StringMatchType.Equals)
                .addUsingString("--ZiNgMeEmAiL--\r\n", StringMatchType.Equals)
                .addUsingString("error_message", StringMatchType.Equals)
                .addUsingNumber(-11110000)
            )), classLoader);
        if (methods.isEmpty())
        {
            Logger.e("Target method not found 2");
            return;
        }
        for (Method method : methods)
        {
            Logger.i("Hooking: " + method);
            module.hook(method).intercept(chain ->
            {
                if (!Config.getDisableZaloTracking())
                    return chain.proceed();
                for (Field field : chain.getThisObject().getClass().getDeclaredFields())
                {
                    if (Modifier.isStatic(field.getModifiers()))
                        continue;
                    Object fieldValue = field.get(chain.getThisObject());
                    String fieldValueStr = fieldValue == null ? "" : fieldValue.toString();
                    if (fieldValueStr.contains("/tracking-v3"))
                        return null;
                }
                return chain.proceed();
            });
        }
        methods = DexKitCache.GetMethods("DisableZaloTracking_3", () -> bridge.findMethod(FindMethod.create()
        .matcher(MethodMatcher.create()
            .modifiers(Modifier.PUBLIC | Modifier.FINAL)
            .returnType("void")
            .paramCount(4)
            .paramTypes(int.class, byte[].class, int.class, null)
            .addUsingString("/tracking", StringMatchType.Equals)
            .addUsingString("STR_URL_UPLOAD_ZALO_TRACKING_BY_TYPE_%d", StringMatchType.Equals)
        )), classLoader);
        if (methods.isEmpty())
        {
            Logger.e("Target method not found 3");
            return;
        }
        for (Method method : methods)
        {
            Logger.i("Hooking: " + method);
            module.hook(method).intercept(chain ->
            {
                if (Config.getDisableZaloTracking())
                    return null;
                return chain.proceed();
            });
        }
        methods = DexKitCache.GetMethods("DisableZaloTracking_GetServiceMapPath", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("java.lang.String")
                .paramCount(1)
                .addUsingString("/group/ads", StringMatchType.Equals)
                .addUsingString("/api/qos/uploadcalllog", StringMatchType.Equals)
                .addUsingString("/zpads/inboxnative/getads", StringMatchType.Equals)
                .addUsingString("/api/qos/zinstant", StringMatchType.Equals)
                .addUsingString("/api/qos/uploaddetaillog", StringMatchType.Equals)
                .addUsingString("/api/qos/uploadactionlog", StringMatchType.Equals)
                .addUsingString("/api/qos/uploadv3", StringMatchType.Equals)
                .addUsingString("/api/qos/uploadv2", StringMatchType.Equals)
                .addUsingString("/zalocloudqos", StringMatchType.Equals)
                .addUsingString("/uploadservicepublic", StringMatchType.Equals)
                .addUsingString("/tracking", StringMatchType.Equals)
            )), classLoader);
        if (methods.isEmpty())
        {
            Logger.e("Target method not found 4");
            return;
        }
        for (Method method : methods)
        {
            Logger.i("Hooking: " + method);
            module.hook(method).intercept(chain ->
            {
                if (!Config.getBlockQoSEndpoints())
                    return chain.proceed();
                Enum<?> enumValue = (Enum<?>)chain.getArg(0);
                int ord = enumValue.ordinal();
                return switch (ord)
                {
                    case 154, 110, 115, 109, 108, 107, 106, 105, 145 -> "";
                    default -> chain.proceed();
                };
            });
        }
    }
}
