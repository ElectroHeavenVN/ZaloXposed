package com.ehvn.zaloxposed.hooks.tracking;

import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Config;
import com.ehvn.zaloxposed.utilities.Logger;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;
import org.luckypray.dexkit.result.UsingFieldData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class DisableZaloTrackingHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("java.lang.String")
                .paramCount(1)
                .addUsingString("service_map", StringMatchType.Equals)
                .addUsingString("type=?", StringMatchType.Equals)
                .addUsingString("last_used ASC", StringMatchType.Equals)
                .addUsingString("http://", StringMatchType.Equals)
                .addUsingString("_id=?", StringMatchType.Equals)
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        for (MethodData md : methods)
        {
            Logger.i("Hooking: " + md.getMethodInstance(classLoader));
            module.hook(md.getMethodInstance(classLoader)).intercept(chain ->
            {
                if (Config.getDisableZaloTracking())
                {
                    Object enumValue = chain.getArg(0);
                    if (enumValue != null)
                    {
                        String enumName = ((Enum<?>)enumValue).name();
                        if (enumName.equals("ZALO_LOG"))
                            return "";
                    }
                }
                return chain.proceed();
            });
        }

        methods = bridge.findMethod(FindMethod.create()
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
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        for (MethodData md : methods)
        {
            Logger.i("Hooking: " + md.getMethodInstance(classLoader));
            module.hook(md.getMethodInstance(classLoader)).intercept(chain ->
            {
                if (Config.getDisableZaloTracking())
                {
                    for (Field field : chain.getThisObject().getClass().getDeclaredFields())
                    {
                        if (Modifier.isStatic(field.getModifiers()))
                            continue;
                        String fieldValueStr = field.get(chain.getThisObject()).toString();
                        if (fieldValueStr.contains("/tracking-v3"))
                            return null;
                    }
                }
                return chain.proceed();
            });
        }
        methods = bridge.findMethod(FindMethod.create()
        .matcher(MethodMatcher.create()
            .modifiers(Modifier.PUBLIC | Modifier.FINAL)
            .returnType("void")
            .paramCount(4)
            .paramTypes(int.class, byte[].class, int.class, null)
            .addUsingString("/tracking", StringMatchType.Equals)
            .addUsingString("STR_URL_UPLOAD_ZALO_TRACKING_BY_TYPE_%d", StringMatchType.Equals)
        ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        for (MethodData md : methods)
        {
            Logger.i("Hooking: " + md.getMethodInstance(classLoader));
            module.hook(md.getMethodInstance(classLoader)).intercept(chain ->
            {
                if (Config.getDisableZaloTracking())
                    return null;
                return chain.proceed();
            });
        }
        methods = bridge.findMethod(FindMethod.create()
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
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        for (MethodData methodData : methods)
        {
            List<UsingFieldData> usedFields = methodData.getUsingFields();
            if (usedFields.isEmpty())
                continue;
            UsingFieldData usedField = usedFields.get(0);
            Field field = usedField.getField().getFieldInstance(classLoader);
            if (!field.getType().equals(int[].class))
                continue;
            if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isStatic(field.getModifiers()) || !Modifier.isFinal(field.getModifiers()))
                continue;
            Method method = methodData.getMethodInstance(classLoader);
            Logger.i("Hooking: " + method);
            module.hook(method).intercept(chain ->
            {
                if (!Config.getBlockQoSEndpoints())
                    return chain.proceed();
                Enum<?> enumValue = (Enum<?>)chain.getArg(0);
                int ord = enumValue.ordinal();
                Object value = field.get(null);
                if (value == null)
                    return chain.proceed();
                return switch (((int[])value)[ord])
                {
                    case 154, 110, 115, 109, 108, 107, 106, 105, 145 -> "";
                    default -> chain.proceed();
                };
            });
        }
    }
}
