package com.ehvn.zaloxposed.hooks.permanent;

import android.view.View;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.ehvn.zaloxposed.MorpheConstants;
import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Logger;
import com.ehvn.zaloxposed.utilities.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

@SuppressWarnings("unused")
public class WatermarkHook extends BaseHook
{
    private Field actionEditTextField = null;

    @Override
    public void hook() throws Throwable
    {
        hookChatInputBar();
    }

    private void hookChatInputBar() throws ClassNotFoundException, NoSuchMethodException
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
        module.hook(setupEditTextState).setPriority(15).intercept(chain ->
        {
            Object result = chain.proceed();
            TextView actionEditTextView = (TextView)actionEditTextField.get(chain.getThisObject());
            if (actionEditTextView == null)
                return result;
            if (actionEditTextView.getHint().length() <= 0)
                return result;
            actionEditTextView.setHint(MorpheConstants.getModuleName() + " by ElectroHeavenVN");
            return result;
        });
    }
}