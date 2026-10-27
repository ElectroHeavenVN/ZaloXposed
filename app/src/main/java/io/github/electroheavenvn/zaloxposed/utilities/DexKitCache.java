package io.github.electroheavenvn.zaloxposed.utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.luckypray.dexkit.result.ClassData;
import org.luckypray.dexkit.result.FieldData;
import org.luckypray.dexkit.result.MethodData;
import org.luckypray.dexkit.wrap.DexClass;
import org.luckypray.dexkit.wrap.DexField;
import org.luckypray.dexkit.wrap.DexMethod;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.github.electroheavenvn.zaloxposed.BuildConfig;

public final class DexKitCache
{
    private DexKitCache() {}

    private static JSONObject cache = new JSONObject();
    private static boolean needsUpdate = false;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void Load(ClassLoader classLoader, String appdataDir) throws Exception
    {
        // Don't use Utils.GetAppVersionCode here, Utils.Init is not called yet, so it will return 0
        Class<?> buildConfig = Class.forName("com.zing.zalo.BuildConfig", false, classLoader);
        Field versionCodeField = buildConfig.getDeclaredField("VERSION_CODE");
        int appVersionCode = versionCodeField.getInt(null);
        File file = new File(Utils.GetZaloXposedDir(appdataDir), "dexkit_cache.json");
        if (!file.exists())
        {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists())
                parent.mkdirs();
            FileWriter fw = new FileWriter(file);
            fw.write("{}");
            fw.close();
        }
        StringBuilder content = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null)
            content.append(line).append("\n");
        reader.close();
        cache = new JSONObject(content.toString());
        boolean isZaloUpdated = cache.optInt("version_code", -1) != appVersionCode;
        boolean isModuleUpdated = cache.optInt("module_version_code", -1) != BuildConfig.MODULE_VERSION_CODE;
        if (isZaloUpdated || isModuleUpdated)
        {
            cache = new JSONObject();
            needsUpdate = true;
        }
        if (cache.optJSONObject("constructors") == null)
            cache.put("constructors", new JSONObject());
        if (cache.optJSONObject("classes") == null)
            cache.put("classes", new JSONObject());
        if (cache.optJSONObject("methods") == null)
            cache.put("methods", new JSONObject());
        if (cache.optJSONObject("fields") == null)
            cache.put("fields", new JSONObject());
    }

    public static synchronized void Save() throws IOException, JSONException
    {
        if (!needsUpdate)
            return;
        cache.put("version_code", Utils.GetAppVersionCode());
        cache.put("module_version_code", BuildConfig.MODULE_VERSION_CODE);
        File file = new File(Utils.GetZaloXposedDir(), "dexkit_cache.json");
        FileWriter fw = new FileWriter(file);
        fw.write(cache.toString());
        fw.close();
        needsUpdate = false;
        Logger.i("Saved DexKit cache to " + file.getAbsolutePath());
    }

    public static synchronized boolean ClearCache()
    {
        try
        {
            cache = new JSONObject();
            needsUpdate = false;
            File file = new File(Utils.GetZaloXposedDir(), "dexkit_cache.json");
            if (file.exists())
                return file.delete();
            return true;
        }
        catch (Exception e)
        {
            Logger.e(e);
            return false;
        }
    }

    @SuppressWarnings("BusyWait")
    public static void AutoSave()
    {
        //TODO: don't auto save
        new Thread(() ->
        {
            while (true)
            {
                try
                {
                    Thread.sleep(10000);
                    Save();
                }
                catch (InterruptedException ignored)
                {
                    break;
                }
                catch (Exception e) { Logger.e(e); }
            }
        }, "DexKitCacheAutoSave").start();
    }

    public static List<Constructor<?>> GetConstructors(String key, Supplier<List<MethodData>> matcher, ClassLoader classLoader)
    {
        try
        {
            JSONObject constructorsObj = cache.getJSONObject("constructors");
            if (constructorsObj.has(key))
            {
                JSONArray cached = constructorsObj.getJSONArray(key);
                List<Constructor<?>> constructors = new ArrayList<>();
                for (int i = 0; i < cached.length(); i++)
                {
                    String descriptor = cached.getString(i);
                    Constructor<?> constructor = new DexMethod(descriptor).getConstructorInstance(classLoader);
                    constructor.setAccessible(true);
                    constructors.add(constructor);
                }
                return constructors;
            }
        }
        catch (Exception e) { Logger.e(e); }
        try
        {
            Logger.i("[Ctor] Cache miss for key: " + key);
            List<MethodData> methodDataList = matcher.get();
            if (methodDataList == null)
                return new ArrayList<>();
            List<Constructor<?>> result = new ArrayList<>();
            JSONArray jsonArray = new JSONArray();
            for (MethodData md : methodDataList)
            {
                try
                {
                    Constructor<?> constructor = md.getConstructorInstance(classLoader);
                    constructor.setAccessible(true);
                    result.add(constructor);
                    jsonArray.put(md.getDescriptor());
                }
                catch (Exception e) { Logger.e(e); }
            }
            JSONObject constructorsObj = cache.optJSONObject("constructors");
            assert constructorsObj != null;
            constructorsObj.put(key, jsonArray);
            needsUpdate = true;
            return result;
        }
        catch (Exception e) { Logger.e(e); }
        return new ArrayList<>();
    }

    public static Constructor<?> GetConstructor(String key, Supplier<List<MethodData>> matcher, ClassLoader classLoader)
    {
        List<Constructor<?>> list = GetConstructors(key, matcher, classLoader);
        if (list.isEmpty())
        {
            LogError("Target constructor not found");
            return null;
        }
        if (list.size() > 1)
            LogWarn("Multiple constructors found");
        return list.get(0);
    }

    public static List<Class<?>> GetClasses(String key, Supplier<List<ClassData>> matcher, ClassLoader classLoader)
    {
        try
        {
            JSONObject classesObj = cache.getJSONObject("classes");
            if (classesObj.has(key))
            {
                JSONArray cached = classesObj.getJSONArray(key);
                List<Class<?>> classes = new ArrayList<>();
                for (int i = 0; i < cached.length(); i++)
                {
                    String descriptor = cached.getString(i);
                    classes.add(new DexClass(descriptor).getInstance(classLoader));
                }
                return classes;
            }
        }
        catch (Exception e) { Logger.e(e); }
        try
        {
            Logger.i("[Class] Cache miss for key: " + key);
            List<ClassData> classDataList = matcher.get();
            if (classDataList == null)
                return new ArrayList<>();
            List<Class<?>> result = new ArrayList<>();
            JSONArray jsonArray = new JSONArray();
            for (ClassData cd : classDataList)
            {
                try
                {
                    Class<?> clazz = cd.getInstance(classLoader);
                    result.add(clazz);
                    jsonArray.put(cd.getDescriptor());
                }
                catch (Exception e) { Logger.e(e); }
            }
            JSONObject classesObj = cache.optJSONObject("classes");
            assert classesObj != null;
            classesObj.put(key, jsonArray);
            needsUpdate = true;
            return result;
        }
        catch (Exception e) { Logger.e(e); }
        return new ArrayList<>();
    }

    public static Class<?> GetClass(String key, Supplier<List<ClassData>> matcher, ClassLoader classLoader)
    {
        List<Class<?>> list = GetClasses(key, matcher, classLoader);
        if (list.isEmpty())
        {
            LogError("Target class not found");
            return null;
        }
        if (list.size() > 1)
            LogWarn("Multiple classes found");
        return list.get(0);
    }

    public static List<Field> GetFields(String key, Supplier<List<FieldData>> matcher, ClassLoader classLoader)
    {
        try
        {
            JSONObject fieldsObj = cache.getJSONObject("fields");
            if (fieldsObj.has(key))
            {
                JSONArray cached = fieldsObj.getJSONArray(key);
                List<Field> fields = new ArrayList<>();
                for (int i = 0; i < cached.length(); i++)
                {
                    String descriptor = cached.getString(i);
                    Field field = new DexField(descriptor).getFieldInstance(classLoader);
                    field.setAccessible(true);
                    fields.add(field);
                }
                return fields;
            }
        }
        catch (Exception e) { Logger.e(e); }
        try
        {
            Logger.i("[Field] Cache miss for key: " + key);
            List<FieldData> fieldDataList = matcher.get();
            if (fieldDataList == null)
                return new ArrayList<>();
            List<Field> result = new ArrayList<>();
            JSONArray jsonArray = new JSONArray();
            for (FieldData fd : fieldDataList)
            {
                try
                {
                    Field field = fd.getFieldInstance(classLoader);
                    field.setAccessible(true);
                    result.add(field);
                    jsonArray.put(fd.getDescriptor());
                }
                catch (Exception e) { Logger.e(e); }
            }
            JSONObject fieldsObj = cache.optJSONObject("fields");
            assert fieldsObj != null;
            fieldsObj.put(key, jsonArray);
            needsUpdate = true;
            return result;
        }
        catch (Exception e) { Logger.e(e); }
        return new ArrayList<>();
    }

    public static Field GetField(String key, Supplier<List<FieldData>> matcher, ClassLoader classLoader)
    {
        List<Field> list = GetFields(key, matcher, classLoader);
        if (list.isEmpty())
        {
            LogError("Target field not found");
            return null;
        }
        if (list.size() > 1)
            LogWarn("Multiple fields found");
        return list.get(0);
    }

    public static List<Method> GetMethods(String key, Supplier<List<MethodData>> matcher, ClassLoader classLoader)
    {
        try
        {
            JSONObject methodsObj = cache.getJSONObject("methods");
            if (methodsObj.has(key))
            {
                JSONArray cached = methodsObj.getJSONArray(key);
                List<Method> methods = new ArrayList<>();
                for (int i = 0; i < cached.length(); i++)
                {
                    String descriptor = cached.getString(i);
                    Method method = new DexMethod(descriptor).getMethodInstance(classLoader);
                    method.setAccessible(true);
                    methods.add(method);
                }
                return methods;
            }
        }
        catch (Exception e) { Logger.e(e); }
        try
        {
            Logger.i("[Method] Cache miss for key: " + key);
            List<MethodData> methodDataList = matcher.get();
            if (methodDataList == null)
                return new ArrayList<>();
            List<Method> result = new ArrayList<>();
            JSONArray jsonArray = new JSONArray();
            for (MethodData md : methodDataList)
            {
                try
                {
                    Method method = md.getMethodInstance(classLoader);
                    method.setAccessible(true);
                    result.add(method);
                    jsonArray.put(md.getDescriptor());
                }
                catch (Exception e) { Logger.e(e); }
            }
            JSONObject methodsObj = cache.optJSONObject("methods");
            assert methodsObj != null;
            methodsObj.put(key, jsonArray);
            needsUpdate = true;
            return result;
        }
        catch (Exception e) { Logger.e(e); }
        return new ArrayList<>();
    }

    public static Method GetMethod(String key, Supplier<List<MethodData>> matcher, ClassLoader classLoader)
    {
        List<Method> list = GetMethods(key, matcher, classLoader);
        if (list.isEmpty())
        {
            LogError("Target method not found");
            return null;
        }
        if (list.size() > 1)
            LogWarn("Multiple methods found");
        return list.get(0);
    }

    public static List<Constructor<?>> GetConstructorsRaw(String key, ClassLoader classLoader)
    {
        try
        {
            JSONObject constructorsObj = cache.getJSONObject("constructors");
            if (constructorsObj.has(key))
            {
                JSONArray cached = constructorsObj.getJSONArray(key);
                List<Constructor<?>> constructors = new ArrayList<>();
                for (int i = 0; i < cached.length(); i++)
                {
                    String descriptor = cached.getString(i);
                    Constructor<?> constructor = new DexMethod(descriptor).getConstructorInstance(classLoader);
                    constructor.setAccessible(true);
                    constructors.add(constructor);
                }
                return constructors;
            }
        }
        catch (Exception e) { Logger.e(e); }
        return new ArrayList<>();
    }

    public static Constructor<?> GetConstructorRaw(String key, ClassLoader classLoader)
    {
        List<Constructor<?>> list = GetConstructorsRaw(key, classLoader);
        if (list.isEmpty())   
        {
            LogError("Target constructor not found");
            return null;
        }
        if (list.size() > 1)
            LogWarn("Multiple constructors found");
        return list.get(0);
    }

    public static void SetConstructorsRaw(String key, List<Constructor<?>> constructors)
    {
        try
        {
            JSONArray jsonArray = new JSONArray();
            if (constructors != null)
            {
                for (Constructor<?> c : constructors)
                    jsonArray.put(new DexMethod(c).toString());
            }
            JSONObject constructorsObj = cache.optJSONObject("constructors");
            assert constructorsObj != null;
            constructorsObj.put(key, jsonArray);
            needsUpdate = true;
        }
        catch (Exception e) { Logger.e(e); }
    }

    public static void SetConstructorRaw(String key, Constructor<?> constructor)
    {
        SetConstructorsRaw(key, List.of(constructor));
    }

    public static List<Class<?>> GetClassesRaw(String key, ClassLoader classLoader)
    {
        try
        {
            JSONObject classesObj = cache.getJSONObject("classes");
            if (classesObj.has(key))
            {
                JSONArray cached = classesObj.getJSONArray(key);
                List<Class<?>> classes = new ArrayList<>();
                for (int i = 0; i < cached.length(); i++)
                {
                    String descriptor = cached.getString(i);
                    classes.add(new DexClass(descriptor).getInstance(classLoader));
                }
                return classes;
            }
        }
        catch (Exception e) { Logger.e(e); }
        return new ArrayList<>();
    }

    public static Class<?> GetClassRaw(String key, ClassLoader classLoader)
    {
        List<Class<?>> list = GetClassesRaw(key, classLoader);
        if (list.isEmpty())
        {
            LogError("Target class not found");
            return null;
        }
        if (list.size() > 1)
            LogWarn("Multiple classes found");
        return list.get(0);
    }

    public static void SetClassesRaw(String key, List<Class<?>> classes)
    {
        try
        {
            JSONArray jsonArray = new JSONArray();
            if (classes != null)
            {
                for (Class<?> c : classes)
                    jsonArray.put(new DexClass(c).toString());
            }
            JSONObject classesObj = cache.optJSONObject("classes");
            assert classesObj != null;
            classesObj.put(key, jsonArray);
            needsUpdate = true;
        }
        catch (Exception e) { Logger.e(e); }
    }

    public static void SetClassRaw(String key, Class<?> clazz)
    {
        SetClassesRaw(key, List.of(clazz));
    }

    public static List<Field> GetFieldsRaw(String key, ClassLoader classLoader)
    {
        try
        {
            JSONObject fieldsObj = cache.getJSONObject("fields");
            if (fieldsObj.has(key))
            {
                JSONArray cached = fieldsObj.getJSONArray(key);
                List<Field> fields = new ArrayList<>();
                for (int i = 0; i < cached.length(); i++)
                {
                    String descriptor = cached.getString(i);
                    Field field = new DexField(descriptor).getFieldInstance(classLoader);
                    field.setAccessible(true);
                    fields.add(field);
                }
                return fields;
            }
        }
        catch (Exception e) { Logger.e(e); }
        return new ArrayList<>();
    }

    public static Field GetFieldRaw(String key, ClassLoader classLoader)
    {
        List<Field> list = GetFieldsRaw(key, classLoader);
        if (list.isEmpty())   
        {
            LogError("Target field not found");
            return null;
        }
        if (list.size() > 1)
            LogWarn("Multiple fields found");
        return list.get(0);
    }

    public static void SetFieldsRaw(String key, List<Field> fields)
    {
        try
        {
            JSONArray jsonArray = new JSONArray();
            if (fields != null)
            {
                for (Field f : fields)
                    jsonArray.put(new DexField(f).toString());
            }
            JSONObject fieldsObj = cache.optJSONObject("fields");
            assert fieldsObj != null;
            fieldsObj.put(key, jsonArray);
            needsUpdate = true;
        }
        catch (Exception e) { Logger.e(e); }
    }

    public static void SetFieldRaw(String key, Field field)
    {
        SetFieldsRaw(key, List.of(field));
    }

    public static List<Method> GetMethodsRaw(String key, ClassLoader classLoader)
    {
        try
        {
            JSONObject methodsObj = cache.getJSONObject("methods");
            if (methodsObj.has(key))
            {
                JSONArray cached = methodsObj.getJSONArray(key);
                List<Method> methods = new ArrayList<>();
                for (int i = 0; i < cached.length(); i++)
                {
                    String descriptor = cached.getString(i);
                    Method method = new DexMethod(descriptor).getMethodInstance(classLoader);
                    method.setAccessible(true);
                    methods.add(method);
                }
                return methods;
            }
        }
        catch (Exception e) { Logger.e(e); }
        return new ArrayList<>();
    }

    public static Method GetMethodRaw(String key, ClassLoader classLoader)
    {
        List<Method> list = GetMethodsRaw(key, classLoader);
        if (list.isEmpty())   
        {
            LogError("Target method not found");
            return null;
        }
        if (list.size() > 1)
            LogWarn("Multiple methods found");
        return list.get(0);
    }

    public static void SetMethodsRaw(String key, List<Method> methods)
    {
        try
        {
            JSONArray jsonArray = new JSONArray();
            if (methods != null)
            {
                for (Method m : methods)
                    jsonArray.put(new DexMethod(m).toString());
            }
            JSONObject methodsObj = cache.optJSONObject("methods");
            assert methodsObj != null;
            methodsObj.put(key, jsonArray);
            needsUpdate = true;
        }
        catch (Exception e) { Logger.e(e); }
    }

    public static void SetMethodRaw(String key, Method method)
    {
        SetMethodsRaw(key, List.of(method));
    }
    
    private static String getPrefix()
    {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 3; i < stackTrace.length; i++)
        {
            StackTraceElement element = stackTrace[i];
            String className = element.getClassName();
            if (!className.equals(DexKitCache.class.getName()))
                return "{" + className.substring(className.lastIndexOf('.') + 1) + "} ";
        }
        return "";
    }
    
    public static void LogWarn(String msg)
    {
        Logger.w(getPrefix() + msg);
    }

    public static void LogError(String msg)
    {
        Logger.e(getPrefix() + msg);
    }
}