package io.github.electroheavenvn.zaloxposed.hooks.permanent;

import java.lang.reflect.Method;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;

public class DisableDohHook extends BaseHook
{
    @Override
    public void installHooks() throws Throwable
    {
        Class<?> clazz = Class.forName("com.zing.zalocore.connection.socket.NativeSocket", false, classLoader);
        Method method = clazz.getDeclaredMethod("nativeSetDohUrl", String.class);
        doHook(method, chain ->
        {
            // String dohUrl = (String) chain.getArg(0);
            // Logger.i("nativeSetDohUrl called with: " + dohUrl);
            return null;
            // return chain.proceed(new Object[]{"https://dns.adguard-dns.com/dns-query"});
            // return chain.proceed();
        });
    }
}