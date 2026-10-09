package com.ehvn.zaloxposed.hooks.custommenu;

import com.android.tools.smali.dexlib2.Opcode;
import com.android.tools.smali.dexlib2.iface.instruction.Instruction;
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction;
import com.android.tools.smali.dexlib2.iface.reference.FieldReference;
import com.android.tools.smali.dexlib2.iface.reference.StringReference;
import com.ehvn.zaloxposed.utilities.Logger;
import com.ehvn.zaloxposed.utilities.Utils;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.ClassData;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public final class TabMeSettingItemHelper
{
    private static Class<?> tabMeSettingItemClass = null;
    private static Class<? extends Enum> tabMeItemIdEnumClass = null;
    private static Field trackingField = null;
    private static Field titleField = null;
    private static Field descriptionField = null;
    private static Field iconField = null;
    private static Field idField = null;
    private static Method setDesc = null;
    public static void Init(DexKitBridge bridge, ClassLoader classLoader) throws Exception
    {
        List<ClassData> classes = bridge.findClass(FindClass.create()
            .matcher(ClassMatcher.create()
                .addUsingString("", StringMatchType.Equals)
                .addUsingString("id", StringMatchType.Equals)
                .addUsingString("title", StringMatchType.Equals)
                .addUsingString("<set-?>", StringMatchType.Equals)
                .addUsingString("SettingData(id=", StringMatchType.Equals)
                .addUsingString(", icon=", StringMatchType.Equals)
                .addUsingString(", title=", StringMatchType.Equals)
                .addUsingString(", desc=", StringMatchType.Equals)
                .addUsingString(", type=", StringMatchType.Equals)
                .addUsingString(")", StringMatchType.Equals)
                .addMethod(MethodMatcher.create().name("hashCode"))
                .addMethod(MethodMatcher.create().name("toString"))
            ));
        if (classes.isEmpty())
            Logger.e("Target class not found");
        else
            tabMeSettingItemClass = classes.get(0).getInstance(classLoader);
        classes = bridge.findClass(FindClass.create()
            .matcher(ClassMatcher.create()
                .addFieldForName("UNKNOWN")
                .addFieldForName("PROFILE")
                .addFieldForName("QUICK_ACTION")
                .addFieldForName("MY_QR")
                .addFieldForName("SAVED_MESSAGE")
                .addFieldForName("MY_OA")
                .addFieldForName("ACCOUNT")
                .addFieldForName("PRIVACY")
                .addFieldForName("QR_WALLET")
                .addFieldForName("BA_TOOL")
                .addFieldForName("BA_IAP")
                .addFieldForName("TOOL_STORAGE")
                .addFieldForName("ZALO_CLOUD")
                .addFieldForName("PROMOTE_BA")
            ));
        if (classes.isEmpty())
            Logger.e("Target enum not found");
        else
            tabMeItemIdEnumClass = classes.get(0).getInstance(classLoader).asSubclass(Enum.class);
        // Find tracking field (no way to find via reflection, must use disassembly)
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass("com.zing.zalo.ui.maintab.me.TabMeView")
                .modifiers(Modifier.STATIC)
                .returnType("java.util.ArrayList")
                .addUsingString("tab_me_privacy", StringMatchType.Equals)
                .addUsingString("tab_me_tool_storage", StringMatchType.Equals)
                .addUsingString("tab_me_account_and_security", StringMatchType.Equals)
                .addUsingString("tab_me_business_tools", StringMatchType.Equals)
            ));
        if (methods.isEmpty())
            Logger.e("Target method not found 1");
        else
        {
            ArrayList<Instruction> instructions = Utils.Disassemble(methods.get(0).getMethodInstance(classLoader));
            /*
                const-string v0, "tab_me_privacy"                       # can be "tab_me_tool_storage", "tab_me_business_tools", etc.
                iput-object v0, v5, Lrh1/e;->p:Ljava/lang/String;       # <<< tracking field
            */
            for (int i = 0; i < instructions.size() - 2; i++)
            {
                Instruction instruction = instructions.get(i);
                if (instruction.getOpcode() != Opcode.CONST_STRING)
                    continue;
                ReferenceInstruction refInstruction = (ReferenceInstruction) instruction;
                if (!(refInstruction.getReference() instanceof StringReference stringRef))
                    continue;
                if (!"tab_me_privacy".equals(stringRef.getString()))
                    continue;
                Instruction nextInstruction = instructions.get(i + 1);
                if (nextInstruction.getOpcode() != Opcode.IPUT_OBJECT)
                    continue;
                if (!(nextInstruction instanceof ReferenceInstruction referenceInstruction))
                    continue;
                FieldReference field = (FieldReference)referenceInstruction.getReference();
                String fieldName = field.getName();
                trackingField = tabMeSettingItemClass.getDeclaredField(fieldName);
                trackingField.setAccessible(true);
                break;
            }
        }
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(tabMeSettingItemClass)
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("void")
                .paramCount(1)
                .paramTypes("java.lang.String")
                .addUsingString("<set-?>", StringMatchType.Equals)
            ));
        if (methods.isEmpty())
            Logger.e("Target method not found 2");
        else
            setDesc = methods.get(0).getMethodInstance(classLoader);
    }

    private static void LateInit() throws Exception
    {
        if (titleField != null && descriptionField != null && iconField != null && idField != null)
            return;
        Object templateInstance = CreateNew(SettingItemID.TOOL_STORAGE, 12345, "title12345");
        if (setDesc != null)
            setDesc.invoke(templateInstance, "desc67890");
        for (Field field : tabMeSettingItemClass.getDeclaredFields())
        {
            boolean oldAccessible = field.isAccessible();
            field.setAccessible(true);
            if (field.getType() == String.class)
            {
                if ("title12345".equals(field.get(templateInstance)))
                {
                    titleField = field;
                    continue;
                }
                if ("desc67890".equals(field.get(templateInstance)))
                {
                    descriptionField = field;
                    continue;
                }
            }
            if (field.getType() == int.class)
            {
                if (field.getInt(templateInstance) == 12345)
                {
                    iconField = field;
                    continue;
                }
            }
            if (field.getType() == tabMeItemIdEnumClass)
            {
                // SettingItem only has one field of this enum type
                // if (field.get(templateInstance) == MapEnumToSettingItemID(SettingItemID.TOOL_STORAGE))
                idField = field;
                continue;
            }
            field.setAccessible(oldAccessible);
        }
        if (titleField == null)
            Logger.w("LateInit: titleField not found");
        if (descriptionField == null)
            Logger.w("LateInit: descriptionField not found");
        if (iconField == null)
            Logger.w("LateInit: iconField not found");
        if (idField == null)
            Logger.w("LateInit: idField not found");
    }

    public static Object CreateNew(SettingItemID id, int icon, String title) throws Exception
    {
        return tabMeSettingItemClass
            .getDeclaredConstructor(tabMeItemIdEnumClass, int.class, String.class, int.class, int.class)
            .newInstance(MapSettingItemIDToEnum(id), icon, title, 0, 24);
    }
    public static Object CreateNew(SettingItemID id, String drawableName, String title) throws Exception
    {
        return tabMeSettingItemClass
            .getDeclaredConstructor(tabMeItemIdEnumClass, int.class, String.class, int.class, int.class)
            .newInstance(MapSettingItemIDToEnum(id), Utils.GetDrawableResourceIdByName(drawableName), title, 0, 24);
    }

    public static String GetTracking(Object tabMeSettingItem) throws Exception
    {
        CheckType(tabMeSettingItem);
        LateInit();
        return (String)trackingField.get(tabMeSettingItem);
    }

    public static void SetTracking(Object tabMeSettingItem, String value) throws Exception
    {
        CheckType(tabMeSettingItem);
        LateInit();
        trackingField.set(tabMeSettingItem, value);
    }

    public static String GetTitle(Object tabMeSettingItem) throws Exception
    {
        CheckType(tabMeSettingItem);
        LateInit();
        return (String)titleField.get(tabMeSettingItem);
    }

    public static void SetTitle(Object tabMeSettingItem, String value) throws Exception
    {
        CheckType(tabMeSettingItem);
        LateInit();
        titleField.set(tabMeSettingItem, value);
    }

    public static String GetDescription(Object tabMeSettingItem) throws Exception
    {
        CheckType(tabMeSettingItem);
        LateInit();
        return (String)descriptionField.get(tabMeSettingItem);
    }

    public static void SetDescription(Object tabMeSettingItem, String value) throws Exception
    {
        CheckType(tabMeSettingItem);
        LateInit();
        descriptionField.set(tabMeSettingItem, value);
    }

    public static int GetIcon(Object tabMeSettingItem) throws Exception
    {
        CheckType(tabMeSettingItem);
        LateInit();
        return iconField.getInt(tabMeSettingItem);
    }

    public static void SetIcon(Object tabMeSettingItem, int value) throws Exception
    {
        CheckType(tabMeSettingItem);
        LateInit();
        iconField.setInt(tabMeSettingItem, value);
    }

    public static void SetIcon(Object tabMeSettingItem, String drawableName) throws Exception
    {
        CheckType(tabMeSettingItem);
        LateInit();
        iconField.setInt(tabMeSettingItem, Utils.GetDrawableResourceIdByName(drawableName));
    }

    public static SettingItemID GetID(Object tabMeSettingItem) throws Exception
    {
        CheckType(tabMeSettingItem);
        LateInit();
        Enum<?> enumValue = (Enum<?>)idField.get(tabMeSettingItem);
        if (enumValue == null)
            return null;
        return MapEnumToSettingItemID(enumValue);
    }

    public static void SetID(Object tabMeSettingItem, SettingItemID value) throws Exception
    {
        CheckType(tabMeSettingItem);
        LateInit();
        idField.set(tabMeSettingItem, MapSettingItemIDToEnum(value));
    }
 
    @SuppressWarnings("unchecked")
    private static Enum<?> MapSettingItemIDToEnum(SettingItemID id)
    {
        return Enum.valueOf(tabMeItemIdEnumClass, id.name());
    }

    private static SettingItemID MapEnumToSettingItemID(Enum<?> enumValue)
    {
        return SettingItemID.valueOf(enumValue.name());
    }

    private static void CheckType(Object tabMeSettingItem) throws IllegalArgumentException
    {
        if (!tabMeSettingItemClass.isInstance(tabMeSettingItem))
            throw new IllegalArgumentException("Object is not an instance of SettingItem");
    }
}
