package com.ehvn.zaloxposed.hooks.permanent;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Logger;

import org.json.JSONObject;
import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.ClassData;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class AntiRecallDeleteChatRowHook extends BaseHook
{
    Field msgField = null;
    Field msgTypeField = null;
    Field quoteField = null;
    Field quoteDisplayNameField = null;
    Field quoteAttrField = null;

    @Override
    public void hook() throws Throwable
    {
        List<ClassData> msgClasses = bridge.findClass(FindClass.create()
            .matcher(ClassMatcher.create()
                .addUsingString("gallery_save_video_detail", StringMatchType.Equals)
                .addUsingString("video_download", StringMatchType.Equals)
                .addUsingString("currentUserId", StringMatchType.Equals)
                .addUsingString("recommened.link", StringMatchType.Equals)
                .addUsingString("senderName", StringMatchType.Equals)
                .addUsingString("platform", StringMatchType.Equals)
                .addUsingString("recommened.user", StringMatchType.Equals)
                .addUsingString("l.a.header.only", StringMatchType.Equals)
                .addUsingString("l.a.header.full", StringMatchType.Equals)
                .addUsingString("description", StringMatchType.Equals)
                .addUsingString("recommened.vip", StringMatchType.Equals)
                .addUsingString("platform", StringMatchType.Equals)
                .addUsingString("globalMsgId", StringMatchType.Equals)
                .addUsingString("mentions", StringMatchType.Equals)
                .addUsingString("reference", StringMatchType.Equals)
            ));
        if (msgClasses.isEmpty())
        {
            Logger.e("Target class not found");
            return;
        }
        Class<?> msgClass = msgClasses.get(0).getInstance(classLoader);
        Class<?> chatRow = Class.forName("com.zing.zalo.ui.chat.chatrow.ChatRow", false, classLoader);
        for (Field field : chatRow.getDeclaredFields())
        {
            if (field.getType().equals(msgClass))
            {
                msgField = field;
                break;
            }
        }
        if (msgField == null)
        {
            Logger.e("Target field not found");
            return;
        }
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(msgClass)
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("boolean")
                .paramCount(0)
                .addUsingNumber(36)
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        msgTypeField = methods.get(0).getUsingFields().get(0).getField().getFieldInstance(classLoader); 
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(chatRow)
                .modifiers(Modifier.PUBLIC)
                .returnType("void")
                .paramCount(5)
                .paramTypes("android.graphics.Canvas", "int", "int", "int", "int")
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        Method drawMethod = methods.get(0).getMethodInstance(classLoader);
        Logger.i("Hooking: " + drawMethod);
        module.hook(drawMethod).intercept(chain ->
        {
            Object thisObj = chain.getThisObject();
            Object msg = msgField.get(thisObj);
            if (msg == null)
                return chain.proceed();
            if (msgTypeField.get(msg) != Integer.valueOf(0))
                return chain.proceed();
            String msgStr = msg.toString();
            if (
                !msgStr.startsWith("ChatContent{msg='Message recalled', ") && 
                !msgStr.startsWith("ChatContent{msg='Message deleted', ") &&

                !msgStr.matches("ChatContent\\{msg='[0-9]+ messages dele.*?', .*") &&
                !msgStr.matches("ChatContent\\{msg='[0-9]+ messages reca.*?', .*") // toString limited msg content to ~18-20 chars
                )
                return chain.proceed();
            Object quote = null;
            if (quoteField == null)
            {
                for (Field f : msg.getClass().getFields())  // public fields
                {
                    Class<?> type = f.getType();
                    if (type.isPrimitive() || String.class.equals(type))
                        continue;
                    Object value = f.get(msg);
                    if (value == null)
                        continue;
                    String valueStr = value.toString();
                    if (!valueStr.startsWith("replySenderUid="))
                        continue;
                    if (!valueStr.contains("replySenderGlobalUid="))
                        continue;
                    quoteField = f;
                    quote = value;
                    break;
                }
            }
            else
            {
                quote = quoteField.get(msg);
            }
            if (quote == null)
                return chain.proceed();
            if (quoteDisplayNameField == null || quoteAttrField == null)
            {
                int count = 0;
                for (Field f : quote.getClass().getFields())  // public fields
                {
                    Class<?> type = f.getType();
                    if (type.isPrimitive() || !String.class.equals(type))
                        continue;
                    String value = (String) f.get(quote);
                    if (value == null)
                        continue;
                    if (value.equals("ZaloXposed by ElectroHeavenVN"))
                    {
                        quoteDisplayNameField = f;
                        count++;
                        continue;
                    } 
                    if (value.endsWith(",\"generatedBy\":\"AntiRecallDeleteHook\"}"))
                    {
                        try
                        {
                            JSONObject obj = new JSONObject(value);
                            quoteAttrField = f;
                            count++;
                        }
                        catch (Exception ignored) { }
                    }
                }
                if (count != 2)
                    return chain.proceed();
            }
            else
            {
                if (!"ZaloXposed by ElectroHeavenVN".equals(quoteDisplayNameField.get(quote)))
                    return chain.proceed();
                Object attr = quoteAttrField.get(quote);
                if (attr == null)
                    return chain.proceed();
                if (!attr.toString().endsWith(",\"generatedBy\":\"AntiRecallDeleteHook\"}"))
                    return chain.proceed();
            }
            Canvas canvas = (Canvas)chain.getArg(0);
            int left = (int)chain.getArg(1);
            int top = (int)chain.getArg(2);
            int right = (int)chain.getArg(3);
            int bottom = (int)chain.getArg(4);
            RectF rect = new RectF(left, top, right, bottom);
            int w = right - left;
            int h = bottom - top;
            Bitmap bmp = Bitmap.createBitmap(Math.max(w, 1), Math.max(h, 1), Bitmap.Config.ARGB_8888);
            Canvas fakeCanvas = new Canvas(bmp);
            fakeCanvas.translate(-left, -top);
            Object result = chain.proceed(new Object[]{fakeCanvas, left, top, right, bottom});
            int sampleColor = bmp.getPixel(w / 4, h / 4);
            bmp.recycle();
            double lum = (0.299 * Color.red(sampleColor) + 0.587 * Color.green(sampleColor) + 0.114 * Color.blue(sampleColor)) / 255.0;
            boolean darkTheme = lum < 0.5;
            Paint fill = new Paint();
            fill.setStyle(Paint.Style.FILL);
            fill.setColor(darkTheme ? 0xB0400000 : 0xDDFFDDDD);
            fill.setAntiAlias(true);
            canvas.drawRoundRect(rect, 24f, 24f, fill);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4f);
            paint.setColor(0xFFE53935);
            paint.setAntiAlias(true);
            paint.setPathEffect(new DashPathEffect(new float[]{20f, 12f}, 0f));
            canvas.drawRoundRect(rect, 24f, 24f, paint);
            return result;
        });
    }
}