package io.github.electroheavenvn.zaloxposed.microg;

import android.text.TextUtils;

import java.util.Objects;

import io.github.electroheavenvn.zaloxposed.utilities.Config;

public final class MicroGVariant
{
    public static final MicroGVariant[] availableVariants = new MicroGVariant[]
    {
        new MicroGVariant("microg_morphe", "MicroG RE (Morphe)", "app.revanced.android.gms", "app.revanced", "https://morphe.software/microg"),
        new MicroGVariant("microg_reseam", "MicroG (Reseam)", "app.reseam.android.gms", "app.reseam", "https://reseam.app/download/#gmscore"),
        new MicroGVariant("microg_vanced", "MicroG (Vanced)", "com.mgoogle.android.gms", "com.mgoogle", "https://github.com/TeamVanced/VancedMicroG/releases/latest/"),
        // new MicroGVariant("microg_original", "MicroG Original", "org.microg.gms", "com.google", "https://github.com/microg/GmsCore/releases/latest/"),
    };

    public final String id;
    public final String displayName;
    public final String packageName;
    public final String accountType;
    public final String downloadUrl;

    private MicroGVariant(String id, String displayName, String packageName, String accountType, String downloadUrl) {
        this.id = id;
        this.displayName = displayName;
        this.packageName = packageName;
        this.accountType = accountType;
        this.downloadUrl = downloadUrl;
    }

    public static MicroGVariant GetSelectedVariant()
    {
        String selectedId = Config.getSelectedMicroGVariantID();
        if (TextUtils.isEmpty(selectedId))
            return null;
        for (MicroGVariant variant : availableVariants)
        {
            if (Objects.equals(variant.id, selectedId))
                return variant;
        }
        return null;
    }
}