package com.ehvn.zaloxposed.utilities;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public final class Config 
{
    private Config() { }

    public static final String KEY_ENABLE_BACKGROUND = "enable_bg";
    public static final String KEY_ENABLE_EXTENDED_GRID_MENU = "enable_extended_grid_menu";
    public static final String KEY_ENABLE_CHAT_HEAD = "enable_chat_head";
    public static final String KEY_UNLOCK_Z_CLOUD = "unlock_z_cloud";
    public static final String KEY_ENABLE_TTL_OVERRIDE = "enable_msg_ttl_override";
    public static final String KEY_MSG_TTL = "msg_ttl_value";
    public static final String KEY_ENABLE_FAKE_GROUP_ROLE = "enable_fake_group_role";
    public static final String KEY_FAKE_GROUP_ROLE_LEVEL = "fake_group_role_level";
    public static final String KEY_ENABLE_SHARE_HIDDEN_STICKER_PACK = "enable_share_hidden_sticker_pack";
    public static final String KEY_ENABLE_EXTENDED_GROUP_SETTING_MENU = "enable_extended_group_setting_menu";
    public static final String KEY_HIDE_MEDIA_BOX = "ads_hide_media_box";
    public static final String KEY_HIDE_BIZ_BOX = "ads_hide_business_box";
    public static final String KEY_ENABLE_CUSTOMIZE_BOTTOM_ROW = "enable_customize_bottom_row";
    public static final String KEY_HIDE_DISCOVERY_TAB = "ads_hide_discovery_tab";
    public static final String KEY_HIDE_NEWS_FEED_TAB = "ads_hide_news_feed_tab";
    public static final String KEY_SHOW_MORE_TAB = "ads_show_more_tab";
    public static final String KEY_SHOW_GROUPS_TAB = "ads_show_groups_tab";
    public static final String KEY_ENABLE_ANTI_RECALL = "enable_anti_recall";
    public static final String KEY_ENABLE_ANTI_DELETE = "enable_anti_delete";
    public static final String KEY_ANTI_RECALL_INCLUDE_ME = "anti_recall_include_me";
    public static final String KEY_ANTI_DELETE_INCLUDE_MY_DELETION = "anti_delete_include_my_deletion";
    public static final String KEY_HIDE_Z_INSTANT_ADS = "ads_hide_z_instant";
    public static final String KEY_HIDE_FEED_ITEM_Z_INSTANT_ADS = "ads_hide_feed_item_z_instant";
    public static final String KEY_DISABLE_FIREBASE_LOGGING = "logging_disable_firebase";
    public static final String KEY_DISABLE_ZALO_TRACKING = "logging_disable_zalo_tracking";

    public interface OnConfigChangedListener 
    {
        void onConfigChanged(String key, Object oldValue, Object newValue);
    }

    private static final List<OnConfigChangedListener> listeners = new ArrayList<>();

    private static final File file = new File(Utils.GetZaloXposedDir(), "config.json");

    private static JSONObject config = new JSONObject();

    public static void addOnConfigChangedListener(OnConfigChangedListener listener)
    {
        listeners.add(listener);
    }

    public static void removeOnConfigChangedListener(OnConfigChangedListener listener)
    {
        listeners.remove(listener);
    }

    private static void notifyConfigChanged(String key, Object oldValue, Object newValue)
    {
        for (OnConfigChangedListener listener : listeners)
        {
            try
            {
                listener.onConfigChanged(key, oldValue, newValue);
            }
            catch (Exception e)
            {
                Logger.e(e);
            }
        }
    }

    public static void Load() 
    {
        try 
        {
            if (!file.exists())
            {
                FileWriter fw = new FileWriter(file);
                fw.write("{}");
                fw.close();
            }
            StringBuilder content = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null)
                content.append(line).append("\n");
            reader.close();
            config = new JSONObject(content.toString());
        } 
        catch (Exception e)
        {
            Logger.e(e);
        }
    }

    private static void save()
    {
        try 
        {
            FileWriter fw = new FileWriter(file);
            fw.write(config.toString());
            fw.close();
        }
        catch (Exception e) 
        {
            Logger.e(e);
        }
    }

    private static Object get(String key, Object defaultValue)
    {
        try
        {
            if (!config.has(key))
                config.putOpt(key, defaultValue);
            if (defaultValue instanceof Long)
                return config.getLong(key);
            return config.get(key);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
        return defaultValue;
    }

    private static void set(String key, Object value)
    {
        try
        {
            Object oldValue = config.opt(key);
            config.putOpt(key, value);
            save();
            notifyConfigChanged(key, oldValue, value);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
    }

    public static boolean getEnableBackground()
    {
        return (boolean)get(KEY_ENABLE_BACKGROUND, true);
    }

    public static void setEnableBackground(boolean value)
    {
        set(KEY_ENABLE_BACKGROUND, value);
    }

    public static boolean getEnableExtendedGridMenu()
    {
        return (boolean)get(KEY_ENABLE_EXTENDED_GRID_MENU, false);
    }

    public static void setEnableExtendedGridMenu(boolean value)
    {
        set(KEY_ENABLE_EXTENDED_GRID_MENU, value);
    }

    public static boolean getEnableChatHead()
    {
        return (boolean)get(KEY_ENABLE_CHAT_HEAD, false);
    }

    public static void setEnableChatHead(boolean value)
    {
        set(KEY_ENABLE_CHAT_HEAD, value);
    }

    public static boolean getUnlockZCloud()
    {
        return (boolean)get(KEY_UNLOCK_Z_CLOUD, false);
    }

    public static void setUnlockZCloud(boolean value)
    {
        set(KEY_UNLOCK_Z_CLOUD, value);
    }

    public static boolean getEnableTTLOverride()
    {
        return (boolean)get(KEY_ENABLE_TTL_OVERRIDE, false);
    }

    public static void setEnableTTLOverride(boolean value)
    {
        set(KEY_ENABLE_TTL_OVERRIDE, value);
    }
  
    public static long getTTL()
    {
        return (long)get(KEY_MSG_TTL, 0L);
    }

    public static void setTTL(long value)
    {
        set(KEY_MSG_TTL, value);
    }
    
    public static boolean getEnableFakeGroupRole()
    {
        return (boolean)get(KEY_ENABLE_FAKE_GROUP_ROLE, false);
    }

    public static void setEnableFakeGroupRole(boolean value)
    {
        set(KEY_ENABLE_FAKE_GROUP_ROLE, value);
    }

    public static int getFakeGroupRoleLevel()
    {
        return (int)get(KEY_FAKE_GROUP_ROLE_LEVEL, 0);
    }

    public static void setFakeGroupRoleLevel(int value)
    {
        set(KEY_FAKE_GROUP_ROLE_LEVEL, value);
    }

    public static boolean getEnableShareHiddenStickerPack()
    {
        return (boolean)get(KEY_ENABLE_SHARE_HIDDEN_STICKER_PACK, false);
    }

    public static void setEnableShareHiddenStickerPack(boolean value)
    {
        set(KEY_ENABLE_SHARE_HIDDEN_STICKER_PACK, value);
    }

    public static boolean getEnableExtendedGroupSettingMenu()
    {
        return (boolean)get(KEY_ENABLE_EXTENDED_GROUP_SETTING_MENU, false);
    }

    public static void setEnableExtendedGroupSettingMenu(boolean value)
    {
        set(KEY_ENABLE_EXTENDED_GROUP_SETTING_MENU, value);
    }

    public static boolean getHideMediaBox()
    {
        return (boolean)get(KEY_HIDE_MEDIA_BOX, false);
    }

    public static void setHideMediaBox(boolean value)
    {
        set(KEY_HIDE_MEDIA_BOX, value);
    }

    public static boolean getHideBizBox()
    {
        return (boolean)get(KEY_HIDE_BIZ_BOX, false);
    }

    public static void setHideBizBox(boolean value)
    {
        set(KEY_HIDE_BIZ_BOX, value);
    }

    public static boolean getEnableCustomizeBottomRow()
    {
        return (boolean)get(KEY_ENABLE_CUSTOMIZE_BOTTOM_ROW, false);
    }

    public static void setEnableCustomizeBottomRow(boolean value)
    {
        set(KEY_ENABLE_CUSTOMIZE_BOTTOM_ROW, value);
    }

    public static boolean getHideDiscoveryTab()
    {
        return (boolean)get(KEY_HIDE_DISCOVERY_TAB, false);
    }

    public static void setHideDiscoveryTab(boolean value)
    {
        set(KEY_HIDE_DISCOVERY_TAB, value);
    }

    public static boolean getHideNewsFeedTab()
    {
        return (boolean)get(KEY_HIDE_NEWS_FEED_TAB, false);
    }

    public static void setHideNewsFeedTab(boolean value)
    {
        set(KEY_HIDE_NEWS_FEED_TAB, value);
    }

    public static boolean getShowMoreTab()
    {
        return (boolean)get(KEY_SHOW_MORE_TAB, false);
    }

    public static void setShowMoreTab(boolean value)
    {
        set(KEY_SHOW_MORE_TAB, value);
    }

    public static boolean getShowGroupsTab()
    {
        return (boolean)get(KEY_SHOW_GROUPS_TAB, false);
    }

    public static void setShowGroupsTab(boolean value)
    {
        set(KEY_SHOW_GROUPS_TAB, value);
    }

    public static boolean getEnableAntiRecall()
    {
        return (boolean)get(KEY_ENABLE_ANTI_RECALL, false);
    }

    public static void setEnableAntiRecall(boolean value)
    {
        set(KEY_ENABLE_ANTI_RECALL, value);
    }

    public static boolean getEnableAntiDelete()
    {
        return (boolean)get(KEY_ENABLE_ANTI_DELETE, false);
    }

    public static void setEnableAntiDelete(boolean value)
    {
        set(KEY_ENABLE_ANTI_DELETE, value);
    }

    public static boolean getAntiRecallIncludeMe()
    {
        return (boolean)get(KEY_ANTI_RECALL_INCLUDE_ME, false);
    }

    public static void setAntiRecallIncludeMe(boolean value)
    {
        set(KEY_ANTI_RECALL_INCLUDE_ME, value);
    }

    public static boolean getAntiDeleteIncludeMyDeletion()
    {
        return (boolean)get(KEY_ANTI_DELETE_INCLUDE_MY_DELETION, false);
    }

    public static void setAntiDeleteIncludeMyDeletion(boolean value)
    {
        set(KEY_ANTI_DELETE_INCLUDE_MY_DELETION, value);
    }

    public static boolean getHideZInstantAds()
    {
        return (boolean)get(KEY_HIDE_Z_INSTANT_ADS, false);
    }

    public static void setHideZInstantAds(boolean value)
    {
        set(KEY_HIDE_Z_INSTANT_ADS, value);
    }

    public static boolean getHideFeedItemZInstantAds()
    {
        return (boolean)get(KEY_HIDE_FEED_ITEM_Z_INSTANT_ADS, false);
    }

    public static void setHideFeedItemZInstantAds(boolean value)
    {
        set(KEY_HIDE_FEED_ITEM_Z_INSTANT_ADS, value);
    }

    public static boolean getDisableFirebaseLogging()
    {
        return (boolean)get(KEY_DISABLE_FIREBASE_LOGGING, true);
    }

    public static void setDisableFirebaseLogging(boolean value)
    {
        set(KEY_DISABLE_FIREBASE_LOGGING, value);
    }

    public static boolean getDisableZaloTracking()
    {
        return (boolean)get(KEY_DISABLE_ZALO_TRACKING, true);
    }

    public static void setDisableZaloTracking(boolean value)
    {
        set(KEY_DISABLE_ZALO_TRACKING, value);
    }
}