package com.ehvn.zaloxposed.hooks.ui;

import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Config;
import com.ehvn.zaloxposed.utilities.Logger;
import com.ehvn.zaloxposed.utilities.Utils;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ExtendedGroupRightMenuHook extends BaseHook
{
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void hook() throws Throwable
    {
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("java.util.ArrayList")
                .paramCount(0)
                .addUsingString("tip.csc.rightmenu.addmember", StringMatchType.Equals)
                .addUsingString("E2EE_BETA", StringMatchType.Equals)
                .addUsingString("tip.group.summary", StringMatchType.Equals)
                .addUsingString("ENABLE_TOOL_STORAGE_RIGHT_MENU_${UserID}", StringMatchType.Contains)
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
                ArrayList result = (ArrayList)chain.proceed();
                if (!Config.getEnableExtendedGroupRightMenu())
                    return result;
                Object element = result.get(result.size() - 1);
                Class<?> clazz = element.getClass();
                Constructor<?> ctor = clazz.getConstructor(int.class, int.class, int.class, boolean.class, boolean.class);
                // [id] [drawable icon] [title] [show line separator] [show blank separator]
                result.add(ctor.newInstance(67, Utils.GetDrawableResourceIdByName("ic_trophy"), Utils.GetResourceIdByName("str_group_summary"), false, true));
                result.add(ctor.newInstance(23, Utils.GetDrawableResourceIdByName("ic_group"), Utils.GetResourceIdByName("str_nickname_in_group"), true, false));
                result.add(ctor.newInstance(75, Utils.GetDrawableResourceIdByName("ic_right_menu_chevron_double_up_line_24"), Utils.GetResourceIdByName("str_right_menu_upgrade_to_community"), true, false));
                //result.add(ctor.newInstance(81, Utils.GetDrawableResourceIdByName("zds_ic_shield_line_24"), Utils.GetResourceIdByName("chat_protection_title"), true, false));
                result.add(ctor.newInstance(40, Utils.GetDrawableResourceIdByName("ic_disband"), Utils.GetResourceIdByName("str_community_settings_delete_community"), true, false));
                return result;
            });
        }
    }
}