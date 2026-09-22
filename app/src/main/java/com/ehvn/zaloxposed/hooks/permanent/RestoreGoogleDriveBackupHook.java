package com.ehvn.zaloxposed.hooks.permanent;

import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Logger;

import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.ClassData;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class RestoreGoogleDriveBackupHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        hookShowNewGDriveBackupSection();
        hookRestoreOldGDriveBackupSection();
    }

    private void hookShowNewGDriveBackupSection() throws ClassNotFoundException, NoSuchMethodException
    {
        // Show new Google Drive backup section in Text message backup settings
        // This section is marked as "Maintenance-only features" ?
        // They tried really hard to push users to buy zCloud subscription!
        List<ClassData> classes = bridge.findClass(FindClass.create()
            .matcher(ClassMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.ABSTRACT)
                .addUsingString("?e2esession=", StringMatchType.Equals)
                .addUsingString("&version=", StringMatchType.Equals)
                .addUsingString("provideZaloCloudCriticalCaseManager(...)", StringMatchType.Equals)
                .addUsingString("getMessageId(...)", StringMatchType.Equals)
                .addUsingString("provideZaloCloudRepo(...)", StringMatchType.Equals)
                .addUsingString("quotaUsage", StringMatchType.Equals)
                .addUsingString("groupId", StringMatchType.Equals)
                .addUsingString("workManagerImpl.workDatabase", StringMatchType.Equals)
                // bruh
                .addUsingString("zalo@123ZALO", StringMatchType.Equals)
            ));
        if (classes.isEmpty())
        {
            Logger.e("Target class not found 2");
            return;
        }
        Class<?> zCloudManager = classes.get(0).getInstance(classLoader);
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(zCloudManager)
                .modifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)
                .returnType("boolean")
                .paramCount(0)
                .addInvoke(MethodMatcher.create()
                    .declaredClass(zCloudManager)
                    .modifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)
                    .returnType("boolean")
                    .paramCount(0)
                    .addInvoke(MethodMatcher.create()
                        .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                        .returnType("int")
                        .paramCount(0)
                        .addUsingString("ZALO_CLOUD_SUBSCRIPTION_PLAN_", StringMatchType.Equals)
                    )
                    .addInvoke(MethodMatcher.create()
                        .declaredClass(zCloudManager)
                        .modifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)
                        .returnType("boolean")
                        .paramCount(0)
                    )
                )
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found 2");
            return;
        }
        for (MethodData methodData : methods)
        {
            Method method = methodData.getMethodInstance(classLoader);
            Logger.i("Hooking: " + method);
            module.hook(method).intercept(chain -> true);
        }
    }

    private void hookRestoreOldGDriveBackupSection() throws ClassNotFoundException, NoSuchMethodException
    {
        // Restore old Google Drive backup section in Text message backup settings
        List<ClassData> classes = bridge.findClass(FindClass.create()
            .matcher(ClassMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .addUsingString("OLD_BACKUP_THRESHOLD", StringMatchType.Equals)
                .addUsingString("AUTO_BACKUP_PENDING_TIME", StringMatchType.Equals)
                .addUsingString("SMLBackupManager", StringMatchType.Equals)
                .addUsingString("reason", StringMatchType.Equals)
                .addUsingString("BACKUP_RESTORE_SKIP_REASON_", StringMatchType.Equals)
                // Matches log strings, may not be reliable
                .addUsingString("Auto-backup setting: ", StringMatchType.Equals)
                .addUsingString("Wifi-only mode: ", StringMatchType.Equals)
                .addUsingString("Backup Info: ", StringMatchType.Equals)
                .addUsingString("Last restore TS: ", StringMatchType.Equals)
            ));
        if (classes.isEmpty())
        {
            Logger.e("Target class not found 1");
            return;
        }
        Class<?> smlBackupManager = classes.get(0).getInstance(classLoader);
        List<MethodData> methods = bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .declaredClass(smlBackupManager)
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("boolean")
                .paramCount(0)
                .addUsingNumber(2)
                .addUsingNumber(3)
            ));
        if (methods.isEmpty())
        {
            Logger.e("Target method not found 1");
            return;
        }
        for (MethodData methodData : methods)
        {
            Method method = methodData.getMethodInstance(classLoader);
            Logger.i("Hooking: " + method);
            module.hook(method).intercept(chain -> true);
        }
    }
}