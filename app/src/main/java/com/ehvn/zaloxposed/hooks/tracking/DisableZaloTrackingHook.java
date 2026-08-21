package com.ehvn.zaloxposed.hooks.tracking;

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

public class DisableZaloTrackingHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
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
                if (!Config.getBlockQoSEndpoints())
                    return chain.proceed();
                Enum<?> enumValue = (Enum<?>)chain.getArg(0);
                int ord = enumValue.ordinal();
                return switch (ord)
                {
                    case 154, 110, 109, 108, 107, 106, 105, 145 -> "";
                    default -> chain.proceed();
                };
            });
        }
    }
}
