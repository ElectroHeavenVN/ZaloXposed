package com.ehvn.zaloxposed.hooks;

import com.ehvn.zaloxposed.utilities.Utils;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

@SuppressWarnings("unused")
public class TTLHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("void")
                .paramCount(0)
                .addUsingString("LAST_TIME_LOAD_TTL_CONFIG_", StringMatchType.Equals)
            ));
        List<MethodData> formatTimeMethods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("java.lang.String")
                .paramCount(2)
                .addUsingNumber(86400000)
                .addUsingNumber(3600000)
                .addUsingNumber(60000)
                .addUsingNumber(1000)
            ));
        Set<String> classes = new HashSet<>();
        for (MethodData m : methods)
            classes.add(m.getClassName());
        String targetClassName = null;
        for (MethodData m : formatTimeMethods)
        {
            if (classes.contains(m.getClassName()))
            {
                targetClassName = m.getClassName();
                break;
            }
        }
        if (targetClassName == null)
        {
            log("Target class not found");
            return;
        }
        log("Found target class: " + targetClassName);
        List<MethodData> ttlMethods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(targetClassName)
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returnType("long")
                .paramTypes("java.lang.String")
            ));
        for (MethodData md : ttlMethods)
        {
            Method method = md.getMethodInstance(lpparam.classLoader);
            log("Hooking: " + method);
            XposedBridge.hookMethod(method, new XC_MethodReplacement()
            {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param)
                {
                    return readTTLFromFile();
                }
            });
        }
    }

    private long readTTLFromFile()
    {
        try
        {
            String externalFilesDir = Utils.GetExternalFilesDir();
            if (externalFilesDir.isEmpty())
            {
                log("externalFilesDir not initialized");
                return 1000L * 60L * 5L;
            }
            File dir = new File(externalFilesDir, "zaloxposed");
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(dir, "ttl.txt");
            if (!file.exists())
            {
                FileWriter fw = new FileWriter(file);
                fw.write("300000");
                fw.close();
                log("Created " + file.getAbsolutePath());
                return 1000L * 60L * 5L;
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            br.close();
            if (line != null && !line.trim().isEmpty())
                return Long.parseLong(line.trim());
        }
        catch (Exception e)
        {
            log("Read config error: " + e.getMessage());
        }
        return 1000L * 60L * 5L;
    }
}