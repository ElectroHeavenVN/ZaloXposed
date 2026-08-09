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
                        View view = (View)field.get(result);
                        if (view == null)
                            continue;
                        ViewGroup.LayoutParams layout = view.getLayoutParams();
                        layout.height = 0;
                        view.setVisibility(View.GONE);
                    }
                    return result;
                }
                return result;
            });
        } 
    }
}
