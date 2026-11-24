package io.github.electroheavenvn.zaloxposed.utilities;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.FieldMatcher;
import org.luckypray.dexkit.query.matchers.FieldsMatcher;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ResourceHelper
{
    private ResourceHelper() { }

    private static final ArrayList<Class<?>> resourceClasses = new ArrayList<>();
    private static final Map<String, List<Class<?>>> resourceClassesByType = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Integer>> resourceMapByType = new ConcurrentHashMap<>();
    private static final Map<Integer, String> typeIdToTypeName = new ConcurrentHashMap<>();
    private static final Map<String, Integer> typeNameToTypeId = new ConcurrentHashMap<>();

    private static void registerType(int typeByte, String typeName)
    {
        if (typeName == null)
            return;
        String lower = typeName.toLowerCase(Locale.ROOT);
        typeIdToTypeName.put(typeByte, lower);
        typeNameToTypeId.put(lower, typeByte);
    }

    private static void initDefaultTypeMappings()
    {
        registerType(0x01, "anim");
        registerType(0x02, "animator");
        registerType(0x03, "interpolator");
        registerType(0x04, "attr");
        registerType(0x05, "bool");
        registerType(0x06, "color");
        registerType(0x07, "dimen");
        registerType(0x08, "drawable");
        registerType(0x09, "id");
        registerType(0x0a, "integer");
        registerType(0x0b, "interpolator");
        registerType(0x0c, "layout");
        registerType(0x0d, "menu");
        registerType(0x0e, "mipmap");
        registerType(0x0f, "navigation");
        registerType(0x10, "plurals");
        registerType(0x11, "raw");
        registerType(0x12, "string");
        registerType(0x13, "style");
        registerType(0x14, "styleable");
        registerType(0x15, "transition");
        registerType(0x16, "xml");
    }

    static
    {
        initDefaultTypeMappings();
    }

    public static void Init(DexKitBridge bridge, ClassLoader classLoader)
    {
        resourceClasses.clear();
        resourceClassesByType.clear();
        resourceMapByType.clear();
        initDefaultTypeMappings();
        try
        {
            List<Class<?>> candidates = DexKitCache.GetClasses("ResourceHelper_Candidates", () -> bridge.findClass(FindClass.create()
                .matcher(ClassMatcher.create()
                    .superClass("java.lang.Object")
                    .methodCount(0)
                    .not(ClassMatcher.create().className("androidx.", StringMatchType.StartsWith))
                    .not(ClassMatcher.create().className("org.", StringMatchType.StartsWith))
                    .not(ClassMatcher.create().className("com.", StringMatchType.StartsWith))
                    .not(ClassMatcher.create().className("kotlinx.", StringMatchType.StartsWith))
                    .fields(FieldsMatcher.create()
                        .add(FieldMatcher.create()
                            .modifiers(Modifier.STATIC)
                            .type(int.class)
                        )
                    )
                )), classLoader);
            for (Class<?> clazz : candidates)
            {
                if (!IsResourceClass(clazz))
                    continue;
                resourceClasses.add(clazz);
                classifyClass(clazz);
            }
        }
        catch (Throwable t) { Logger.e(t); }
    }

    private static void classifyClass(Class<?> clazz)
    {
        Field[] fields = clazz.getDeclaredFields();
        int detectedTypeByte = -1;
        for (Field f : fields)
        {
            if (f.getType() != int.class || !Modifier.isStatic(f.getModifiers()))
                continue;
            f.setAccessible(true);
            int resId;
            try
            {
                resId = f.getInt(null);
            }
            catch (Throwable t)
            {
                continue;
            }
            if ((resId >>> 24) == 0x7f)
            {
                detectedTypeByte = (resId >>> 16) & 0xff;
                break;
            }
        }
        if (detectedTypeByte == -1)
            return;
        String finalTypeName = typeIdToTypeName.get(detectedTypeByte);
        if (finalTypeName == null)
        {
            for (Field f : fields)
            {
                String inferred = inferTypeFromName(f.getName());
                if (inferred != null)
                {
                    registerType(detectedTypeByte, inferred);
                    finalTypeName = inferred;
                    break;
                }
            }
        }
        if (finalTypeName == null)
        {
            finalTypeName = "type_0x" + Integer.toHexString(detectedTypeByte);
            registerType(detectedTypeByte, finalTypeName);
        }
        resourceClassesByType.computeIfAbsent(finalTypeName, k -> new ArrayList<>()).add(clazz);
        for (Field f : fields)
        {
            if (f.getType() != int.class || !Modifier.isStatic(f.getModifiers()))
                continue;
            f.setAccessible(true);
            int resId;
            try
            {
                resId = f.getInt(null);
            }
            catch (Throwable t)
            {
                continue;
            }
            if ((resId >>> 24) != 0x7f)
                continue;
            int fTypeByte = (resId >>> 16) & 0xff;
            String fTypeName = typeIdToTypeName.get(fTypeByte);
            if (fTypeName == null)
                fTypeName = finalTypeName;
            resourceMapByType.computeIfAbsent(fTypeName, k -> new ConcurrentHashMap<>()).put(f.getName(), resId);
        }
    }

    private static String inferTypeFromName(String name)
    {
        if (name == null || name.isEmpty())
            return null;
        if (name.startsWith("ic_") || name.startsWith("btn_") || name.startsWith("bg_") ||
            name.startsWith("img_") || name.startsWith("selector_") || name.startsWith("layer_") ||
            name.startsWith("shape_") || name.startsWith("ripple_") || name.startsWith("vector_"))
            return "drawable";
        if (name.startsWith("str_") || name.startsWith("txt_") || name.startsWith("msg_") ||
            name.startsWith("label_") || name.startsWith("hint_") || name.startsWith("err_"))
            return "string";
        if (name.startsWith("layout_") || name.endsWith("_layout") || name.endsWith("_view") ||
            name.endsWith("_fragment") || name.endsWith("_activity") || name.endsWith("_dialog"))
            return "layout";
        if (name.startsWith("color_") || name.startsWith("clr_"))
            return "color";
        if (name.startsWith("dimen_") || name.startsWith("dp_") || name.startsWith("sp_"))
            return "dimen";
        if (name.startsWith("anim_"))
            return "anim";
        if (name.startsWith("menu_"))
            return "menu";
        if (name.startsWith("raw_"))
            return "raw";
        return null;
    }

    public static int GetResourceIdByName(String type, String resourceName)
    {
        if (resourceName == null || resourceName.isEmpty())
            return 0;
        int slashIdx = resourceName.indexOf('/');
        if (slashIdx != -1)
        {
            type = resourceName.substring(0, slashIdx);
            resourceName = resourceName.substring(slashIdx + 1);
        }
        if (type == null || type.isEmpty())
            return GetResourceIdByName(resourceName);
        String normType = type.toLowerCase(Locale.ROOT).trim();
        Map<String, Integer> map = resourceMapByType.get(normType);
        if (map != null)
        {
            Integer val = map.get(resourceName);
            if (val != null)
                return val;
        }
        Integer expectedTypeId = typeNameToTypeId.get(normType);
        List<Class<?>> classes = resourceClassesByType.get(normType);
        if (classes != null)
        {
            for (Class<?> clazz : classes)
            {
                try
                {
                    Field field = clazz.getDeclaredField(resourceName);
                    if (field.getType() == int.class && Modifier.isStatic(field.getModifiers()))
                    {
                        field.setAccessible(true);
                        int val = field.getInt(null);
                        if (expectedTypeId == null || ((val >>> 16) & 0xff) == expectedTypeId)
                        {
                            if (map != null)
                                map.put(resourceName, val);
                            return val;
                        }
                    }
                }
                catch (NoSuchFieldException ignored) { }
                catch (Throwable t) { Logger.e(t); }
            }
        }
        for (Class<?> clazz : resourceClasses)
        {
            try
            {
                Field field = clazz.getDeclaredField(resourceName);
                if (field.getType() == int.class && Modifier.isStatic(field.getModifiers()))
                {
                    field.setAccessible(true);
                    int val = field.getInt(null);
                    if (expectedTypeId == null || ((val >>> 16) & 0xff) == expectedTypeId)
                    {
                        if (map != null)
                            map.put(resourceName, val);
                        return val;
                    }
                }
            }
            catch (NoSuchFieldException ignored) { }
            catch (Throwable t) { Logger.e(t); }
        }
        Logger.e("Resource field not found for type '" + normType + "': " + resourceName);
        return 0;
    }

    public static int GetResourceIdByName(int typeByte, String resourceName)
    {
        if (resourceName == null || resourceName.isEmpty())
            return 0;
        String typeName = typeIdToTypeName.get(typeByte);
        if (typeName != null)
            return GetResourceIdByName(typeName, resourceName);
        for (Class<?> clazz : resourceClasses)
        {
            try
            {
                Field field = clazz.getDeclaredField(resourceName);
                if (field.getType() == int.class && Modifier.isStatic(field.getModifiers()))
                {
                    field.setAccessible(true);
                    int val = field.getInt(null);
                    if (((val >>> 16) & 0xff) == typeByte)
                        return val;
                }
            }
            catch (NoSuchFieldException ignored) { }
            catch (Throwable t) { Logger.e(t); }
        }
        return 0;
    }

    public static int GetResourceIdByName(String resourceName)
    {
        if (resourceName == null || resourceName.isEmpty())
            return 0;
        int slashIdx = resourceName.indexOf('/');
        if (slashIdx != -1)
        {
            String type = resourceName.substring(0, slashIdx);
            String name = resourceName.substring(slashIdx + 1);
            return GetResourceIdByName(type, name);
        }
        String inferredType = inferTypeFromName(resourceName);
        if (inferredType != null)
        {
            Map<String, Integer> map = resourceMapByType.get(inferredType);
            if (map != null)
            {
                Integer val = map.get(resourceName);
                if (val != null)
                    return val;
            }
        }
        List<String> foundTypes = new ArrayList<>();
        int lastFoundId = 0;
        for (Map.Entry<String, Map<String, Integer>> entry : resourceMapByType.entrySet())
        {
            Integer val = entry.getValue().get(resourceName);
            if (val != null)
            {
                foundTypes.add(entry.getKey());
                lastFoundId = val;
            }
        }
        if (foundTypes.size() == 1)
            return lastFoundId;
        else if (foundTypes.size() > 1)
        {
            Logger.w("Ambiguous resource name '" + resourceName + "' found in types: " + foundTypes +
                ". Please specify resource type using GetResourceIdByName(type, name) or Get<Type>(name).");
            if (inferredType != null && foundTypes.contains(inferredType))
            {
                Map<String, Integer> map = resourceMapByType.get(inferredType);
                if (map != null)
                {
                    Integer val = map.get(resourceName);
                    if (val != null)
                        return val;
                }
            }
            for (String pref : new String[]{"id", "layout", "drawable", "string", "color", "dimen"})
            {
                if (foundTypes.contains(pref))
                {
                    Map<String, Integer> map = resourceMapByType.get(pref);
                    if (map != null)
                    {
                        Integer val = map.get(resourceName);
                        if (val != null)
                            return val;
                    }
                }
            }
            return lastFoundId;
        }
        for (Class<?> clazz : resourceClasses)
        {
            try
            {
                Field field = clazz.getDeclaredField(resourceName);
                if (field.getType() == int.class && Modifier.isStatic(field.getModifiers()))
                {
                    field.setAccessible(true);
                    return field.getInt(null);
                }
            }
            catch (NoSuchFieldException ignored) { }
            catch (Throwable t) { Logger.e(t); }
        }
        Logger.e("Target resource field not found: " + resourceName);
        return 0;
    }

    public static int GetId(String name)
    {
        return GetResourceIdByName("id", name);
    }

    public static int GetDrawable(String name)
    {
        return GetResourceIdByName("drawable", name);
    }

    public static int GetString(String name)
    {
        return GetResourceIdByName("string", name);
    }

    public static int GetLayout(String name)
    {
        return GetResourceIdByName("layout", name);
    }

    public static int GetColor(String name)
    {
        return GetResourceIdByName("color", name);
    }

    public static int GetDimen(String name)
    {
        return GetResourceIdByName("dimen", name);
    }

    public static int GetAnim(String name)
    {
        return GetResourceIdByName("anim", name);
    }

    public static int GetAnimator(String name)
    {
        return GetResourceIdByName("animator", name);
    }

    public static int GetMenu(String name)
    {
        return GetResourceIdByName("menu", name);
    }

    public static int GetRaw(String name)
    {
        return GetResourceIdByName("raw", name);
    }

    public static int GetStyle(String name)
    {
        return GetResourceIdByName("style", name);
    }

    public static int GetAttr(String name)
    {
        return GetResourceIdByName("attr", name);
    }

    public static int GetArray(String name)
    {
        return GetResourceIdByName("array", name);
    }

    public static int GetPlurals(String name)
    {
        return GetResourceIdByName("plurals", name);
    }

    public static int GetXml(String name)
    {
        return GetResourceIdByName("xml", name);
    }

    public static int GetMipmap(String name)
    {
        return GetResourceIdByName("mipmap", name);
    }

    public static int GetBool(String name)
    {
        return GetResourceIdByName("bool", name);
    }

    public static int GetInteger(String name)
    {
        return GetResourceIdByName("integer", name);
    }

    public static int GetFont(String name)
    {
        return GetResourceIdByName("font", name);
    }

    public static String GetResourceTypeName(int resId)
    {
        if ((resId >>> 24) != 0x7f)
            return null;
        int typeByte = (resId >>> 16) & 0xff;
        return typeIdToTypeName.get(typeByte);
    }

    public static int GetResourceTypeByte(int resId)
    {
        if ((resId >>> 24) != 0x7f)
            return -1;
        return (resId >>> 16) & 0xff;
    }

    public static List<Class<?>> GetResourceClasses()
    {
        return Collections.unmodifiableList(resourceClasses);
    }

    public static List<Class<?>> GetResourceClasses(String type)
    {
        if (type == null)
            return Collections.emptyList();
        List<Class<?>> list = resourceClassesByType.get(type.toLowerCase(Locale.ROOT));
        return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
    }

    private static boolean IsResourceClass(Class<?> clazz)
    {
        if (clazz.isInterface() || clazz.isEnum() || clazz.isArray() || clazz.isPrimitive())
            return false;
        if (clazz.getSuperclass() != Object.class || clazz.getInterfaces().length != 0)
            return false;
        Field[] fields = clazz.getDeclaredFields();
        if (fields.length == 0)
            return false;
        // R class does not have any methods
        if (clazz.getDeclaredMethods().length != 0 || clazz.getDeclaredConstructors().length > 1)
            return false;
        int intCount = 0;
        for (Field f : fields)
        {
            int mod = f.getModifiers();
            if (!Modifier.isStatic(mod))
                return false;
            Class<?> type = f.getType();
            if (type == int[].class)
                continue; // styleable
            if (type != int.class)
                return false;
            f.setAccessible(true);
            int id;
            try
            {
                id = f.getInt(null);
            }
            catch (Throwable t)
            {
                return false;
            }
            if ((id >>> 24) != 0x7f)
            {
                if (id >= 0 && id < 0x10000)
                    continue;
                return false;
            }
            intCount++;
        }
        return intCount != 0;
    }
}