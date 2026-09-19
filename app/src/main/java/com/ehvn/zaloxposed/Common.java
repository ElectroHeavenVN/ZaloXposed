package com.ehvn.zaloxposed;

public final class Common
{
    private Common() { }

    private static String packageName = "";

    public static String getPackageName()
    {
        return packageName;
    }

    public static void setPackageName(String pkgName)
    {
        packageName = pkgName;
    }
}
