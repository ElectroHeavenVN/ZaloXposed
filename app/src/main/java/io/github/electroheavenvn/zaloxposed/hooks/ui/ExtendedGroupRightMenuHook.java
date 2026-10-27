package io.github.electroheavenvn.zaloxposed.hooks.ui;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;
import io.github.electroheavenvn.zaloxposed.utilities.Utils;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

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
        List<Method> methods = DexKitCache.GetMethods("ExtendedGroupRightMenu_GetItems", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("java.util.ArrayList")
                .paramCount(0)
                .addUsingString("tip.csc.rightmenu.addmember", StringMatchType.Equals)
                .addUsingNumber(58) // change owner
                .addUsingNumber(40) // disband
                .addUsingNumber(74) // about communities
                .addUsingNumber(75) // upgrade to community
                // Wrong subtype (for user, not group), maybe adding back disappearing messages for user?
                // .addUsingString("E2EE_BETA", StringMatchType.Equals)
                // .addUsingString("ENABLE_TOOL_STORAGE_RIGHT_MENU_${UserID}", StringMatchType.Equals)
            )), classLoader);
        if (methods.isEmpty())
        {
            Logger.e("Target method not found 1");
            return;
        }
        if (methods.size() > 1)
            Logger.w("Multiple target methods found 1");
        List<Method> methods2 = DexKitCache.GetMethods("ExtendedGroupRightMenu_GetItemsNewVer", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .name(methods.get(0).getName()) // override the same method in the parent class
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("java.util.ArrayList")
                .paramCount(0)
                .addUsingString("<this>", StringMatchType.Equals)
                .addUsingString("<set-?>", StringMatchType.Equals)
                .addUsingNumber(84)
                .addUsingNumber(82)
                .addUsingNumber(10)
                .addUsingNumber(0)
                .addUsingNumber(2)
                .addInvoke("Landroid/text/SpannableString;-><init>(Ljava/lang/CharSequence;)V")
            )), classLoader);
        if (methods2.isEmpty())
        {
            Logger.e("Target method not found 2");
            return;
        }
        methods.addAll(methods2);
        for (Method method : methods)
        {
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
                result.add(ctor.newInstance(68, Utils.GetDrawableResourceIdByName("ic_countdown"), Utils.GetResourceIdByName("str_disappear_messages"), false, true));
                result.add(ctor.newInstance(23, Utils.GetDrawableResourceIdByName("ic_group"), Utils.GetResourceIdByName("str_nickname_in_group"), true, false));
                result.add(ctor.newInstance(75, Utils.GetDrawableResourceIdByName("ic_right_menu_chevron_double_up_line_24"), Utils.GetResourceIdByName("str_right_menu_upgrade_to_community"), true, false));
                result.add(ctor.newInstance(40, Utils.GetDrawableResourceIdByName("ic_disband"), Utils.GetResourceIdByName("str_community_settings_delete_community"), true, false));
                return result;
            });
        }
    }
}