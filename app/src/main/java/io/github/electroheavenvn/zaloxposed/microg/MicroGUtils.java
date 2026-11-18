package io.github.electroheavenvn.zaloxposed.microg;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.electroheavenvn.zaloxposed.MorpheConstants;
import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.CoreUtilityHelper;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;
import io.github.electroheavenvn.zaloxposed.utilities.S;

public final class MicroGUtils
{
    private MicroGUtils() { }

    public static final String ACCOUNTS_PERMISSION = "android.permission.GET_ACCOUNTS";
    private static final Map<String, Boolean> installedPackages = new ConcurrentHashMap<>();

    public static void ShowAlertDialogMicroGMissing(Activity activity)
    {
        MicroGVariant selectedVariant = MicroGVariant.GetSelectedVariant();
        if (selectedVariant == null)
            return;
        try
        {
            new AlertDialog.Builder(activity)
                .setTitle(S.tr("microGMissingTitle", selectedVariant.displayName))
                .setMessage(S.tr("microGMissingDesc", selectedVariant.displayName))
                .setNegativeButton(S.tr("cancel"), null)
                .setPositiveButton(S.tr("gotoDownload"), (dialog, which) ->
                {
                    try
                    {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(selectedVariant.downloadUrl)));
                    }
                    catch (ActivityNotFoundException ignored) { }
                }).show();
        }
        catch (RuntimeException ignored) { }
    }

    public static int GetMicroGVersionCode(Context context, String packageName)
    {
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                return (int)(context.getPackageManager().getPackageInfo(packageName, 0).getLongVersionCode() & 0xffffffffL);
            //noinspection deprecation
            return context.getPackageManager().getPackageInfo(packageName, 0).versionCode;
        }
        catch (PackageManager.NameNotFoundException ignored) { }
        return 0;
    }

    public static MicroGVariant GetEnabledVariant()
    {
        if (!MorpheConstants.isEnableMicroG())
            return null;
        return MicroGVariant.GetSelectedVariant();
    }

    public static boolean IsPackageInstalled(Context context, String packageName)
    {
        if (TextUtils.isEmpty(packageName))
            return false;
        Boolean cached = installedPackages.get(packageName);
        if (cached != null)
            return cached;
        if (context == null)
            context = CoreUtilityHelper.GetAppContext();
        if (context == null)
            return false; // app context not available yet, do not cache the failure
        boolean result = false;
        try
        {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            if (packageInfo.applicationInfo == null)
                throw new NullPointerException();
            result = packageInfo.applicationInfo.enabled;
        }
        catch (Throwable ignored)
        {
            try
            {
                for (AuthenticatorDescription description : AccountManager.get(context).getAuthenticatorTypes())
                {
                    if (!packageName.equals(description.packageName))
                        continue;
                    result = true;
                    break;
                }
            }
            catch (Throwable ignored2) { }
        }
        if (result)
            installedPackages.put(packageName, true);
        return result;
    }

    public static boolean HasGetAccountsPermission(Context context)
    {
        if (context == null)
            context = CoreUtilityHelper.GetAppContext();
        if (context == null)
            return false;
        try
        {
            return context.checkSelfPermission(ACCOUNTS_PERMISSION) == PackageManager.PERMISSION_GRANTED;
        }
        catch (Throwable ignored) { }
        return false;
    }

    @SuppressWarnings("deprecation")
    public static Account[] QueryAccounts(Context context, MicroGVariant variant)
    {
        if (variant == null)
            return null;
        if (context == null)
            context = CoreUtilityHelper.GetAppContext();
        if (context == null)
            return null;
        try
        {
            Uri uri = Uri.parse("content://" + variant.packageName + ".auth.accounts");
            Bundle result = context.getContentResolver().call(uri, "get_accounts", variant.accountType, null);
            if (result == null)
                return null;
            Parcelable[] parcelables;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                parcelables = result.getParcelableArray("accounts", Bundle.class);
            else
                parcelables = result.getParcelableArray("accounts");
            if (parcelables == null)
                return new Account[0];
            List<Account> accounts = new ArrayList<>(parcelables.length);
            for (Parcelable parcelable : parcelables)
            {
                if (parcelable instanceof Account account)
                    accounts.add(account);
            }
            return accounts.toArray(new Account[0]);
        }
        catch (Throwable t) { Logger.e(t); }
        return null;
    }

    public static Context FindContext(Object instance)
    {
        if (instance instanceof Context context)
            return context;
        if (instance == null)
            return null;
        for (Class<?> clazz = instance.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass())
        {
            for (Field field : clazz.getDeclaredFields())
            {
                if (!Context.class.isAssignableFrom(field.getType()))
                    continue;
                try
                {
                    field.setAccessible(true);
                    if (field.get(instance) instanceof Context context)
                        return context;
                }
                catch (Throwable ignored) { }
            }
        }
        return null;
    }
}