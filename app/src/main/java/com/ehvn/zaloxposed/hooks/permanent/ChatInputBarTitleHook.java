package com.ehvn.zaloxposed.hooks.permanent;

import android.widget.TextView;

import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class ChatInputBarTitleHook extends BaseHook
{
    private Field actionEditTextField = null;

    @Override
    public void hook() throws Throwable
    {
        Class<?> chatInputBar = Class.forName("com.zing.zalo.ui.chat.widget.inputbar.ChatInputBar", false, classLoader);
        Class<?> actionEditText = Class.forName("com.zing.zalo.uicontrol.ActionEditText", false, classLoader);
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
            Logger.e("Target field not found");
            return;
        }
        Logger.i("Hooking: " + setupEditTextState);
        module.hook(setupEditTextState).intercept(chain ->
        {
            Object result = chain.proceed();
            TextView actionEditTextView = (TextView)actionEditTextField.get(chain.getThisObject());
            if (actionEditTextView == null)
                return result;
            if (actionEditTextView.getHint().length() <= 0)
                return result;
            actionEditTextView.setHint("ZaloXposed by ElectroHeavenVN");
            return result;
        });
    }
}