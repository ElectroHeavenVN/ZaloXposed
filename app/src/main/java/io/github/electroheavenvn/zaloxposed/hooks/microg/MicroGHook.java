package io.github.electroheavenvn.zaloxposed.hooks.microg;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.FieldMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.electroheavenvn.zaloxposed.MorpheConstants;
import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.microg.MicroGUtils;
import io.github.electroheavenvn.zaloxposed.microg.MicroGVariant;
import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.CoreUtilityHelper;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;

public class MicroGHook extends BaseHook
{
    private static final int MAX_ACCOUNTS_PERMISSION_ATTEMPTS = 5;
    private static final int REQUEST_ACCOUNTS_PERMISSION = 0x676767;
    private static volatile boolean requestingAccountsPerm = false;
    private static final AtomicInteger reqAccountsPermAttempts = new AtomicInteger();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static volatile WeakReference<Activity> currentActivity = new WeakReference<>(null);
    private static volatile Method invalidateAccountList;
    private static volatile WeakReference<Object> accountListInst = new WeakReference<>(null);
    private static volatile String accountListSelection;

    @Override
    public void installHooks() throws Throwable
    {
        invalidateAccountList = DexKitCache.GetMethod("MicroG_InvalidateAccountList", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("void")
                .paramCount(1)
                .paramTypes("java.lang.String")
                .addUsingString("invalidateDataListGoogleAccount, selectedAccountParam=", StringMatchType.Equals)
                .addUsingString("com.google", StringMatchType.Equals)
                .addUsingField(FieldMatcher.create().name("sync_choose_account_option_add"))
            )), classLoader);
        if (invalidateAccountList == null)
            return;
        doHook(invalidateAccountList, 5, chain ->
        {
            if (!MorpheConstants.isEnableMicroG())
                return chain.proceed();
            Object owner = chain.getThisObject();
            if (owner != null)
            {
                accountListInst = new WeakReference<>(owner);
                accountListSelection = (String)chain.getArg(0);
            }
            return chain.proceed();
        });
        doHook(AccountManager.class.getMethod("addAccount", String.class, String.class, String[].class, Bundle.class, Activity.class, AccountManagerCallback.class, Handler.class), 5, chain ->
        {
            MicroGVariant variant = MicroGUtils.GetEnabledVariant();
            if (variant == null)
                return chain.proceed();
            if (!"com.google".equals(chain.getArg(0)))
                return chain.proceed();
            @SuppressWarnings("unchecked")
            AccountManagerCallback<Bundle> cb = (AccountManagerCallback<Bundle>)chain.getArg(5);
            Context ctx = chain.getArg(4) instanceof Context context ? context : MicroGUtils.FindContext(chain.getThisObject());
            Bundle options = (Bundle)chain.getArg(3);
            String requested = options != null ? options.getString("authAccount") : "";
            if (!MicroGUtils.HasGetAccountsPermission(ctx) && requestAccountsPerm(ctx, currentActivity.get()))
            {
                MAIN.post(() ->
                {
                    if (cb != null)
                        cb.run(new FinishedAccountManagerFuture(new Bundle()));
                });
                return null;
            }
            if (!TextUtils.isEmpty(requested))
            {
                Account[] existing = MicroGUtils.QueryAccounts(ctx, variant);
                if (existing != null && existing.length > 0)
                {
                    String selected = null;
                    for (Account account : existing)
                    {
                        if (requested.equalsIgnoreCase(account.name))
                        {
                            selected = account.name;
                            break;
                        }
                    }
                    if (selected != null)
                    {
                        Bundle result = new Bundle();
                        result.putString(AccountManager.KEY_ACCOUNT_NAME, selected);
                        result.putString("authAccount", selected);
                        result.putString(AccountManager.KEY_ACCOUNT_TYPE, "com.google");
                        MAIN.post(() ->
                        {
                            if (cb != null)
                                cb.run(new FinishedAccountManagerFuture(result));
                        });
                        return null;
                    }
                }
            }
            Object[] args = chain.getArgs().toArray();
            args[0] = variant.accountType;
            AccountManagerCallback<Bundle> cbb = f ->
            {
                try
                {
                    Bundle bundle = f.getResult();
                    if (bundle != null)
                    {
                        // microG reports "accountName", Zalo only reads "authAccount" and expects the Google account type.
                        // Map them.
                        String name = bundle.getString(AccountManager.KEY_ACCOUNT_NAME);
                        if (TextUtils.isEmpty(name))
                            name = bundle.getString("authAccount");
                        if (!TextUtils.isEmpty(name))
                        {
                            bundle.putString("authAccount", name);
                            bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, "com.google");
                        }
                    }
                }
                catch (android.accounts.OperationCanceledException ignored) { }
                catch (Throwable t) { Logger.e(t); }
                if (cb != null)
                    cb.run(f);
            };
            args[5] = cbb;
            return chain.proceed(args);
        });
        doHook(AccountManager.class.getMethod("getAccountsByType", String.class), 5, chain ->
        {
            MicroGVariant variant = MicroGUtils.GetEnabledVariant();
            if (variant == null)
                return chain.proceed();
            if (!"com.google".equals(chain.getArg(0)))
                return chain.proceed();
            Context ctx = MicroGUtils.FindContext(chain.getThisObject());
            if (ctx == null)
                ctx = currentActivity.get();
            Object result = chain.proceed(new Object[] { variant.accountType });
            if (result instanceof Account[] accounts && accounts.length > 0)
                return result;
            Account[] fromMicroG = MicroGUtils.QueryAccounts(ctx, variant);
            if (fromMicroG != null && fromMicroG.length > 0)
                return fromMicroG;
            if (!MicroGUtils.HasGetAccountsPermission(ctx))
                requestAccountsPerm(ctx, currentActivity.get());
            return result;
        });
        doHook(Activity.class.getDeclaredMethod("onResume"), 5, chain ->
        {
            if (chain.getThisObject() instanceof Activity activity)
                currentActivity = new WeakReference<>(activity);
            return chain.proceed();
        });
        doHook(Activity.class.getDeclaredMethod("onRequestPermissionsResult", int.class, String[].class, int[].class), 5, chain ->
        {
            int requestCode = (int)chain.getArg(0);
            Object result = chain.proceed();
            if (requestCode == REQUEST_ACCOUNTS_PERMISSION)
                onAccountsPermResult();
            return result;
        });
    }

    private static boolean requestAccountsPerm(Context context, Activity activity)
    {
        if (requestingAccountsPerm)
            return true;
        if (reqAccountsPermAttempts.get() >= MAX_ACCOUNTS_PERMISSION_ATTEMPTS)
            return false;
        if (activity == null && context instanceof Activity)
            activity = (Activity)context;
        if (activity == null || activity.isFinishing())
        {
            Logger.d("GET_ACCOUNTS missing");
            return false;
        }
        requestingAccountsPerm = true;
        final int attempt = reqAccountsPermAttempts.incrementAndGet();
        final Activity target = activity;
        MAIN.post(() ->
        {
            try
            {
                Logger.d("Requesting GET_ACCOUNTS perm to query microG accounts (" + attempt + ")");
                target.requestPermissions(new String[]{ MicroGUtils.ACCOUNTS_PERMISSION }, REQUEST_ACCOUNTS_PERMISSION);
                MAIN.postDelayed(() -> requestingAccountsPerm = false, 10000L);
            }
            catch (Throwable t)
            {
                requestingAccountsPerm = false;
                Logger.e(t);
            }
        });
        return true;
    }

    private static void onAccountsPermResult()
    {
        requestingAccountsPerm = false;
        Context ctx = CoreUtilityHelper.GetAppContext();
        if (ctx == null)
            return;
        if (ctx.checkSelfPermission(MicroGUtils.ACCOUNTS_PERMISSION) != PackageManager.PERMISSION_GRANTED)
        {
            Logger.w("GET_ACCOUNTS denied (" + reqAccountsPermAttempts.get() + "/" + MAX_ACCOUNTS_PERMISSION_ATTEMPTS + ")");
            return;
        }
        reqAccountsPermAttempts.set(0);
        Logger.d("GET_ACCOUNTS granted");
        MAIN.postDelayed(MicroGHook::refreshAccountList, 400L);
    }

    private static void refreshAccountList()
    {
        Method refresher = invalidateAccountList;
        Object inst = accountListInst.get();
        if (refresher == null || inst == null)
            return;
        try
        {
            refresher.invoke(inst, accountListSelection);
        }
        catch (Throwable t) { Logger.e(t); }
    }

    public static final class FinishedAccountManagerFuture implements AccountManagerFuture<Bundle>
    {
        private final Bundle result;

        public FinishedAccountManagerFuture(Bundle result)
        {
            this.result = result;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) { return false; }
        @Override
        public boolean isCancelled() { return false; }
        @Override
        public boolean isDone() { return true; }
        @Override
        public Bundle getResult() { return result; }
        @Override
        public Bundle getResult(long timeout, TimeUnit unit) { return result; }
    }
}