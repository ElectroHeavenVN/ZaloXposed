package com.ehvn.zaloxposed.utilities;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedBridge;

public final class Config 
{
    private Config() { }

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
                XposedBridge.log(e);
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
            XposedBridge.log(e);
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
            XposedBridge.log(e);
        }
    }

    private static Object get(String key, Object defaultValue)
    {
        try
        {
            if (defaultValue instanceof Long)
                return config.getLong(key);
            return config.get(key);
        }
        catch (Exception e)
        {
            XposedBridge.log(e);
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
            XposedBridge.log(e);
        }
    }

    public static boolean getEnableExtendedGridMenu()
    {
        return (boolean)get("enable_extended_grid_menu", false);
    }

    public static void setEnableExtendedGridMenu(boolean value)
    {
        set("enable_extended_grid_menu", value);
    }

    public static boolean getEnableChatHead()
    {
        return (boolean)get("enable_chat_head", false);
    }

    public static void setEnableChatHead(boolean value)
    {
        set("enable_chat_head", value);
    }

    public static boolean getUnlockZCloud()
    {
        return (boolean)get("unlock_z_cloud", false);
    }

    public static void setUnlockZCloud(boolean value)
    {
        set("unlock_z_cloud", value);
    }

    public static boolean getEnableTTLOverride()
    {
        return (boolean)get("enable_msg_ttl_override", false);
    }

    public static void setEnableTTLOverride(boolean value)
    {
        set("enable_msg_ttl_override", value);
    }
  
    public static long getTTL()
    {
        return (long)get("msg_ttl_value", 0L);
    }

    public static void setTTL(long value)
    {
        set("msg_ttl_value", value);
    }
    
    public static boolean getEnableFakeGroupRole()
    {
        return (boolean)get("enable_fake_group_role", false);
    }

    public static void setEnableFakeGroupRole(boolean value)
    {
        set("enable_fake_group_role", value);
    }

    public static int getFakeGroupRoleLevel()
    {
        return (int)get("fake_group_role_level", 0);
    }

    public static void setFakeGroupRoleLevel(int value)
    {
        set("fake_group_role_level", value);
    }
}