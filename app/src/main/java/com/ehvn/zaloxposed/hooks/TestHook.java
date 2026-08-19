package com.ehvn.zaloxposed.hooks;

import com.ehvn.zaloxposed.utilities.*;

import org.json.*;
import org.luckypray.dexkit.query.*;
import org.luckypray.dexkit.query.enums.*;
import org.luckypray.dexkit.query.matchers.*;
import org.luckypray.dexkit.result.*;

import java.io.InputStream;
import java.lang.reflect.*;
import java.nio.charset.*;
import java.util.*;

import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;

public class TestHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        // Constructor<?> ctor = JSONObject.class.getConstructor(String.class);
        // Logger.i("Hooking: " + ctor);
        // module.hook(ctor).intercept(chain ->
        // {
        //     Object result = chain.proceed();
        //     Logger.i("JSONObject constructor called with string: " + chain.getArg(0));
        //     Logger.i("stack trace: " + Utils.GetStackTrace());
        //     return result;
        // });
        // Method printStackTrace = Throwable.class.getDeclaredMethod("printStackTrace");
        // module.hook(printStackTrace).intercept(chain ->
        // {
        //     Object result = chain.proceed();
        //     Logger.e("Exception:");
        //     Logger.e((Throwable)chain.getThisObject());
        //     Logger.i("stack trace: " + Utils.GetStackTrace());
        //     return result;
        // });
    }
}