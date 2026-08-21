package com.ehvn.zaloxposed.hooks.ads;

import android.view.View;
import android.view.ViewGroup;

import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Config;
import com.ehvn.zaloxposed.utilities.Logger;
import com.ehvn.zaloxposed.utilities.Utils;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.FieldMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class HideAdsHook extends BaseHook
{ 
    @Override
    public void hook() throws Throwable
    { 
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(2)
                .paramTypes("android.view.ViewGroup", "int")
                .addUsingString("btn_footer_add_multi_conversation_label", StringMatchType.Equals)
                .addUsingString("TAB_MSG_ITEM_MINI_CHAT_KEY", StringMatchType.Equals)
                .addUsingString("context", StringMatchType.Equals)
                .addUsingField(FieldMatcher.create().name("btn_see_more"))
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
                Object result = chain.proceed(); 
                int viewType = (Integer)chain.getArg(1);
                if (
                    (Config.getHideMediaBox() && viewType == 2) || 
                    (Config.getHideBizBox() && viewType == 3) || 
                    (Config.getHideZInstantAds() && viewType == 27)
                )
                {
                    for (Field field : Utils.GetAllFields(result.getClass()))
                    {
                        if (View.class != field.getType())
                            continue;
                        Utils.HideView((View)field.get(result));
                    }
                }
                return result;
            });
        }
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.social.presentation.timeline.view.TimelineView")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("void")
                .paramCount(2)
                .paramTypes(null, "android.view.View")
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
                Object result = chain.proceed();
                if (Config.getHideFeedItemZInstantAds())
                {
                    View view = (View)chain.getArg(1);
                    if (view == null)
                        return result;
                    Class<?> viewClass = view.getClass();
                    if (viewClass.getName().equals("com.zing.zalo.social.presentation.timeline.components.ads.FeedItemZInstantAds"))
                        Utils.HideView(view);
                }
                return result;
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
                if (!Config.getBlockAdsEndpoints())
                    return chain.proceed();
                Enum<?> enumValue = (Enum<?>)chain.getArg(0);
                int ord = enumValue.ordinal();
                return switch (ord)
                {
                    case 147, 161 -> "";
                    default -> chain.proceed();
                };
            });
        }
    }
}
