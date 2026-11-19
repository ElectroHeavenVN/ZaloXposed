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
import java.util.List;

public final class ResourceHelper
{
    private ResourceHelper() { }

    private static final ArrayList<Class<?>> resourceClasses = new ArrayList<>();

    public static void Init(DexKitBridge bridge, ClassLoader classLoader)
    {
        resourceClasses.clear();
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
                if (IsResourceClass(clazz))
                    resourceClasses.add(clazz);
            }
        }
        catch (Throwable t) { Logger.e(t); }
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
                    field = clazz.getDeclaredField(resourceName);
                    if (field.getType() != int.class)
                    {
                        field = null;
                        continue;
                    }
                    field.setAccessible(true);
                    break;
                }
                catch (NoSuchFieldException ignored) { }
            }
            if (field == null)
            {
                Logger.e("Target resource field not found");
                return 0;
            }
            return field.getInt(null);
        }
        catch (Exception e) { Logger.e(e); }
        return 0;
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