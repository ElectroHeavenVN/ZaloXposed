package io.github.electroheavenvn.zaloxposed.hooks.permanent;

import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;

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