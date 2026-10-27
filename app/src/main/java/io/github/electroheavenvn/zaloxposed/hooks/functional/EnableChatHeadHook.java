package io.github.electroheavenvn.zaloxposed.hooks.functional;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import com.android.tools.smali.dexlib2.Opcode;
import com.android.tools.smali.dexlib2.iface.instruction.Instruction;
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction;
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction;
import com.android.tools.smali.dexlib2.iface.reference.FieldReference;

import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.FieldMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;
import io.github.electroheavenvn.zaloxposed.utilities.Utils;
import io.github.libxposed.api.XposedInterface;

// It would be great if we can patch dex...
public class EnableChatHeadHook extends BaseHook
{
    static Field isAdr10OrOld = null;
    static Field isAdr11OrNew = null;
    static Field chatHeadUnavailable = null;
    static boolean chatHeadUnavailableOriginalValue = false;
    static Class<?> androidVersionCheckClass = null;
    static Class<?> classContainsChatHeadUnavailableConfig = null;
    static boolean hooked = false;

    @Override
    public void hook() throws Throwable
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
            return;
        fixMiniChatAndroid13();
        // Very long method that do something with JSON data, related to domains
        Method targetMethod = DexKitCache.GetMethod("EnableChatHead_ClassContainsChatHeadUnavailableConfig", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .returnType("void")
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .paramCount(0)
                .addUsingString("ENABLE_SOCKET_UPLOAD_FOR_VIDEO", StringMatchType.Equals)
                .addUsingString("MALICIOUS_MIME_TYPE", StringMatchType.Equals)
                .addUsingString("NOTIFICATION_MANAGER_CONFIG", StringMatchType.Equals)
                .addUsingString("ACTION_FOR_SKIP_JUMP_DOMAIN", StringMatchType.Equals)
            )), classLoader);
        if (targetMethod == null)
            return;
        classContainsChatHeadUnavailableConfig = targetMethod.getDeclaringClass();
        androidVersionCheckClass = DexKitCache.GetClass("EnableChatHead_AndroidVersionCheckClass", () -> bridge.findClass(FindClass.create()
            .matcher(ClassMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.ABSTRACT)
                .addField(FieldMatcher.create()
                    .modifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)
                    .type(boolean.class))
                .addMethod(MethodMatcher.create()
                    .name("<clinit>")
                    .addUsingNumber(26)
                    .addUsingNumber(29)
                    .addUsingNumber(30)
                    .addUsingNumber(33)
                    .addUsingField(FieldMatcher.create().name("SDK_INT")))
            )), classLoader);
        if (androidVersionCheckClass == null)
            return;
        Logger.i("Hooking [0]: " + targetMethod);
        module.hook(targetMethod).intercept(chain ->
        {
            Object result = chain.proceed();
            Config.addOnConfigChangedListener((key, oldValue, newValue) ->
            {
                if (!Config.KEY_ENABLE_CHAT_HEAD.equals(key))
                    return;
                boolean enabled = (Boolean) newValue;
                try
                {
                    if (enabled)
                    {
                        chatHeadUnavailableOriginalValue = chatHeadUnavailable.getBoolean(null);
                        chatHeadUnavailable.set(null, false);
                    }
                    else
                        chatHeadUnavailable.set(null, chatHeadUnavailableOriginalValue);
                }
                catch (Exception ignored) { }
            });
            if (!Config.getEnableChatHead())
                return result;
            try
            {
                chatHeadUnavailableOriginalValue = chatHeadUnavailable.getBoolean(null);
                chatHeadUnavailable.set(null, false);
            }
            catch (Exception ignored) { }
            return result;
        });
        loadFields();
        doHook(new SpoofAndroidVersionCheckFields());
    }

    private void doHook(SpoofAndroidVersionCheckFields spoofHook) throws NoSuchMethodException, ClassNotFoundException
    {
        if (hooked)
            return;
        List<Method> methods;
        methods = DexKitCache.GetMethods("EnableChatHead_SpoofHook_1", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.settings.SettingMessageV2View")
                .returnType("void")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(0)
                .addUsingString("QUICK_MESSAGE_FEATURE_ENABLE", StringMatchType.Equals)
            )), classLoader);
        if (methods.isEmpty())
            Logger.e("Target method not found 1");
        for (Method method : methods)
        {
            Logger.i("Hooking [1]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        methods = DexKitCache.GetMethods("EnableChatHead_SpoofHook_2", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.maintab.MainTabView")
                .returnType("void")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(0)
                .addUsingString("DEFAULT_REQUEST_CHAT_HEAD_AFTER_INSTALL_AUTO_RESTORE", StringMatchType.Equals)
                .addUsingString("LAST_TIME_SHOW_REMIND_UPDATE_MINI_CHAT_PERMISSION", StringMatchType.Equals)
                .addUsingString("CONFIG_POPUP_BA_PURCHASE_SUCCESS_${UserID}", StringMatchType.Equals)
                .addUsingString("SHOULD_REMIND_UPDATE_MINI_CHAT_PERMISSION", StringMatchType.Equals)
                .addUsingString("LAST_TIME_SHOW_REMIND_UPDATE_MINI_CHAT_PERMISSION", StringMatchType.Equals)
            )), classLoader);
        if (methods.isEmpty())
            Logger.e("Target method not found 2");
        for (Method method : methods)
        {
            Logger.i("Hooking [2]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        methods = DexKitCache.GetMethods("EnableChatHead_SpoofHook_3", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.maintab.group.GroupTabView")
                .returnType("void")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(1)
                .paramTypes("int")
                .addUsingString("name", StringMatchType.Equals)
                .addUsingString("id", StringMatchType.Equals)
                .addUsingField(FieldMatcher.create().name("str_optionM_receiveNotification"))
                .addUsingField(FieldMatcher.create().name("str_optionM_muteConversation"))
                .addUsingField(FieldMatcher.create().name("str_hide_message"))
                .addUsingField(FieldMatcher.create().name("context_menu_item_leave_group"))
                .addUsingField(FieldMatcher.create().name("str_leave_community"))
                .addUsingField(FieldMatcher.create().name("str_open_bubble_chat"))
                .addUsingField(FieldMatcher.create().name("str_open_chat_head"))
            )), classLoader);
        if (methods.isEmpty())
            Logger.e("Target method not found 3");
        for (Method method : methods)
        {
            Logger.i("Hooking [3]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        methods = DexKitCache.GetMethods("EnableChatHead_SpoofHook_4", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.maintab.group.GroupTabView")
                .returnType("void")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(0)
                .addUsingNumber(27)
                .addUsingNumber(36)
                .addUsingNumber(59)
                .addUsingNumber(6074)
                .addUsingField(FieldMatcher.create().name("Companion"))
            )), classLoader);
        if (methods.isEmpty())
            Logger.e("Target method not found 4");
        for (Method method : methods)
        {
            Logger.i("Hooking [4]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        methods = DexKitCache.GetMethods("EnableChatHead_SpoofHook_5", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.maintab.msg.MessagesView")
                .returnType("void")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(0)
                .addUsingNumber(36)
                .addUsingNumber(137)
                .addUsingNumber(5200)
                .addUsingNumber(3000)
                .addUsingString("newMode", StringMatchType.Equals)
                .addUsingString("ConversationLabel", StringMatchType.Equals)
                .addUsingString("checkRetrySyncTabReddot ", StringMatchType.Equals)
                .addUsingField(FieldMatcher.create().name("Companion"))
            )), classLoader);
        if (methods.isEmpty())
            Logger.e("Target method not found 5");
        for (Method method : methods)
        {
            Logger.i("Hooking [5]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        methods = DexKitCache.GetMethods("EnableChatHead_SpoofHook_6", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.maintab.msg.TabMsgContextMenuView")
                .returnType("void")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(1)
                .paramTypes("java.util.ArrayList")
                .addUsingField(Utils.GetDescriptor(isAdr10OrOld))
            )), classLoader);
        if (methods.isEmpty())
            Logger.e("Target method not found 6");
        for (Method method : methods)
        {
            Logger.i("Hooking [6]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        methods = DexKitCache.GetMethods("EnableChatHead_SpoofHook_7", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .returnType("boolean")
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .paramCount(1)
                .paramTypes("int")
                .addUsingString("%s", StringMatchType.Equals)
                .addUsingString("ENABLE_POPUP_QUICK_REPLY_%s", StringMatchType.Equals)
                .addUsingString("my", StringMatchType.Equals)
                .addUsingString("DEACTIVATE_ACCOUNT_SETTING_%s", StringMatchType.Equals)
                .addUsingString("ENABLE_BLOCK_HIDE_PRIVACY_SETTING_${UserID}", StringMatchType.Equals)
            )), classLoader);
        if (methods.isEmpty())
            Logger.e("Target method not found 7");
        for (Method method : methods)
        {
            Logger.i("Hooking [7]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        methods = DexKitCache.GetMethods("EnableChatHead_SpoofHook_8", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .name("run")
                .returnType("void")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(0)
                .addUsingString("System Setting not allow to create Bubbles on Screen", StringMatchType.Equals)
                .addUsingString("ChatView: Open Bubble Chat", StringMatchType.Contains)
            )), classLoader);
        if (methods.isEmpty())
            Logger.e("Target method not found 8");
        for (Method method : methods)
        {
            Logger.i("Hooking [8]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        methods = DexKitCache.GetMethods("EnableChatHead_SpoofHook_9", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .returnType("boolean")
                .modifiers(Modifier.PUBLIC)
                .paramCount(3)
                .paramTypes("com.zing.zalo.control.ContactProfile", null, null)
                .addUsingString("KILL_CHAT_HEAD_COUNT", StringMatchType.Equals)
                .addUsingString("CHAT_HEAD_SPAM_COUNT", StringMatchType.Equals)
                .addUsingString("KILL_CHAT_HEAD_DISABLE_TIME", StringMatchType.Equals)
                .addUsingString("CHAT_HEAD_EXPIRED_SPAM_TIME", StringMatchType.Equals)
                .addUsingString("KILL_CHAT_HEAD_START_TIME", StringMatchType.Equals)
            )), classLoader);
        if (methods.isEmpty())
            Logger.e("Target method not found 9");
        for (Method method : methods)
        {
            Logger.i("Hooking [9]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        // 13(12 now) already included the same method
        // methods = DexKitCache.GetMethods("EnableChatHead_SpoofHook_10", () -> bridge.findMethod(FindMethod.create()
        //     .matcher(MethodMatcher.create()
        //         .name("run")
        //         .returnType("void")
        //         .modifiers(Modifier.PUBLIC | Modifier.FINAL)
        //         .paramCount(0)
        //         .addUsingString("MAIN_CHAT_HEAD", StringMatchType.Equals)
        //         .addUsingString("MiniChatController", StringMatchType.Equals)
        //         .addUsingField(FieldMatcher.create().name("logo_zalo_chathead"))
        //     )), classLoader);
        // if (methods.isEmpty())
        //     Logger.e("Target method not found 10");
        // for (Method method : methods)
        // {
        //     Logger.i("Hooking [10]: " + method);
        //     module.hook(method).intercept(spoofHook);
        // }
        methods = DexKitCache.GetMethods("EnableChatHead_SpoofHook_10", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(ClassMatcher.create()
                    .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                    .addUsingString("MiniChatController", StringMatchType.Equals)
                    .fieldCount(5)
                    .addMethod(MethodMatcher.create()
                        .returnType("boolean")
                        .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                        .paramCount(0)
                        .addInvoke(MethodMatcher.create()
                            .name("canDrawOverlays")
                            .declaredClass("android.provider.Settings"))
                        .addInvoke(MethodMatcher.create()
                            .name("getAppContext")
                            .declaredClass("com.zing.zalo.MainApplication"))
                    )
                )
                .addUsingField(Utils.GetDescriptor(isAdr10OrOld))
            )), classLoader);
        if (methods.isEmpty())
            Logger.e("Target method not found 10");
        for (Method method : methods)
        {
            Logger.i("Hooking [10]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        methods = DexKitCache.GetMethods("EnableChatHead_SpoofHook_11", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .returnType("boolean")
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .paramCount(0)
                .addUsingString("SETTING_ENABLE_CHAT_HEAD_SERVER", StringMatchType.Equals)
            )), classLoader);
        if (methods.isEmpty())
            Logger.e("Target method not found 11");
        for (Method method : methods)
        {
            Logger.i("Hooking [11]: " + method);
            module.hook(method).intercept(chain ->
            {
                if (!Config.getEnableChatHead())
                    return chain.proceed();
                return true;
            });
        }
        methods = DexKitCache.GetMethods("EnableChatHead_SpoofHook_12", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .name("run")
                .returnType("void")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(0)
                .addUsingField(Utils.GetDescriptor(isAdr10OrOld))
                .addInvoke(MethodMatcher.create()
                    .name("run")
                    .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                    .returnType("void")
                    .addUsingString("MiniChatController", StringMatchType.Equals)
                    .addUsingString("ChatHeadManager", StringMatchType.Equals))
                .addInvoke("Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z")
            )), classLoader);
        if (methods.isEmpty())
            Logger.e("Target method not found 12");
        for (Method method : methods)
        {
            Logger.i("Hooking [12]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        methods = DexKitCache.GetMethods("EnableChatHead_SpoofHook_13", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .name("run")
                .returnType("void")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(0)
                .addInvoke("Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z")
                .addUsingField(Utils.GetDescriptor(isAdr10OrOld))
                .addUsingField(FieldMatcher.create().declaredClass("com.zing.zalo.ui.WebViewMPActivity").name("Companion"))
            )), classLoader);
        if (methods.isEmpty())
            Logger.e("Target method not found 13");
        for (Method method : methods)
        {
            Logger.i("Hooking [13]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        methods = DexKitCache.GetMethods("EnableChatHead_SpoofHook_14", () -> bridge.findMethod(FindMethod.create()
        .matcher(MethodMatcher.create()
            .name("run")
            .returnType("void")
            .modifiers(Modifier.PUBLIC | Modifier.FINAL)
            .paramCount(0) 
            .addInvoke("Landroid/widget/Toast;->show()V")
            .addInvoke("Landroid/view/LayoutInflater;->from(Landroid/content/Context;)Landroid/view/LayoutInflater;")
            .addUsingField(Utils.GetDescriptor(isAdr10OrOld))
            .addUsingField(FieldMatcher.create().declaredClass("com.zing.zalo.comm.chathead.bubblechat.ui.ZaloBubbleActivity").name("Companion"))
        )), classLoader);
        if (methods.isEmpty())
            Logger.e("Target method not found 14");
        for (Method method : methods)
        {
            Logger.i("Hooking [14]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        Method method = Class.forName("com.zing.zalo.ui.maintab.group.GroupTabView", false, classLoader).getDeclaredMethod("onActivityResult", int.class, int.class, Intent.class);
        Logger.i("Hooking [-1]: " + method);
        module.hook(method).intercept(spoofHook);
        method = Class.forName("com.zing.zalo.ui.maintab.msg.MessagesView", false, classLoader).getDeclaredMethod("onActivityResult", int.class, int.class, Intent.class);
        Logger.i("Hooking [-2]: " + method);
        module.hook(method).intercept(spoofHook);
        hooked = true;
    }

    private void fixMiniChatAndroid13() throws ClassNotFoundException, NoSuchMethodException
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            return;
        Method method = Class.forName("android.content.ContextWrapper", false, classLoader).getDeclaredMethod("registerReceiver", BroadcastReceiver.class, IntentFilter.class);
        module.hook(method).intercept(chain ->
        {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            for (StackTraceElement e : stack)
            {
                if (!e.getClassName().equals("com.zing.zalo.comm.chathead.minichat.ui.service.MiniChatService"))
                    continue;
                if (!e.getMethodName().equals("onCreate"))
                    continue;
                Method registerReceiver3Args = Context.class.getDeclaredMethod("registerReceiver", BroadcastReceiver.class, IntentFilter.class, int.class);
                registerReceiver3Args.setAccessible(true);
                registerReceiver3Args.invoke(chain.getThisObject(), (BroadcastReceiver) chain.getArg(0), (IntentFilter) chain.getArg(1), Context.RECEIVER_NOT_EXPORTED);
                return null;
            }
            return chain.proceed();
        });
        method = Class.forName("androidx.window.extensions.layout.WindowLayoutComponentImpl", false, classLoader).getDeclaredMethod("addWindowLayoutInfoListener", Context.class, Class.forName("androidx.window.extensions.core.util.function.Consumer", false, classLoader));
        module.hook(method).intercept(new FixWindowLayoutComponentImpl());
    }

    private void loadFields() throws IOException, NoSuchFieldException
    {
        isAdr10OrOld = DexKitCache.GetFieldRaw("EnableChatHead_AndroidVersionCheckClass_IsAdr10OrOld", classLoader);
        isAdr11OrNew = DexKitCache.GetFieldRaw("EnableChatHead_AndroidVersionCheckClass_IsAdr11OrNew", classLoader);
        chatHeadUnavailable = DexKitCache.GetFieldRaw("EnableChatHead_AndroidVersionCheckClass_ChatHeadUnavailable", classLoader);
        if (isAdr10OrOld != null && isAdr11OrNew != null && chatHeadUnavailable != null)
            return;
        ArrayList<Instruction> instructions;
        if (isAdr10OrOld == null || isAdr11OrNew == null)
        {
            instructions = Utils.Disassemble(androidVersionCheckClass, "<clinit>");
            for (Instruction instruction : instructions)
            {
                if (instruction.getOpcode() == Opcode.SPUT_BOOLEAN && instruction instanceof OneRegisterInstruction oneRegisterInstruction)
                {
                    FieldReference field = (FieldReference)((ReferenceInstruction)instruction).getReference();
                    String fieldName = field.getName();
                    if (isAdr10OrOld == null)
                        isAdr10OrOld = androidVersionCheckClass.getDeclaredField(fieldName);
                    else if (isAdr11OrNew == null)
                    {
                        isAdr11OrNew = androidVersionCheckClass.getDeclaredField(fieldName);
                        break;
                    }
                }
            }
            if (isAdr10OrOld == null)
            {
                Logger.e("Target field not found 1");
                return;
            }
            if (isAdr11OrNew == null)
            {
                Logger.e("Target field not found 2");
                return;
            }
            isAdr10OrOld.setAccessible(true);
            isAdr11OrNew.setAccessible(true);
            DexKitCache.SetFieldRaw("EnableChatHead_AndroidVersionCheckClass_IsAdr10OrOld", isAdr10OrOld);
            DexKitCache.SetFieldRaw("EnableChatHead_AndroidVersionCheckClass_IsAdr11OrNew", isAdr11OrNew);
        }
        if (chatHeadUnavailable == null)
        {
            instructions = Utils.Disassemble(classContainsChatHeadUnavailableConfig, "<clinit>");
            for (int i = instructions.size() - 1; i >= 1; i--)
            {
                Instruction instruction = instructions.get(i);
                Instruction prevInstruction = instructions.get(i - 1);
                if (instruction.getOpcode() == Opcode.SPUT_BOOLEAN && instruction instanceof OneRegisterInstruction oneRegisterInstruction
                    && prevInstruction.getOpcode() == Opcode.SGET_BOOLEAN && prevInstruction instanceof OneRegisterInstruction prevOneRegisterInstruction)
                {
                    FieldReference field = (FieldReference)((ReferenceInstruction)prevInstruction).getReference();
                    String fieldName = field.getName();
                    if (!fieldName.equals(isAdr11OrNew.getName()) || !field.getDefiningClass().equals(Utils.GetDescriptor(isAdr11OrNew.getDeclaringClass())))
                        continue;
                    field = (FieldReference)((ReferenceInstruction)instruction).getReference();
                    fieldName = field.getName();
                    chatHeadUnavailable = classContainsChatHeadUnavailableConfig.getDeclaredField(fieldName);
                    break;
                }
            }
            if (chatHeadUnavailable == null)
            {
                Logger.e("Target field not found 3");
                return;
            }
            chatHeadUnavailable.setAccessible(true);
            DexKitCache.SetFieldRaw("EnableChatHead_AndroidVersionCheckClass_ChatHeadUnavailable", chatHeadUnavailable);
        }
    }

    static class FixWindowLayoutComponentImpl implements XposedInterface.Hooker
    {
        public Object intercept(XposedInterface.Chain chain) throws Throwable
        {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
                return chain.proceed();
            Context ctx = (Context) chain.getArg(0);
            boolean validUiContext = ctx instanceof Activity;
            if (validUiContext)
                return chain.proceed();
            try
            {
                Logger.i("Invalid context, creating new...");
                Context appCtx = ctx.getApplicationContext();
                DisplayManager dm = (DisplayManager) appCtx.getSystemService(Context.DISPLAY_SERVICE);
                Display defaultDisplay = dm.getDisplay(Display.DEFAULT_DISPLAY);
                Context windowContext = appCtx.createWindowContext(defaultDisplay, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, null);
                return chain.proceed(new Object[]{windowContext, chain.getArg(1)});
            }
            catch (Exception e) { Logger.e(e); }
            return chain.proceed();
        }
    }

    static class SpoofAndroidVersionCheckFields implements XposedInterface.Hooker
    {
        private static final HashMap<Field, Boolean> originalValues = new HashMap<>();

        public Object intercept(XposedInterface.Chain chain) throws Throwable
        {
            if (!Config.getEnableChatHead())
                return chain.proceed();
            try
            {
                if (!originalValues.isEmpty())
                    return chain.proceed();
                Boolean originalValue = (Boolean)isAdr10OrOld.get(null);
                Boolean originalValue2 = (Boolean)isAdr11OrNew.get(null);
                if (originalValue == null || originalValue2 == null)
                    return chain.proceed();
                originalValues.put(isAdr10OrOld, originalValue);
                originalValues.put(isAdr11OrNew, originalValue2);
                isAdr10OrOld.set(null, !originalValue);
                isAdr11OrNew.set(null, !originalValue2);
                Object result = chain.proceed();
                for (Field f : originalValues.keySet())
                    f.set(null, originalValues.get(f));
                originalValues.clear();
                return result;
            }
            catch (Exception e) { Logger.e(e); }
            return chain.proceed();
        }
    }
}