package com.ehvn.zaloxposed.hooks.permanent;

import android.widget.TextView;

import com.ehvn.zaloxposed.hooks.BaseHook;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

@SuppressWarnings("unused")
public class ChatInputBarTitleHook extends BaseHook
{
    private Field actionEditTextField = null;

    @Override
    public void hook() throws Throwable
    {
        Class<?> chatInputBar = Class.forName("com.zing.zalo.ui.chat.widget.inputbar.ChatInputBar", false, lpparam.classLoader);
        Class<?> actionEditText = Class.forName("com.zing.zalo.uicontrol.ActionEditText", false, lpparam.classLoader);
        Method setupEditTextState = chatInputBar.getDeclaredMethod("setupEditTextState", int.class);
        for (Field f : chatInputBar.getDeclaredFields())
        {
            if (!actionEditText.isAssignableFrom(f.getType()))
                continue;
            actionEditTextField = f;
            break;
        }
        if (actionEditTextField == null)
        {
            log("Target field not found");
            return;
        }
        log("Hooking: " + setupEditTextState);
        XposedBridge.hookMethod(setupEditTextState, new XC_MethodHook()
        {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable
            {
                TextView actionEditTextView = (TextView)actionEditTextField.get(param.thisObject);
                if (actionEditTextView == null)
                    return;
                if (actionEditTextView.getHint().length() <= 0)
                    return;
                actionEditTextView.setHint("ZaloXposed by ElectroHeavenVN");
            }
        });
    }
}