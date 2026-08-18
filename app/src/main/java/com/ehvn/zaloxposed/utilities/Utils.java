package com.ehvn.zaloxposed.utilities;

import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.android.tools.smali.dexlib2.DexFileFactory;
import com.android.tools.smali.dexlib2.Opcodes;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;
import com.android.tools.smali.dexlib2.iface.ClassDef;
import com.android.tools.smali.dexlib2.iface.MethodImplementation;
import com.android.tools.smali.dexlib2.iface.MultiDexContainer;
import com.android.tools.smali.dexlib2.iface.instruction.Instruction;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.ClassData;
import org.luckypray.dexkit.result.MethodData;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Utils
{
    private Utils() { }

    private static final Map<Class<?>, String> PRIMITIVE_MAP = new HashMap<>();
    private static final String TAG = "ZaloXposed";
    private static ClassLoader sClassLoader;
    private static String externalFilesDir = "";
    private static String packageName = "";
    private static Class<?> cfgClass;
    private static Method getCurrentUserInfoMethod;
    private static String mdPath = "";
    private static MultiDexContainer<? extends DexBackedDexFile> dexContainer;
    private static Class<?> drawableResourceClass = null;
    private static Class<?> resourceClass = null;

    static
    {
        PRIMITIVE_MAP.put(boolean.class, "Z");
        PRIMITIVE_MAP.put(byte.class, "B");
        PRIMITIVE_MAP.put(short.class, "S");
        PRIMITIVE_MAP.put(char.class, "C");
        PRIMITIVE_MAP.put(int.class, "I");
        PRIMITIVE_MAP.put(long.class, "J");
        PRIMITIVE_MAP.put(float.class, "F");
        PRIMITIVE_MAP.put(double.class, "D");
        PRIMITIVE_MAP.put(void.class, "V");
    }

    public static void Init(ApplicationInfo appInfo, ClassLoader classLoader, DexKitBridge bridge) throws NoSuchMethodException, IOException
    {
        sClassLoader = classLoader;
        packageName = appInfo.packageName;
        File apkFile = new File(appInfo.sourceDir);
        dexContainer = DexFileFactory.loadDexContainer(apkFile, Opcodes.getDefault());
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .paramCount(0)
                .returnType("int")
                .addUsingString("PRIVACY_SETTINGS_SETTING_VIEW_DOB_%s", StringMatchType.Equals)
                .addUsingString("%s", StringMatchType.Equals)
            ));
        if (methods.isEmpty())
        {
            Log.e(TAG, "Config method not found");
            return;
        }
        cfgClass = methods.get(0).getMethodInstance(sClassLoader).getDeclaringClass();
        methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(cfgClass)
                .paramCount(0)
                .returnType("java.lang.String")
                .addUsingString("UserInfo", StringMatchType.Equals)
            ));
        getCurrentUserInfoMethod = methods.isEmpty() ? null : methods.get(0).getMethodInstance(sClassLoader);
        List<ClassData> classes = bridge.findClass(FindClass.create().matcher(ClassMatcher.create().addFieldForName("zds_ic_storage_line_24")));
        for (ClassData classData : classes)
        {
            try
            {
                Class<?> clazz = classData.getInstance(classLoader);
                if (clazz.getName().equals("com.zing.zalo.R.drawable"))
                    continue;
                drawableResourceClass = clazz;
                break;
            }
            catch (Throwable ignored) { }
        }
        if (drawableResourceClass == null && classes.size() > 0)
        {
            try
            {
                drawableResourceClass = classes.get(0).getInstance(classLoader);
            }
            catch (Throwable ignored) { }
        }
        classes = bridge.findClass(FindClass.create().matcher(ClassMatcher.create().addFieldForName("signup_gender_female")));
        for (ClassData classData : classes)
        {
            try
            {
                Class<?> clazz = classData.getInstance(classLoader);
                if (clazz.getName().equals("com.zing.zalo.R"))
                    continue;
                resourceClass = clazz;
                break;
            }
            catch (Throwable ignored) { }
        }
        if (resourceClass == null && classes.size() > 0)
        {
            try
            {
                resourceClass = classes.get(0).getInstance(classLoader);
            }
            catch (Throwable ignored) { }
        }
    }

    public static Class<?> GetConfigClass()
    {
        return cfgClass;
    }

    public static String GetCurrentUserID()
    {
        try
        {
            Class<?> clazz = Class.forName("com.zing.zalocore.CoreUtility", true, sClassLoader);
            for (Field field : clazz.getDeclaredFields())
            {
                if (field.getType() != String.class)
                    continue;
                field.setAccessible(true);
                String value = (String) field.get(null);
                if (value != null && value.matches("\\d+"))
                    return value;
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "GetCurrentUserID error: " + e);
        }
        return "0";
    }

    public static String GetCurrentUserToken()
    {
        try
        {
            String userID = GetCurrentUserID();
            if (userID.isEmpty())
                return "";
            Class<?> clazz = Class.forName("com.zing.zalocore.CoreUtility", true, sClassLoader);
            for (Field field : clazz.getDeclaredFields())
            {
                if (field.getType() != String.class)
                    continue;
                field.setAccessible(true);
                String value = (String) field.get(null);
                if (value != null && value.contains(userID) && !value.equals(userID))
                    return value;
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "GetCurrentUserToken error: " + e);
        }
        return "";
    }

    public static String GetAppVersion()
    {
        try
        {
            Class<?> clazz = Class.forName("com.zing.zalocore.CoreUtility", true, sClassLoader);
            for (Field field : clazz.getDeclaredFields())
            {
                if (field.getType() != String.class)
                    continue;
                field.setAccessible(true);
                String value = (String) field.get(null);
                if (value != null && value.matches("\\d{2}\\.\\d{2}\\.\\d{2}"))
                    return value;
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "GetAppVersion error: " + e);
        }
        return "";
    }

    public static String GetStackTrace()
    {
        Throwable throwable = new Throwable();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    public static String GetStackTrace(Throwable throwable)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    public static String GetCurrentUserInfo()
    {
        if (getCurrentUserInfoMethod == null)
            return "";
        try
        {
            Object userInfo = getCurrentUserInfoMethod.invoke(null);
            if (userInfo != null)
                return userInfo.toString();
        }
        catch (Exception e)
        {
            Log.e(TAG, "GetCurrentUserInfo error: " + e);
        }
        return "";
    }

    public static Object UnsafeAllocate(Class<?> clazz) throws Exception
    {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafeField.setAccessible(true);
        Object unsafeInstance = theUnsafeField.get(null);
        Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
        allocateInstance.setAccessible(true);
        return allocateInstance.invoke(unsafeInstance, clazz);
    }

    public static List<Field> GetAllFields(Class<?> clazz)
    {
        List<Field> fields = new ArrayList<>();
        Class<?> c = clazz;
        while (c != null && c != Object.class)
        {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
            c = c.getSuperclass();
        }
        return fields;
    }

    public static Field FindFieldByValue(Object obj, Object value)
    {
        List<Field> fields = new ArrayList<>();
        Class<?> c = obj.getClass();
        while (c != null && c != Object.class)
        {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
            c = c.getSuperclass();
        }
        for (Field f : fields)
        {
            try
            {
                f.setAccessible(true);
                Object v = f.get(obj);
                if (value.equals(v))
                    return f;
            }
            catch (Throwable ignored) { }
        }
        return null;
    }

    public static Field FindFieldByType(Class<?> clazz, String typeName)
    {
        List<Field> fields = new ArrayList<>();
        Class<?> c = clazz;
        while (c != null && c != Object.class)
        {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
            c = c.getSuperclass();
        }
        for (Field f : fields)
        {
            try
            {
                if (f.getType().getName().equals(typeName))
                    return f;
            }
            catch (Throwable ignored) { }
        }
        return null;
    }

    public static Object FindObjectByValue(List<Object> objList, Object value)
    {
        for (Object obj : objList)
        {
            List<Field> fields = new ArrayList<>();
            Class<?> c = obj.getClass();
            while (c != null && c != Object.class)
            {
                fields.addAll(Arrays.asList(c.getDeclaredFields()));
                c = c.getSuperclass();
            }
            for (Field f : fields)
            {
                try
                {
                    f.setAccessible(true);
                    Object v = f.get(obj);
                    if (value.equals(v))
                        return obj;
                }
                catch (Throwable ignored) { }
            }
        }
        return null;
    }

    public static ArrayList<Instruction> Disassemble(Method method) throws IOException
    {
        Class<?> clazz = method.getDeclaringClass();
        for (String entryName : dexContainer.getDexEntryNames())
        {
            if (!entryName.endsWith(".dex"))
                continue;
            var entry = dexContainer.getEntry(entryName);
            if (entry == null)
                continue;
            DexBackedDexFile dexFile = entry.getDexFile();
            for (ClassDef classDef : dexFile.getClasses())
            {
                if (!classDef.getType().equals(GetDescriptor(clazz)))
                    continue;
                for (com.android.tools.smali.dexlib2.iface.Method m : classDef.getMethods())
                {
                    if (!m.getName().equals(method.getName()))
                        continue;
                    boolean paramsMatch = true;
                    var parameters = method.getParameterTypes();
                    if (m.getParameters().size() != parameters.length)
                        paramsMatch = false;
                    else
                    {
                        for (int i = 0; i < m.getParameters().size(); i++)
                        {
                            if (m.getParameters().get(i).getType().equals(GetDescriptor(parameters[i])))
                                continue;
                            paramsMatch = false;
                            break;
                        }
                    }
                    if (!paramsMatch)
                        continue;
                    MethodImplementation impl = m.getImplementation();
                    if (impl == null)
                        continue;
                    ArrayList<Instruction> result = new ArrayList<>();
                    impl.getInstructions().forEach(result::add);
                    return result;
                }
            }
        }
        return new ArrayList<>();
    }

    public static ArrayList<Instruction> Disassemble(Class<?> clazz, String methodName) throws IOException
    {
        for (String entryName : dexContainer.getDexEntryNames())
        {
            if (!entryName.endsWith(".dex"))
                continue;
            var entry = dexContainer.getEntry(entryName);
            if (entry == null)
                continue;
            DexBackedDexFile dexFile = entry.getDexFile();
            for (ClassDef classDef : dexFile.getClasses())
            {
                if (!classDef.getType().equals(GetDescriptor(clazz)))
                    continue;
                for (com.android.tools.smali.dexlib2.iface.Method method : classDef.getMethods())
                {
                    if (!method.getName().equals(methodName))
                        continue;
                    MethodImplementation impl = method.getImplementation();
                    if (impl == null)
                        continue;
                    ArrayList<Instruction> result = new ArrayList<>();
                    impl.getInstructions().forEach(result::add);
                    return result;
                }
            }
        }
        return new ArrayList<>();
    }

    public static String GetDescriptor(Class<?> clazz)
    {
        if (clazz == null)
            return "";
        if (PRIMITIVE_MAP.containsKey(clazz))
            return PRIMITIVE_MAP.get(clazz);
        if (clazz.isArray())
            return "[" + GetDescriptor(clazz.getComponentType());
        return "L" + clazz.getName().replace('.', '/') + ";";
    }

    public static String GetExternalFilesDir()
    {
        if (externalFilesDir.isEmpty())
        {
            try
            {
                if (packageName.isEmpty())
                    return "";
                String path = "/storage/emulated/0/Android/data/" + packageName + "/files";
                File dir = new File(path);
                if (dir.exists() || dir.mkdirs())
                    externalFilesDir = dir.getAbsolutePath();
            }
            catch (Exception e)
            {
                Logger.e("[ZaloXposed] Cannot get externalFilesDir:");
                Logger.e(e);
            }
        }
        return externalFilesDir;
    }

    public static String GetZaloXposedDir()
    {
        String externalFilesDir = GetExternalFilesDir();
        if (externalFilesDir.isEmpty())
            return "";
        File dir = new File(externalFilesDir, "ZaloXposed");
        if (!dir.exists())
            dir.mkdirs();
        return dir.getAbsolutePath();
    }

    public static int GetDrawableResourceIdByName(String resourceName)
    {
        try
        { 
            if (drawableResourceClass == null)
            {
                Logger.e("Drawable resource class not found");
                return 0;
            }
            Field field = drawableResourceClass.getField(resourceName);
            return field.getInt(null);
        }
        catch (Exception e)
        {
            Log.e(TAG, "GetDrawableResourceIdByName error: " + e);
        }
        return 0;
    }

    public static int GetResourceIdByName(String resourceName)
    {
        try
        { 
            if (resourceClass == null)
            {
                Logger.e("Resource class not found");
                return 0;
            }
            Field field = resourceClass.getField(resourceName);
            return field.getInt(null);
        }
        catch (Exception e)
        {
            Log.e(TAG, "GetResourceIdByName error: " + e);
        }
        return 0;
    }

    public static void HideView(View view) 
    {
        if (view == null) 
            return;
        view.setVisibility(View.GONE);
        view.setMinimumHeight(0);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) 
            return;
        layoutParams.height = 0;
        if (layoutParams.width <= 0)
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
    }
}