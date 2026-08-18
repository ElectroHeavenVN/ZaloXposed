package com.ehvn.zaloxposed.hooks;

import com.ehvn.zaloxposed.utilities.Logger;
import com.ehvn.zaloxposed.utilities.Utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.ClassData;
import org.luckypray.dexkit.result.ClassDataList;
import org.luckypray.dexkit.result.MethodData;
import org.luckypray.dexkit.result.UsingFieldData;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class TestHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        // List<ClassData> msgClasses = bridge.findClass(FindClass.create()
        //     .matcher(ClassMatcher.create()
        //         .addUsingString("gallery_save_video_detail", StringMatchType.Equals)
        //         .addUsingString("video_download", StringMatchType.Equals)
        //         .addUsingString("currentUserId", StringMatchType.Equals)
        //         .addUsingString("recommened.link", StringMatchType.Equals)
        //         .addUsingString("senderName", StringMatchType.Equals)
        //         .addUsingString("platform", StringMatchType.Equals)
        //         .addUsingString("recommened.user", StringMatchType.Equals)
        //         .addUsingString("l.a.header.only", StringMatchType.Equals)
        //         .addUsingString("l.a.header.full", StringMatchType.Equals)
        //         .addUsingString("description", StringMatchType.Equals)
        //         .addUsingString("recommened.vip", StringMatchType.Equals)
        //         .addUsingString("platform", StringMatchType.Equals)
        //         .addUsingString("globalMsgId", StringMatchType.Equals)
        //         .addUsingString("mentions", StringMatchType.Equals)
        //         .addUsingString("reference", StringMatchType.Equals)
        //     ));
        // if (msgClasses.isEmpty())
        // {
        //     Logger.e("Target class not found");
        //     return;
        // }
        // Class<?> msgClass = msgClasses.get(0).getInstance(classLoader);
        // Logger.i("Found class: " + msgClass);
        // Class<?> chatRow = Class.forName("com.zing.zalo.ui.chat.chatrow.ChatRow", false, classLoader);

        // for (Field field : chatRow.getDeclaredFields())
        // {
        //     if (field.getType().equals(msgClass))
        //     {
        //         msgField = field;
        //         break;
        //     }
        // }
        // if (msgField == null)
        // {
        //     Logger.e("Target field not found");
        //     return;
        // }
        // getRefBackgroundDrawableMethod = chatRow.getDeclaredMethod("getRefBackgroundDrawable");
        // getRefBackgroundDrawableMethod.setAccessible(true);
        // List<MethodData> methods = bridge.findMethod(FindMethod.create()
        //     .matcher(MethodMatcher.create()
        //         .declaredClass(msgClass)
        //         .modifiers(Modifier.PUBLIC | Modifier.FINAL)
        //         .returnType("boolean")
        //         .paramCount(0)
        //         .addUsingNumber(36)
        //     ));
        // if (methods.isEmpty())
        // {
        //     Logger.e("Target method not found");
        //     return;
        // }
        // msgTypeField = methods.get(0).getUsingFields().get(0).getField().getFieldInstance(classLoader);
        // Logger.i("Field: " + msgTypeField);
        // Method drawMethod = chatRow.getDeclaredMethod("L", Canvas.class, int.class, int.class, int.class, int.class);
        // Logger.i("Hooking: " + drawMethod);
        // module.hook(drawMethod).intercept(chain ->
        // {
        //     try 
        //     {
        //         Object thisObj = chain.getThisObject();
        //         Object msg = msgField.get(thisObj);
        //         if (msg == null)
        //             return chain.proceed();
        //         int type = (int)msgTypeField.get(msg);
        //         String msgStr = msg.getClass().getField("f").get(msg).toString();
        //         Logger.i("drawMethod called with msg type: " + type + ", msg: " + msgStr);
        //         if (type != 36 && type != 33)
        //             return chain.proceed();
        //         Logger.i("changing background alpha");
        //         Drawable drawable = (Drawable)getRefBackgroundDrawableMethod.invoke(thisObj);
        //         return null;
        //     }
        //     catch (Exception e)
        //     {
        //         Logger.e("Error in drawMethod hook: " + e);
        //     }
        //     return chain.proceed();
        // });

        // Constructor<?> ctor = JSONObject.class.getConstructor(String.class);
        // Logger.i("Hooking: " + ctor);
        // module.hook(ctor).intercept(chain ->
        // {
        //     Object result = chain.proceed();
        //     Logger.i("JSONObject constructor called with string: " + chain.getArg(0));
        //     Logger.i("stack trace: " + Utils.GetStackTrace());
        //     return result;
        // });
        // Method printStackTrace = Throwable.class.getDeclaredMethod("printStackTrace");
        // module.hook(printStackTrace).intercept(chain ->
        // {
        //     Object result = chain.proceed();
        //     Logger.e("Exception:");
        //     Logger.e((Throwable)chain.getThisObject());
        //     Logger.i("stack trace: " + Utils.GetStackTrace());
        //     return result;
        // });
    }
}