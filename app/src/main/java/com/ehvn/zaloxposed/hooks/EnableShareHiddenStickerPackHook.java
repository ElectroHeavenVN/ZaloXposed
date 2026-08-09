package com.ehvn.zaloxposed.hooks;

import com.android.tools.smali.dexlib2.Opcode;
import com.android.tools.smali.dexlib2.iface.instruction.Instruction;
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction;
import com.android.tools.smali.dexlib2.iface.reference.FieldReference;
import com.android.tools.smali.dexlib2.iface.reference.StringReference;
import com.ehvn.zaloxposed.utilities.Config;
import com.ehvn.zaloxposed.utilities.Logger;
import com.ehvn.zaloxposed.utilities.Utils;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnableShareHiddenStickerPackHook extends BaseHook
{
    Field isHidden = null;
    Object stickerInstance = null;
    Map<Object, Integer> stickerMap = new HashMap<>();

    @Override
    public void hook() throws Throwable
    {  
        Config.addOnConfigChangedListener((key, oldValue, newValue) ->
        {
            if (!"enable_share_hidden_sticker_pack".equals(key))
                return;
            if ((Boolean)newValue)
                return;
            for (Map.Entry<Object, Integer> entry : stickerMap.entrySet())
            {
                Object sticker = entry.getKey();
                Integer originalIsHiddenValue = entry.getValue();
                try
                {
                    isHidden.set(sticker, originalIsHiddenValue);
                }
                catch (Exception e)
                {
                    Logger.e("Error restoring isHidden value for sticker: " + sticker, e);
                }
            }
        });

        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .paramCount(0)
                .returnType("java.lang.String")
                .addUsingString(",\"is_hidden\":", StringMatchType.Equals)
                .addUsingString(",\"iconPreview\":", StringMatchType.Equals)
                .addUsingString(",\"isFree\":", StringMatchType.Equals)
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        Method method = methods.get(0).getMethodInstance(classLoader);
        Class<?> stickerClass = method.getDeclaringClass();
        ArrayList<Instruction> instructions = Utils.Disassemble(method);
        // /*
        //     const-string v1, ",\"is_hidden\":"
        //     invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     iget v1, p0, Lw61/g;->n:I       # <<< isHidden field
        // */
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
            isHidden = method.getDeclaringClass().getDeclaredField(fieldName);
            break;
        }
        if (isHidden == null)
        {
            Logger.e("Target field not found");
            return;
        }
        isHidden.setAccessible(true);
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("void")
                .paramCount(1)
                .paramTypes("java.lang.Object")
                .addUsingString("dataParam", StringMatchType.Equals)
                .addUsingString("getChildZaloViewManager(...)", StringMatchType.Equals)
                .addUsingString("tip.csc.sticker.promotion", StringMatchType.Equals)
                .addUsingString("null cannot be cast to non-null type com.zing.zalo.ui.picker.stickerpanel.custom.EmojiChatPanelPage", StringMatchType.Equals)
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        Method onStickerPackOptionsClicked = methods.get(0).getMethodInstance(classLoader); 
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
                stickerMap.put(result, (Integer)isHidden.get(result));
                isHidden.set(result, 0);
                return result;
            }
            if (stickerInstance != null)
                return stickerInstance;
            try
            {
                Constructor<?> constructor = stickerClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                stickerInstance = constructor.newInstance();
                isHidden.set(stickerInstance, 0);
            }
            catch (Exception e)
            {
                Logger.e("Error creating sticker instance", e);
            }
            return null;
        }); 
    }
}