package com.ehvn.zaloxposed.hooks;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.android.tools.smali.dexlib2.Opcode;
import com.android.tools.smali.dexlib2.iface.instruction.Instruction;
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction;
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction;
import com.android.tools.smali.dexlib2.iface.reference.FieldReference;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import io.github.libxposed.api.XposedInterface;

// It would be great if we can patch dex...
@SuppressWarnings("unused")
public class EnableChatHeadHook extends BaseHook
{
    static Field isAndroid10FullOrOlder = null;
    static Field isAndroid11OrNewerFull = null;
    static Field chatHeadUnavailable = null;
    static boolean chatHeadUnavailableOriginalValue = false;

    @Override
    public void hook() throws Throwable
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
            return;
        fixMiniChatAndroid13();
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .returnType("void")
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .paramCount(0)
                .addUsingString("ENABLE_SOCKET_UPLOAD_FOR_VIDEO", StringMatchType.Equals)
                .addUsingString("MALICIOUS_MIME_TYPE", StringMatchType.Equals)
                .addUsingString("NOTIFICATION_MANAGER_CONFIG", StringMatchType.Equals)
                .addUsingString("ACTION_FOR_SKIP_JUMP_DOMAIN", StringMatchType.Equals)
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found 1");
            return;
        }
        Method targetMethod = methods.get(0).getMethodInstance(classLoader);
        Class<?> classContainsChatHeadUnavailableConfig = targetMethod.getDeclaringClass();
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .name("<clinit>")
                .addUsingNumber(26)
                .addUsingNumber(29)
                .addUsingNumber(30)
                .addUsingNumber(33)
                .addUsingField(FieldMatcher.create().name("SDK_INT"))
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found 2");
            return;
        }
        Class<?> androidVersionCheckClass = null;
        for (MethodData m : methods)
        {
            Class<?> tempandroidVersionCheckClass = Objects.requireNonNull(m.getDeclaredClass()).getInstance(classLoader);
            if (!Modifier.isAbstract(tempandroidVersionCheckClass.getModifiers()))
                continue;
            androidVersionCheckClass = tempandroidVersionCheckClass;
            break;
        }
        if (androidVersionCheckClass == null)
        {
            Logger.e("Target class not found 3");
            return;
        }
        ArrayList<Instruction> instructions = Utils.Disassemble(androidVersionCheckClass, "<clinit>");
        for (Instruction instruction : instructions)
        {
            if (instruction.getOpcode() == Opcode.SPUT_BOOLEAN && instruction instanceof OneRegisterInstruction oneRegisterInstruction)
            { 
                FieldReference field = (FieldReference)((ReferenceInstruction)instruction).getReference();
                String fieldName = field.getName(); 
                if (isAndroid10FullOrOlder == null)
                    isAndroid10FullOrOlder = androidVersionCheckClass.getDeclaredField(fieldName);
                else if (isAndroid11OrNewerFull == null)
                {
                    isAndroid11OrNewerFull = androidVersionCheckClass.getDeclaredField(fieldName);
                    break;
                }
            }
        }
        if (isAndroid10FullOrOlder == null)
        {
            Logger.e("Target field not found 1");
            return;
        }
        if (isAndroid11OrNewerFull == null)
        {
            Logger.e("Target field not found 2");
            return;
        }
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
                if (!fieldName.equals(isAndroid11OrNewerFull.getName()) || !field.getDefiningClass().equals(Utils.GetDescriptor(isAndroid11OrNewerFull.getDeclaringClass())))
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
        isAndroid10FullOrOlder.setAccessible(true);
        isAndroid11OrNewerFull.setAccessible(true);
        chatHeadUnavailable.setAccessible(true);
        SpoofAndroidVersionCheckFields spoofHook = new SpoofAndroidVersionCheckFields();
        Logger.i("Hooking [0]: " + targetMethod);
        module.hook(targetMethod).intercept(chain ->
        {
            Object result = chain.proceed();
            Config.addOnConfigChangedListener((key, oldValue, newValue) ->
            {
                if (!"enable_chat_head".equals(key))
                    return;
                if (chatHeadUnavailable == null)
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
        doHook(spoofHook);
    }

    private void doHook(SpoofAndroidVersionCheckFields spoofHook) throws NoSuchMethodException, ClassNotFoundException
    {
        List<MethodData> methods;
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.settings.SettingMessageV2View")
                .returnType("void")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(0)
                .addUsingString("QUICK_MESSAGE_FEATURE_ENABLE", StringMatchType.Equals)));
        if (methods.isEmpty())
            Logger.e("Target method not found 1");
        for (MethodData m : methods)
        {
            Method method = m.getMethodInstance(classLoader);
            Logger.i("Hooking [1]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.maintab.MainTabView")
                .returnType("void")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(0)
                .addUsingString("DEFAULT_REQUEST_CHAT_HEAD_AFTER_INSTALL_AUTO_RESTORE", StringMatchType.Equals)
                .addUsingString("LAST_TIME_SHOW_REMIND_UPDATE_MINI_CHAT_PERMISSION", StringMatchType.Equals)
                .addUsingString("CONFIG_POPUP_BA_PURCHASE_SUCCESS_${UserID}", StringMatchType.Equals)
                .addUsingString("SHOULD_REMIND_UPDATE_MINI_CHAT_PERMISSION", StringMatchType.Equals)
                .addUsingString("LAST_TIME_SHOW_REMIND_UPDATE_MINI_CHAT_PERMISSION", StringMatchType.Equals)));
        if (methods.isEmpty())
            Logger.e("Target method not found 2");
        for (MethodData m : methods)
        {
            Method method = m.getMethodInstance(classLoader);
            Logger.i("Hooking [2]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.maintab.group.GroupTabView")
                .returnType("void")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(1)
                .paramTypes("int")
                .addUsingString("name", StringMatchType.Equals)
                .addUsingString("id", StringMatchType.Equals)
                .addUsingField(FieldMatcher.create()
                        .name("str_optionM_receiveNotification"))
                .addUsingField(FieldMatcher.create().name("str_optionM_muteConversation"))
                .addUsingField(FieldMatcher.create().name("str_hide_message"))
                .addUsingField(FieldMatcher.create().name("context_menu_item_leave_group"))
                .addUsingField(FieldMatcher.create().name("str_leave_community"))
                .addUsingField(FieldMatcher.create().name("str_open_bubble_chat"))
                .addUsingField(FieldMatcher.create().name("str_open_chat_head"))));
        if (methods.isEmpty())
            Logger.e("Target method not found 3");
        for (MethodData m : methods)
        {
            Method method = m.getMethodInstance(classLoader);
            Logger.i("Hooking [3]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        methods = bridge.findMethod(FindMethod.create()
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
            ));
        if (methods.isEmpty())
            Logger.e("Target method not found 4");
        for (MethodData m : methods)
        {
            Method method = m.getMethodInstance(classLoader);
            Logger.i("Hooking [4]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        methods = bridge.findMethod(FindMethod.create()
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
            ));
        if (methods.isEmpty())
            Logger.e("Target method not found 5");
        for (MethodData m : methods)
        {
            Method method = m.getMethodInstance(classLoader);
            Logger.i("Hooking [5]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.maintab.msg.TabMsgContextMenuView")
                .returnType("void")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(1)
                .paramTypes("java.util.ArrayList")
            ));
        if (methods.isEmpty())
            Logger.e("Target method not found 6");
        for (MethodData m : methods)
        {
            Method method = m.getMethodInstance(classLoader);
            Logger.i("Hooking [6]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        methods = bridge.findMethod(FindMethod.create()
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
            ));
        if (methods.isEmpty())
            Logger.e("Target method not found 7");
        for (MethodData m : methods)
        {
            Method method = m.getMethodInstance(classLoader);
            Logger.i("Hooking [7]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .name("run")
                .returnType("void")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(0)
                .addUsingString("action.open.zinstantview", StringMatchType.Equals)
                .addUsingString("ZPF-PromotionTransferTooltip", StringMatchType.Equals)
                .addUsingString("getChildZaloViewManager(...)", StringMatchType.Equals)
                .addUsingString("System Setting not allow to create Bubbles on Screen", StringMatchType.Equals)
                .addUsingString("ChatView: Open Bubble Chat ", StringMatchType.Equals)
            ));
        if (methods.isEmpty())
            Logger.e("Target method not found 8");
        for (MethodData m : methods)
        {
            Method method = m.getMethodInstance(classLoader);
            Logger.i("Hooking [8]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        methods = bridge.findMethod(FindMethod.create()
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
            ));
        if (methods.isEmpty())
            Logger.e("Target method not found 9");
        for (MethodData m : methods)
        {
            Method method = m.getMethodInstance(classLoader);
            Logger.i("Hooking [9]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .name("run")
                .returnType("void")
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(0)
                .addUsingString("MAIN_CHAT_HEAD", StringMatchType.Equals)
                .addUsingString("MiniChatController", StringMatchType.Equals)
                .addUsingField(FieldMatcher.create().name("logo_zalo_chathead"))
            ));
        if (methods.isEmpty())
            Logger.e("Target method not found 10");
        for (MethodData m : methods)
        {
            Method method = m.getMethodInstance(classLoader);
            Logger.i("Hooking [10]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .returnType("boolean")
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .paramCount(0)
                .addInvoke(MethodMatcher.create()
                    .name("canDrawOverlays")
                    .declaredClass("android.provider.Settings"))
                .addInvoke(MethodMatcher.create()
                    .name("getAppContext")
                    .declaredClass("com.zing.zalo.MainApplication"))
                ));
        if (methods.isEmpty())
            Logger.e("Target method not found 11");
        Class<?> clazz = null;
        for (MethodData m : methods)
        {
            Method method = m.getMethodInstance(classLoader);
            if (clazz == null)
            {
                Class<?> maybe_DclrClass = method.getDeclaringClass();
                List<MethodData> dclrMethods = bridge.findMethod(FindMethod.create()
                    .matcher(MethodMatcher.create()
                        .declaredClass(maybe_DclrClass)
                        .returnType("boolean")
                        .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                        .paramCount(1)
                        .paramTypes("java.lang.String")
                        .addUsingString("MiniChatController", StringMatchType.Equals)
                    ));
                if (!dclrMethods.isEmpty())
                    clazz = maybe_DclrClass;
                else
                    continue;
            }
            Logger.i("Hooking [11]: " + method);
            module.hook(method).intercept(spoofHook);
        }
        // more than 1
        if (clazz != null)
        {
            methods = bridge.findMethod(FindMethod.create()
                .matcher(MethodMatcher.create()
                    .declaredClass(clazz)
                    .returnType("void")
                    .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                    .paramCount(1)
                    .paramTypes("java.lang.String")
                ));
            if (methods.isEmpty())
                Logger.e("Target method not found 12");
            for (MethodData m : methods)
            {
                Method method = m.getMethodInstance(classLoader);
                Logger.i("Hooking [12]: " + method);
                module.hook(method).intercept(spoofHook);
            }
            methods = bridge.findMethod(FindMethod.create()
                .matcher(MethodMatcher.create()
                    .declaredClass(clazz)
                    .returnType("void")
                    .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                    .paramCount(2)
                    .paramTypes("int", "java.lang.String")
                ));
            if (methods.isEmpty())
                Logger.e("Target method not found 13");
            for (MethodData m : methods)
            {
                Method method = m.getMethodInstance(classLoader);
                Logger.i("Hooking [13]: " + method);
                module.hook(method).intercept(spoofHook);
            }
        }
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .returnType("boolean")
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .paramCount(0)
                .addUsingString("SETTING_ENABLE_CHAT_HEAD_SERVER", StringMatchType.Equals)));
        if (methods.isEmpty())
            Logger.e("Target method not found 14");
        for (MethodData m : methods)
        {
            Method method = m.getMethodInstance(classLoader);
            Logger.i("Hooking [14]: " + method);
            module.hook(method).intercept(chain ->
            {
                if (!Config.getEnableChatHead())
                    return chain.proceed();
                return true;
            });
        }
        Method method = Class.forName("com.zing.zalo.ui.maintab.group.GroupTabView", false, classLoader).getDeclaredMethod("onActivityResult", int.class, int.class, Intent.class);
        Logger.i("Hooking [15]: " + method);
        module.hook(method).intercept(spoofHook);
        method = Class.forName("com.zing.zalo.ui.maintab.msg.MessagesView", false, classLoader).getDeclaredMethod("onActivityResult", int.class, int.class, Intent.class);
        Logger.i("Hooking [16]: " + method);
        module.hook(method).intercept(spoofHook);
        // clazz = null;
        // methods = bridge.findMethod(FindMethod.create()
        //     .matcher(MethodMatcher.create()
        //         .returnType("void")
        //         .modifiers(Modifier.PUBLIC)
        //         .paramCount(1)
        //         .addUsingString("UPDATE workspec SET period_count = 1 WHERE last_enqueue_time <> 0 AND interval_duration <> 0", StringMatchType.Equals)
        //     ));
        // if (methods.isEmpty())
        //     Logger.e("Target method not found 17");
        // if (!methods.isEmpty())
        // {
        //     clazz = Objects.requireNonNull(methods.get(0).getDeclaredClass()).getInstance(classLoader);
        //     methods = bridge.findMethod(FindMethod.create()
        //         .matcher(MethodMatcher.create()
        //             .declaredClass(clazz)
        //             .returnType("boolean")
        //             .modifiers(Modifier.PUBLIC)
        //             .paramCount(0)
        //         ));
        //     if (methods.isEmpty())
        //         Logger.e("Target method not found 17_2");
        //     for (MethodData m : methods)
        //     {
        //         Method method = m.getMethodInstance(classLoader);
        //         Logger.i("Hooking [17]: " + method);
        //         module.hook(method).intercept(chain ->
        //     }
        // }
        // methods = bridge.findMethod(FindMethod.create()
        //     .matcher(MethodMatcher.create()
        //         .name("run")
        //         .returnType("void")
        //         .modifiers(Modifier.PUBLIC | Modifier.FINAL)
        //         .paramCount(0)
        //         .addUsingString("System.exit returned normally, while it was supposed to halt JVM.", StringMatchType.Equals)
        //         .addUsingString("SMLZCloudMigrationWorkerHelper", StringMatchType.Equals)
        //         .addUsingString("ActionLogRolledMediaDetect", StringMatchType.Equals)
        //         .addUsingString("HAS_MSG_HIDDEN_CHAT_NEW", StringMatchType.Equals)
        //         .addUsingString("features@qr@bank_card@feedback@timeout", StringMatchType.Equals)
        //         .addUsingString("DatabaseHelper", StringMatchType.Equals)));
        // if (methods.isEmpty())
        //     Logger.e("Target method not found 18");
        // for (MethodData m : methods)
        // {
        //     Method method = m.getMethodInstance(classLoader);
        //     Logger.i("Hooking [18]: " + method);
        //     module.hook(method).intercept(spoofHook);
        // }
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

    static class FixWindowLayoutComponentImpl implements XposedInterface.Hooker
    {
        public Object intercept(@NonNull XposedInterface.Chain chain) throws Throwable
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
            catch (Exception e)
            {
                Logger.e(e);
            }
            return chain.proceed();
        }
    }

    static class SpoofAndroidVersionCheckFields implements XposedInterface.Hooker
    {
        private static final HashMap<Field, Boolean> originalValues = new HashMap<>();

        public Object intercept(@NonNull XposedInterface.Chain chain) throws Throwable
        {
            if (!Config.getEnableChatHead())
                return chain.proceed();
            try
            {
                if (!originalValues.isEmpty())
                    return chain.proceed();
                Boolean originalValue = (Boolean)isAndroid10FullOrOlder.get(null);
                Boolean originalValue2 = (Boolean)isAndroid11OrNewerFull.get(null);
                if (originalValue == null || originalValue2 == null)
                    return chain.proceed();
                originalValues.put(isAndroid10FullOrOlder, originalValue);
                originalValues.put(isAndroid11OrNewerFull, originalValue2);
                isAndroid10FullOrOlder.set(null, !originalValue);
                isAndroid11OrNewerFull.set(null, !originalValue2);
                Object result = chain.proceed();
                for (Field f : originalValues.keySet())
                { 
                    f.setAccessible(true);
                    f.set(null, originalValues.get(f));
                }
                originalValues.clear();
                return result;
            }
            catch (Exception e)
            {
                Logger.e(Utils.GetStackTrace(e));
            }
            return chain.proceed();
        }
    }
}