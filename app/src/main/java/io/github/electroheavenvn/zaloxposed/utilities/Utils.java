package io.github.electroheavenvn.zaloxposed.utilities;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.electroheavenvn.zaloxposed.MorpheConstants;

public final class Utils
{
    private Utils() { }

    private static final Map<Class<?>, String> PRIMITIVE_MAP = new HashMap<>();
    private static ClassLoader sClassLoader;
    private static String externalFilesDir = "";
    private static String packageName = "";
    private static String appDataDir = "";
    private static Class<?> cfgClass;
    private static Method getCurrentUserInfoMethod;
    private static MultiDexContainer<? extends DexBackedDexFile> dexContainer;
    private static final List<DexBackedDexFile> cachedDexFiles = new ArrayList<>();
    private static final Map<String, ClassDef> classDefCache = new ConcurrentHashMap<>();
    private static final ArrayList<Class<?>> drawableResourceClasses = new ArrayList<>();
    private static final ArrayList<Class<?>> resourceClasses = new ArrayList<>();
    private static Method getIsVietnameseMethod;
    private static int appVersionCode = 0;

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

    public static void Init(ApplicationInfo appInfo, ClassLoader classLoader, DexKitBridge bridge) throws Exception
    {
        sClassLoader = classLoader;
        packageName = appInfo.packageName;
        appDataDir = appInfo.dataDir;
        Class<?> buildConfig = Class.forName("com.zing.zalo.BuildConfig", false, classLoader);
        Field versionCodeField = buildConfig.getDeclaredField("VERSION_CODE");
        appVersionCode = versionCodeField.getInt(null);
        File apkFile = new File(appInfo.sourceDir);
        dexContainer = DexFileFactory.loadDexContainer(apkFile, Opcodes.getDefault());
        cfgClass = DexKitCache.GetClass("Utils_Config", () -> bridge.findClass(FindClass.create()
            .matcher(ClassMatcher.create()
                .addUsingString("VERIFY_ACCOUNT_WITH_IDENTIFY_CARD_%s", StringMatchType.Equals)
                .addUsingString("HUAWEI_TOKEN_%s", StringMatchType.Equals)
                .addMethod(MethodMatcher.create()
                    .paramCount(0)
                    .returnType("int")
                    .addUsingString("PRIVACY_SETTINGS_SETTING_VIEW_DOB_%s", StringMatchType.Equals)
                    .addUsingString("%s", StringMatchType.Equals))
            )), classLoader);
        if (cfgClass == null)
            return;
        getCurrentUserInfoMethod = DexKitCache.GetMethod("Utils_Config_GetCurrentUserInfo", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(cfgClass)
                .paramCount(0)
                .returnType("java.lang.String")
                .addUsingString("UserInfo", StringMatchType.Equals)
            )), classLoader);
            getIsVietnameseMethod = DexKitCache.GetMethod("Utils_Config_GetIsVietnamese", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .paramCount(0)
                .returnType("boolean")
                .addUsingString("vi", StringMatchType.Equals)
                .declaredClass(ClassMatcher.create()
                    .modifiers(Modifier.PUBLIC | Modifier.ABSTRACT)
                    .addUsingString("com.zing.zalo.ACTION_EXIT", StringMatchType.Equals)
                    .addUsingString("vi", StringMatchType.Equals)
                    .addUsingString("@vi", StringMatchType.Equals)
                    .addUsingString("en", StringMatchType.Equals)
                    .addUsingString("@en", StringMatchType.Equals)
                    .addUsingString("my", StringMatchType.Equals)
                )
            )), classLoader);
        LoadResourceClasses(bridge, classLoader);
    }

    private static void LoadResourceClasses(DexKitBridge bridge, ClassLoader classLoader)
    {
        List<Class<?>> classes = DexKitCache.GetClasses("Utils_Config_DrawableResourceClasses_1", () -> bridge.findClass(FindClass.create()
            .matcher(ClassMatcher.create()
                .addFieldForName("zds_ic_storage_line_24")
            )), classLoader);
        drawableResourceClasses.addAll(classes);
        classes = DexKitCache.GetClasses("Utils_Config_DrawableResourceClasses_2", () -> bridge.findClass(FindClass.create()
            .matcher(ClassMatcher.create()
                .addFieldForName("bg_btn_postfeed")
            )), classLoader);
        drawableResourceClasses.addAll(classes);
        classes = DexKitCache.GetClasses("Utils_Config_ResourceClasses_1", () -> bridge.findClass(FindClass.create()
            .matcher(ClassMatcher.create()
                .addFieldForName("signup_gender_female")
            )), classLoader);
        resourceClasses.addAll(classes);
        classes = DexKitCache.GetClasses("Utils_Config_ResourceClasses_2", () -> bridge.findClass(FindClass.create()
            .matcher(ClassMatcher.create()
                .addFieldForName("str_group_invite_link")
            )), classLoader);
        resourceClasses.addAll(classes);
    }

    public static Class<?> GetConfigClass()
    {
        return cfgClass;
    }

    public static ClassLoader GetClassLoader()
    {
        return sClassLoader;
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
        catch (Exception e) { Logger.e(e); }
        return "";
    }

    @SuppressLint("DiscouragedPrivateApi")
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

    public static Object Clone(Object obj) throws Exception
    {
        if (obj == null)
            return null;
        Class<?> clazz = obj.getClass();
        Object cloned = UnsafeAllocate(clazz);
        for (Field f : GetAllFields(clazz))
        {
            try
            {
                boolean oldAccessible = f.isAccessible();
                f.setAccessible(true);
                f.set(cloned, f.get(obj));
                f.setAccessible(oldAccessible);
            }
            catch (Throwable ignored) { }
        }
        return cloned;
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
                boolean oldAccessible = f.isAccessible();
                f.setAccessible(true);
                Object v = f.get(obj);
                f.setAccessible(oldAccessible);
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

    public static Object FindObjectContainsValue(List<Object> objList, Object value)
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
                    boolean oldAccessible = f.isAccessible();
                    f.setAccessible(true);
                    Object v = f.get(obj);
                    f.setAccessible(oldAccessible);
                    if (value.equals(v))
                        return obj;
                }
                catch (Throwable ignored) { }
            }
        }
        return null;
    }

    private static synchronized List<DexBackedDexFile> getDexFiles() throws IOException
    {
        if (cachedDexFiles.isEmpty() && dexContainer != null)
        {
            for (String entryName : dexContainer.getDexEntryNames())
            {
                if (!entryName.endsWith(".dex"))
                    continue;
                var entry = dexContainer.getEntry(entryName);
                if (entry != null)
                    cachedDexFiles.add(entry.getDexFile());
            }
        }
        return cachedDexFiles;
    }

    private static ClassDef findClassDef(Class<?> clazz) throws IOException
    {
        if (clazz == null)
            return null;
        String descriptor = GetDescriptor(clazz);
        ClassDef cached = classDefCache.get(descriptor);
        if (cached != null)
            return cached;
        for (DexBackedDexFile dexFile : getDexFiles())
        {
            for (ClassDef classDef : dexFile.getClasses())
            {
                if (!descriptor.equals(classDef.getType()))
                    continue;
                classDefCache.put(descriptor, classDef);
                return classDef;
            }
        }
        return null;
    }

    /**
     * @deprecated Please refrain from disassembling methods if possible, especially in the initialization phase, as it may cause performance issues.
     */
    @Deprecated
    public static ArrayList<Instruction> Disassemble(Method method) throws IOException
    {
        long startTime = System.currentTimeMillis();
        try
        {
            Class<?> clazz = method.getDeclaringClass();
            ClassDef classDef = findClassDef(clazz);
            if (classDef == null)
                return new ArrayList<>();
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
            return new ArrayList<>();
        }
        finally
        {
            long endTime = System.currentTimeMillis();
            Logger.d("Disassemble " + method.getDeclaringClass().getName() + "->" + method.getName() + " finished in " + (endTime - startTime) + "ms");
        }
    }

    /**
     * @deprecated Please refrain from disassembling methods if possible, especially in the initialization phase, as it may cause performance issues.
     */
    @Deprecated()
    public static ArrayList<Instruction> Disassemble(Class<?> clazz, String methodName) throws IOException
    {
        long startTime = System.currentTimeMillis();
        try 
        {
            ClassDef classDef = findClassDef(clazz);
            if (classDef == null)
                return new ArrayList<>();
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
            return new ArrayList<>();
        } 
        finally 
        {
            long endTime = System.currentTimeMillis();
            Logger.d("Disassemble " + clazz.getName() + "->" + methodName + " finished in " + (endTime - startTime) + "ms");
        }
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

    public static String GetDescriptor(Method method)
    {
        if (method == null)
            return "";
        StringBuilder sb = new StringBuilder(GetDescriptor(method.getDeclaringClass()));
        sb.append("->");
        sb.append(method.getName());
        sb.append("(");
        for (Class<?> paramType : method.getParameterTypes())
            sb.append(GetDescriptor(paramType));
        sb.append(")");
        sb.append(GetDescriptor(method.getReturnType()));
        return sb.toString();
    }

    public static String GetDescriptor(Field field)
    {
        if (field == null)
            return "";
        return GetDescriptor(field.getDeclaringClass()) + "->" + field.getName() + ":" + GetDescriptor(field.getType());
    }

    public static String DescriptorToClassName(String descriptor)
    {
        if (descriptor == null || descriptor.isEmpty())
            return "";
        if (descriptor.startsWith("L"))
            descriptor = descriptor.substring(1);
        if (descriptor.endsWith(";"))
            descriptor = descriptor.substring(0, descriptor.length() - 1);
        return descriptor.replace('/', '.');
    }

    public static String GetAppDataDir()
    {
        return appDataDir;
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
                Logger.e("[" + MorpheConstants.getModuleName() + "] Cannot get externalFilesDir:");
                Logger.e(e);
            }
        }
        return externalFilesDir;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String GetZaloXposedDir()
    {
        File targetDir = null;
        if (appDataDir != null && !appDataDir.isEmpty())
            targetDir = new File(appDataDir, "files/ZaloXposed");
        else
        {
            String externalFilesDir = GetExternalFilesDir();
            if (!externalFilesDir.isEmpty())
                targetDir = new File(externalFilesDir, "ZaloXposed");
        }
        if (targetDir == null)
            return "";
        if (!targetDir.exists())
            targetDir.mkdirs();
        return targetDir.getAbsolutePath();
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String GetZaloXposedDir(String appdataDir)
    {
        File targetDir = new File(appdataDir, "files/ZaloXposed");
        if (!targetDir.exists())
            targetDir.mkdirs();
        return targetDir.getAbsolutePath();
    }

    public static int GetDrawableResourceIdByName(String resourceName)
    {
        try
        { 
            if (drawableResourceClasses.isEmpty())
            {
                Logger.e("Drawable resource classes not found");
                return 0;
            }
            Field field = null;
            for (Class<?> clazz : drawableResourceClasses)
            {
                try
                {
                    field = clazz.getField(resourceName);
                }
                catch (NoSuchFieldException ignored) { }
            }
            if (field == null)
            {
                Logger.e("Drawable resource class not found");
                return 0;
            }
            return field.getInt(null);
        }
        catch (Exception e) { Logger.e(e); }
        return 0;
    }

    public static int GetResourceIdByName(String resourceName)
    {
        try
        {
            if (resourceClasses.isEmpty())
            {
                Logger.e("Resource classes not found");
                return 0;
            }
            Field field = null;
            for (Class<?> clazz : resourceClasses)
            {
                try
                {
                    field = clazz.getField(resourceName);
                }
                catch (NoSuchFieldException ignored) { }
            }
            if (field == null)
            {
                Logger.e("Resource class not found");
                return 0;
            }
            return field.getInt(null);
        }
        catch (Exception e) { Logger.e(e); }
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

    public static String HexString(byte[] data)
    {
        StringBuilder sb = new StringBuilder();
        for (byte b : data)
            sb.append(String.format("%02x ", b & 0xff));
        return sb.toString();
    }

    public static boolean IsEnglish()
    {
        if (getIsVietnameseMethod == null)
            return true;
        try
        {
            Boolean bool = (Boolean)getIsVietnameseMethod.invoke(null);
            if (bool == null)
                return false;
            return !bool;
        }
        catch (Exception e) { Logger.e(e); }
        return true;
    }

    public static int GetAppVersionCode()
    {
        return appVersionCode;
    }
}