package io.github.electroheavenvn.zaloxposed.hooks.functional;

import com.android.tools.smali.dexlib2.Opcode;
import com.android.tools.smali.dexlib2.iface.instruction.Instruction;
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction;
import com.android.tools.smali.dexlib2.iface.reference.FieldReference;
import com.android.tools.smali.dexlib2.iface.reference.StringReference;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;
import io.github.electroheavenvn.zaloxposed.utilities.Utils;

public class EnableShareHiddenStickerPackHook extends BaseHook
{
    static Field isHiddenField = null;
    Object stickerInstance = null;
    Map<Object, Integer> stickerMap = new HashMap<>();
    static Method toJsonMethod = null;

    @Override
    public void hook() throws Throwable
    {  
        Config.addOnConfigChangedListener((key, oldValue, newValue) ->
        {
            if (!Config.KEY_ENABLE_SHARE_HIDDEN_STICKER_PACK.equals(key))
                return;
            if ((Boolean)newValue)
                return;
            for (Map.Entry<Object, Integer> entry : stickerMap.entrySet())
            {
                Object sticker = entry.getKey();
                Integer originalIsHiddenValue = entry.getValue();
                try
                {
                    if (isHiddenField == null)
                        return;
                    isHiddenField.set(sticker, originalIsHiddenValue);
                }
                catch (Exception e) { Logger.e(e); }
            }
        });

        toJsonMethod = DexKitCache.GetMethod("EnableShareHiddenStickerPack_ToJsonString", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(0)
                .returnType("java.lang.String")
                .addUsingString(",\"is_hidden\":", StringMatchType.Equals)
                .addUsingString(",\"iconPreview\":", StringMatchType.Equals)
                .addUsingString(",\"isFree\":", StringMatchType.Equals)
            )), classLoader);
        if (toJsonMethod == null)
            return;
        Method onStickerPackOptionsClicked = DexKitCache.GetMethod("EnableShareHiddenStickerPack_OnStickerPackOptionsClicked", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("void")
                .paramCount(1)
                .paramTypes("java.lang.Object")
                .addUsingString("dataParam", StringMatchType.Equals)
                .addUsingString("getChildZaloViewManager(...)", StringMatchType.Equals)
                .addUsingString("tip.csc.sticker.promotion", StringMatchType.Equals)
                .addUsingString("null cannot be cast to non-null type com.zing.zalo.ui.picker.stickerpanel.custom.EmojiChatPanelPage", StringMatchType.Equals)
            )), classLoader);
        if (onStickerPackOptionsClicked == null)
            return;
        Method synchronizedMapClassGet = Class.forName("java.util.Collections$SynchronizedMap", false, classLoader).getDeclaredMethod("get", Object.class);
        Logger.i("Hooking: " + synchronizedMapClassGet);
        module.hook(synchronizedMapClassGet).intercept(chain ->
        {
            if (!Config.getEnableShareHiddenStickerPack())
                return chain.proceed();
            Object key = chain.getArg(0);
            if (!(key instanceof Integer stickerId))
                return chain.proceed();
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            boolean calledFromOnStickerPackOptionsClicked = false;
            for (StackTraceElement element : stackTrace)
            {
                if (element.getClassName().equals(onStickerPackOptionsClicked.getDeclaringClass().getName()) &&
                    element.getMethodName().equals(onStickerPackOptionsClicked.getName()))
                {
                    calledFromOnStickerPackOptionsClicked = true;
                    break;
                }
            }
            if (!calledFromOnStickerPackOptionsClicked)
                return chain.proceed();
            Object result = chain.proceed();
            if (result != null)
            {
                stickerMap.put(result, (Integer) isHiddenField.get(result));
                isHiddenField.set(result, 0);
                return result;
            }
            if (stickerInstance != null)
                return stickerInstance;
            try
            {
                Class<?> stickerClass = toJsonMethod.getDeclaringClass();
                Constructor<?> constructor = stickerClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                stickerInstance = constructor.newInstance();
                isHiddenField.set(stickerInstance, 0);
            }
            catch (Exception e) { Logger.e(e); }
            return null;
        });
        loadFields();
    }

    private void loadFields() throws NoSuchFieldException, IOException
    {
        isHiddenField = DexKitCache.GetFieldRaw("EnableShareHiddenStickerPack_StickerIsHidden", classLoader);
        if (isHiddenField != null)
            return;
        ArrayList<Instruction> instructions = Utils.Disassemble(toJsonMethod);
        /*
            const-string v1, ",\"is_hidden\":"
            invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
            iget v1, p0, Lw61/g;->n:I       # <<< isHidden field
        */
        for (int i = 0; i < instructions.size() - 2; i++)
        {
            Instruction instruction = instructions.get(i);
            if (instruction.getOpcode() != Opcode.CONST_STRING)
                continue;
            ReferenceInstruction refInstruction = (ReferenceInstruction) instruction;
            if (!(refInstruction.getReference() instanceof StringReference stringRef))
                continue;
            if (!",\"is_hidden\":".equals(stringRef.getString()))
                continue;
            Instruction nextInstruction = instructions.get(i + 1);
            if (nextInstruction.getOpcode() != Opcode.INVOKE_VIRTUAL)
                continue;
            Instruction nextNextInstr = instructions.get(i + 2);
            if (nextNextInstr.getOpcode() != Opcode.IGET)
                continue;
            if (!(nextNextInstr instanceof ReferenceInstruction referenceInstruction))
                continue;
            FieldReference field = (FieldReference)referenceInstruction.getReference();
            String fieldName = field.getName();
            isHiddenField = toJsonMethod.getDeclaringClass().getDeclaredField(fieldName);
            break;
        }
        if (isHiddenField == null)
        {
            Logger.e("Target field not found");
            return;
        }
        isHiddenField.setAccessible(true);
        DexKitCache.SetFieldRaw("EnableShareHiddenStickerPack_StickerIsHidden", isHiddenField);
    }
}