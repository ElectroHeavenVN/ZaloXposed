package com.ehvn.zaloxposed.hooks.permanent;

import com.ehvn.zaloxposed.hooks.BaseHook;
import com.ehvn.zaloxposed.utilities.Logger;

import java.lang.reflect.Method;

public class DisableDohHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        Class<?> clazz = Class.forName("com.zing.zalocore.connection.socket.NativeSocket", false, classLoader);
        Method method = clazz.getDeclaredMethod("nativeSetDohUrl", String.class);
        Logger.i("Hooking: " + method);
        module.hook(method).intercept(chain ->
        {
            // String dohUrl = (String) chain.getArg(0);
            // Logger.i("nativeSetDohUrl called with: " + dohUrl);
            return null;
            // return chain.proceed(new Object[]{"https://dns.adguard-dns.com/dns-query"});
            // return chain.proceed();
        });
    }
}