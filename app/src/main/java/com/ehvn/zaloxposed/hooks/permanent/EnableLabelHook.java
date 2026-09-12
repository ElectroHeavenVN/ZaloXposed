package com.ehvn.zaloxposed.hooks.permanent;

import com.android.tools.smali.dexlib2.Opcode;
import com.android.tools.smali.dexlib2.iface.instruction.Instruction;
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction;
import com.android.tools.smali.dexlib2.iface.reference.MethodReference;
import com.android.tools.smali.dexlib2.iface.reference.StringReference;
import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Logger;
import com.ehvn.zaloxposed.utilities.Utils;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class EnableLabelHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("java.lang.Object")
                .paramCount(0)
                .addUsingString("features@comm4work@chat_label@enable_manage_tag", StringMatchType.Equals)
                .addUsingString("features@comm4work@chat_label@max_conversation_ids", StringMatchType.Equals)
                .addUsingString("features@comm4work@chat_label@refresh_tag_interval", StringMatchType.Equals)
                .addUsingString("features@comm4work@chat_label@tag_show_beta_badge", StringMatchType.Equals)
                .addUsingString("color_tag_supported", StringMatchType.Equals)
                .addUsingString("CONFIG_CHAT_TAG_", StringMatchType.Equals)
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        Method method = methods.get(0).getMethodInstance(classLoader);
        var instructions = Utils.Disassemble(method);
        Method cfgMethod = null;
        for (int i = 0; i < instructions.size() - 1; i++)
        {
            Instruction instruction = instructions.get(i);
            if (instruction.getOpcode() != Opcode.CONST_STRING)
                continue;
            ReferenceInstruction refInstruction = (ReferenceInstruction) instruction;
            if (!(refInstruction.getReference() instanceof StringReference stringRef))
                continue;
            if (!"features@comm4work@chat_label@enable_manage_tag".equals(stringRef.getString()))
                continue;
            Instruction nextInstruction = instructions.get(i + 1);
            if (nextInstruction.getOpcode() != Opcode.INVOKE_STATIC)
                continue;
            if (!(nextInstruction instanceof ReferenceInstruction referenceInstruction))
                continue;
            MethodReference methodRef = (MethodReference)referenceInstruction.getReference();
            String methodName = methodRef.getName();
            String className = methodRef.getDefiningClass();
            cfgMethod = Class.forName(Utils.DescriptorToClassName(className), false, classLoader).getDeclaredMethod(methodName, String.class, int.class);
            break;
        }
        if (cfgMethod == null)
        {
            Logger.e("Target method not found 2");
            return;
        }
        Logger.i("Hooking: " + cfgMethod);
        module.hook(cfgMethod).intercept(chain ->
        {
            String key = (String)chain.getArg(0);
            if (key.equals("features@comm4work@chat_label@enable_chat_filter"))
                return 2;
            if (key.equals("features@comm4work@chat_label@enable_manage_tag"))
                return 1;
            return chain.proceed();
        });
    }
}
