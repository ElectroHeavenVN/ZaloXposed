package io.github.electroheavenvn.zaloxposed;

import io.github.electroheavenvn.zaloxposed.utilities.Config;

public final class MorpheConstants
{
    private static final String MODULE_NAME = "ZaloXposed";
    private static final String MORPHE_MODULE_NAME = "ZaloMorphe";

    private MorpheConstants() { }

    public static native boolean isPatchedByMorphe();

    public static String getModuleName()
    {
        return isPatchedByMorphe() ? MORPHE_MODULE_NAME : MODULE_NAME;
    }

    /**
     * enable MicroG on root mode (non-root mode always use MicroG)
     */
    public static boolean isEnableMicroG()
    {
        if (isPatchedByMorphe())
            return true;
        return Config.getEnableMicroG();
    }
}