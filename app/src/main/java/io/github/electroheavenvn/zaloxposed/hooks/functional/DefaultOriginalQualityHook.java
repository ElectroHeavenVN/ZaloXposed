package io.github.electroheavenvn.zaloxposed.hooks.functional;

import android.os.Bundle;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;

public class DefaultOriginalQualityHook extends BaseHook
{
    private static Field cameraInputQuality;
    // private static Field cameraInputMode;

    @Override
    public void hook() throws Throwable
    {
        Method getCurrentQuality = DexKitCache.GetMethod("DefaultOriginalQuality_GetCurrentQuality", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("int")
                .paramCount(0)
                .addInvoke(MethodMatcher.create()
                    .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                    .returnType("int")
                    .paramCount(0)
                    .addUsingString("LAST_SELECTION_MEDIA_QUALITY_", StringMatchType.Equals))
                .usingNumbers(1, 2, 3, 4, 5)
            )), classLoader);
        if (getCurrentQuality == null)
            return;
        Logger.h(getCurrentQuality);
        module.hook(getCurrentQuality).intercept(chain ->
        {
            if (!Config.getUnlockZCloud())
                return chain.proceed();
            if (!Config.getDefaultOriginalQuality())
                return chain.proceed();
            return 2;
        });
        Method openView = DexKitCache.GetMethod("DefaultOriginalQuality_OpenView", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC)
                .returnType("void")
                .paramCount(6)
                .paramTypes(Class.class, Bundle.class, int.class, String.class, int.class, boolean.class)
                .addUsingString("Cannot show ZaloView, return. mParentZaloView.mInnerView is null", StringMatchType.Equals)
                .addUsingNumber(16908290)
            )), classLoader);
        if (openView == null)
            return;
        Logger.h(openView);
        module.hook(openView).intercept(chain ->
        {
            if (!Config.getUnlockZCloud())
                return chain.proceed();
            if (!Config.getDefaultOriginalQualityShare())
                return chain.proceed();
            Class<?> cl = (Class<?>)chain.getArg(0);
            if ("com.zing.zalo.ui.zviews.share.ShareView".equals(cl.getTypeName()))
            {
                Bundle bundle = (Bundle)chain.getArg(1);
                bundle.putBoolean("bol_extra_photo_original", true);
                bundle.putBoolean("bol_extra_photo_hd", true);
            }
            return chain.proceed();
        });
        List<Method> methods = DexKitCache.GetMethods("DefaultOriginalQuality_ShareView_Bundles", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.zviews.share.ShareView")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("void")
                .paramCount(1)
                .paramTypes(Bundle.class)
                .addUsingString("bol_extra_photo_hd", StringMatchType.Equals)
                .addUsingString("bol_extra_photo_original", StringMatchType.Equals)
            )), classLoader);
        if (methods.isEmpty())
        {
            Logger.e("Target methods not found 1");
            return;
        }
        for (Method method : methods)
        {
            Logger.h(method);
            module.hook(method).intercept(chain ->
            {
                if (!Config.getUnlockZCloud())
                    return chain.proceed();
                if (!Config.getDefaultOriginalQualityShare())
                    return chain.proceed();
                Bundle bundle = (Bundle)chain.getArg(0);
                bundle.putBoolean("bol_extra_photo_original", true);
                bundle.putBoolean("bol_extra_photo_hd", true);
                return chain.proceed();
            });
        }
        Method sendImageFromCamera = DexKitCache.GetMethod("DefaultOriginalQuality_SendImageFromCamera", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("void")
                .paramCount(9)
                .paramTypes(String.class, String.class, boolean.class, boolean.class, String.class, String.class, int.class , long.class, null)
                .addUsingString("", StringMatchType.Equals)
                .usingNumbers(-1, 0, 1, 3, 4)
                .declaredClass(ClassMatcher.create()
                    .addUsingString("source", StringMatchType.Equals)
                    .addUsingString("keyword", StringMatchType.Equals)
                    .addUsingString("https?://", StringMatchType.Equals)
                    .addUsingString("contentId", StringMatchType.Equals)
                    .addUsingString("width", StringMatchType.Equals)
                    .addUsingString("height", StringMatchType.Equals)
                    .addUsingString("chat_forward", StringMatchType.Equals)
                    .addUsingString("fileSize", StringMatchType.Equals)
                    .addUsingString("fileExt", StringMatchType.Equals)
                    .addUsingString("invalid original rich content. Must be ChatRichContentPhoto", StringMatchType.Equals))
            )), classLoader);
        if (sendImageFromCamera == null)
            return;
        Logger.h(sendImageFromCamera);
        module.hook(sendImageFromCamera).intercept(chain ->
        {
            if (!Config.getUnlockZCloud())
                return chain.proceed();
            if (!Config.getDefaultOriginalQualityCamera())
                return chain.proceed();
            String source = (String)chain.getArg(5);
            if (!"chat_send".equals(source))
                return chain.proceed();
            Object[] args = chain.getArgs().toArray();
            args[2] = args[3] = true;
            return chain.proceed(args);
        });
        // force real original image from camera (disable compression)
        cameraInputQuality = DexKitCache.GetFieldRaw("DefaultOriginalQuality_CameraInputParams_Quality", classLoader);
        //cameraInputMode = DexKitCache.GetFieldRaw("DefaultOriginalQuality_CameraInputParams_Mode", classLoader);
        // if (cameraInputQuality == null || cameraInputMode == null)
        if (cameraInputQuality == null)
        {
            List<MethodData> methodDataList = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.camera.common.models.CameraInputParams")
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("com.zing.zalo.camera.common.models.CameraInputParams")
                .paramCount(1)
                .paramTypes(String.class)
                .addUsingNumber(7)
                .addUsingNumber(3)  // quality
            ));
            if (methodDataList.isEmpty())
            {
                Logger.e("Target method not found 2");
                return;
            }
            if (methodDataList.size() > 1)
                Logger.w("Multiple target methods found");
            Object cameraInputParams = methodDataList.get(0).getMethodInstance(classLoader).invoke(null, "12345678");
            if (cameraInputParams == null)
                return;
            for (Field intField : cameraInputParams.getClass().getDeclaredFields())
            {
                if (intField.getType() != int.class)
                    continue;
                if (Modifier.isStatic(intField.getModifiers()))
                    continue;
                boolean accessible = intField.isAccessible();
                intField.setAccessible(true);
                if (3 == intField.getInt(cameraInputParams))
                {
                    cameraInputQuality = intField;
                    DexKitCache.SetFieldRaw("DefaultOriginalQuality_CameraInputParams_Quality", cameraInputQuality);
                    // continue;
                    break;
                }
                // if (7 == intField.getInt(cameraInputParams))
                // {
                //     cameraInputMode = intField;
                //     DexKitCache.SetFieldRaw("DefaultOriginalQuality_CameraInputParams_Mode", cameraInputQuality);
                //     continue;
                // }
                intField.setAccessible(accessible);
            }
        }
        // if (cameraInputQuality == null || cameraInputMode == null)
        if (cameraInputQuality == null)
            return;
        Method loadFromCameraInputParams = DexKitCache.GetMethod("DefaultOriginalQuality_SendImageFromCamera", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.camera.ZaloCameraView")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("void")
                .paramCount(1)
                .paramTypes("com.zing.zalo.camera.common.models.CameraInputParams")
                .usingNumbers(0, 1, 2, 3, 4, 5)
            )), classLoader);
        if (loadFromCameraInputParams == null)
            return;
        Logger.h(loadFromCameraInputParams);
        module.hook(loadFromCameraInputParams).intercept(chain ->
        {
            if (!Config.getUnlockZCloud())
                return chain.proceed();
            if (!Config.getDefaultOriginalQualityCamera())
                return chain.proceed();
            Object inputParams = chain.getArg(0);
            if (inputParams == null)
                return chain.proceed();
            // if (1 == cameraInputMode.getInt(inputParams))
            cameraInputQuality.setInt(inputParams, 5);
            return chain.proceed(new Object[] { inputParams });
        });
        Method sendImg = DexKitCache.GetMethod("DefaultOriginalQuality_SendImage", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC)
                .returnType("void")
                .paramCount(10)
                .paramTypes(String.class, String.class, boolean.class, boolean.class, String.class, null, null, int.class, null, null)
                .addInvoke("Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z")
            )), classLoader);
        if (sendImg == null)
            return;
        Logger.h(sendImg);
        module.hook(sendImg).intercept(chain ->
        {
            if (!Config.getUnlockZCloud())
                return chain.proceed();
            if (!Config.getDefaultOriginalQualityPasted())
                return chain.proceed();
            Integer src = (Integer)chain.getArg(7);
            if (50 != src)
                return chain.proceed();
            Object[] args = chain.getArgs().toArray();
            args[2] = args[3] = true;
            return chain.proceed(args);
        });
        // Method v5 = Class.forName("vrv", false, classLoader).getDeclaredMethod("V5", Intent.class, int.class);
        // Logger.h(v5);
        // module.hook(v5).intercept(chain ->
        // {
        //     Intent intent = (Intent)chain.getArg(0);
        //     if (intent != null)
        //     {
        //         intent.putExtra("extra_result_is_hd", true);
        //         intent.putExtra("extra_result_is_original", true);
        //     }
        //     return chain.proceed();
        // });
    }
}