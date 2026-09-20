package com.ehvn.zaloxposed;

public final class MorpheConstants
{
    private static final boolean PATCHED_BY_MORPHE = false;
    private static final String MODULE_NAME = "ZaloXposed";
    private static final String MORPHE_MODULE_NAME = "ZaloMorphe";

    private MorpheConstants() { }

    public static boolean isPatchedByMorphe()
    {
        return PATCHED_BY_MORPHE;
    }

    public static String getModuleName()
    {
        return isPatchedByMorphe() ? MORPHE_MODULE_NAME : MODULE_NAME;
    }
}