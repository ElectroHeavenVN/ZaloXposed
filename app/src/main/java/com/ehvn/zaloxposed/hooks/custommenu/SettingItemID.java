package com.ehvn.zaloxposed.hooks.custommenu;

public enum SettingItemID
{
    UNKNOWN(0),
    PROFILE(100),
    QUICK_ACTION(101),
    MY_QR(1),
    SAVED_MESSAGE(2),
    MY_OA(3),
    ACCOUNT(4),
    PRIVACY(5),
    QR_WALLET(6),
    BA_TOOL(7),
    BA_IAP(8),
    TOOL_STORAGE(9),
    ZALO_CLOUD(10),
    PROMOTE_BA(11);

    SettingItemID(int id)
    {
        this.id = id;
    }

    public final int id;
}
