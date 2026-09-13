package com.ehvn.zaloxposed.utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class RequestPacketHelper
{
    private RequestPacketHelper() { }

    private static final Map<String, Field> fieldMap = new HashMap<>();

    private static Constructor<?> ctorWithParams = null;

    private static Constructor<?> ctorNoParams = null;

    public static Object Create()
    {
        loadFields();
        try
        {
            if (ctorNoParams == null)
                return null;
            return ctorNoParams.newInstance();
        }
        catch (Exception e)
        {
            Logger.e(e);
            return null;
        }
    }

    public static Object Create(int sri, int si, int di, int rc, byte v, byte na, byte mt, byte st, byte smd, byte dt, short md, long gmi, byte[] params)
    {
        loadFields();
        try
        {
            if (ctorWithParams == null)
                return null;
            return ctorWithParams.newInstance(0, 0, sri, si, di, 0, rc, v, na, mt, st, smd, dt, md, gmi, params);
        }
        catch (Exception e)
        {
            Logger.e(e);
            return null;
        }
    }
    
    public static byte GetVersion(Object requestPacket)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("v");
            if (field == null)
                return 0;
            return field.getByte(requestPacket);
        }
        catch (Exception e)
        {
            Logger.e(e);
            return 0;
        }
    }

    public static void SetVersion(Object requestPacket, byte value)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("v");
            if (field != null)
                field.setByte(requestPacket, value);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
    }

    public static byte GetNetworkAttribute(Object requestPacket)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("na");
            if (field == null)
                return 0;
            return field.getByte(requestPacket);
        }
        catch (Exception e)
        {
            Logger.e(e);
            return 0;
        }
    }

    public static void SetNetworkAttribute(Object requestPacket, byte value)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("na");
            if (field != null)
                field.setByte(requestPacket, value);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
    }

    public static byte GetMessageType(Object requestPacket)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("mt");
            if (field == null)
                return 0;
            return field.getByte(requestPacket);
        }
        catch (Exception e)
        {
            Logger.e(e);
            return 0;
        }
    }

    public static void SetMessageType(Object requestPacket, byte value)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("mt");
            if (field != null)
                field.setByte(requestPacket, value);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
    }

    public static int GetSequenceId(Object requestPacket)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("si");
            if (field == null)
                return 0;
            return field.getInt(requestPacket);
        }
        catch (Exception e)
        {
            Logger.e(e);
            return 0;
        }
    }

    public static void SetSequenceId(Object requestPacket, int value)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("si");
            if (field != null)
                field.setInt(requestPacket, value);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
    }

    public static int GetSourceId(Object requestPacket)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("sri");
            if (field == null)
                return 0;
            return field.getInt(requestPacket);
        }
        catch (Exception e)
        {
            Logger.e(e);
            return 0;
        }
    }

    public static void SetSourceId(Object requestPacket, int value)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("sri");
            if (field != null)
                field.setInt(requestPacket, value);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
    }

    public static byte GetSocketType(Object requestPacket)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("st");
            if (field == null)
                return 0;
            return field.getByte(requestPacket);
        }
        catch (Exception e)
        {
            Logger.e(e);
            return 0;
        }
    }

    public static void SetSocketType(Object requestPacket, byte value)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("st");
            if (field != null)
                field.setByte(requestPacket, value);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
    }

    public static short GetCommand(Object requestPacket)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("md");
            if (field == null)
                return 0;
            return field.getShort(requestPacket);
        }
        catch (Exception e)
        {
            Logger.e(e);
            return 0;
        }
    }

    public static void SetCommand(Object requestPacket, short value)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("md");
            if (field != null)
                field.setShort(requestPacket, value);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
    }

    public static byte GetSubcommand(Object requestPacket)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("smd");
            if (field == null)
                return 0;
            return field.getByte(requestPacket);
        }
        catch (Exception e)
        {
            Logger.e(e);
            return 0;
        }
    }

    public static void SetSubcommand(Object requestPacket, byte value)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("smd");
            if (field != null)
                field.setByte(requestPacket, value);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
    }

    public static int GetReturnCode(Object requestPacket)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("rc");
            if (field == null)
                return 0;
            return field.getInt(requestPacket);
        }
        catch (Exception e)
        {
            Logger.e(e);
            return 0;
        }
    }

    public static void SetReturnCode(Object requestPacket, int value)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("rc");
            if (field != null)
                field.setInt(requestPacket, value);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
    }

    public static int GetDestinationId(Object requestPacket)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("di");
            if (field == null)
                return 0;
            return field.getInt(requestPacket);
        }
        catch (Exception e)
        {
            Logger.e(e);
            return 0;
        }
    }

    public static void SetDestinationId(Object requestPacket, int value)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("di");
            if (field != null)
                field.setInt(requestPacket, value);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
    }

    public static byte GetDestinationType(Object requestPacket)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("dt");
            if (field == null)
                return 0;
            return field.getByte(requestPacket);
        }
        catch (Exception e)
        {
            Logger.e(e);
            return 0;
        }
    }

    public static void SetDestinationType(Object requestPacket, byte value)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("dt");
            if (field != null)
                field.setByte(requestPacket, value);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
    }

    public static long GetGlobalMessageId(Object requestPacket)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("gmi");
            if (field == null)
                return 0L;
            return field.getLong(requestPacket);
        }
        catch (Exception e)
        {
            Logger.e(e);
            return 0L;
        }
    }

    public static void SetGlobalMessageId(Object requestPacket, long value)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("gmi");
            if (field != null)
                field.setLong(requestPacket, value);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
    }

    public static long GetRequestId(Object requestPacket)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("rid");
            if (field == null)
                return 0L;
            return field.getLong(requestPacket);
        }
        catch (Exception e)
        {
            Logger.e(e);
            return 0L;
        }
    }

    public static void SetRequestId(Object requestPacket, long value)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("rid");
            if (field != null)
                field.setLong(requestPacket, value);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
    }

    public static byte GetEncryptMode(Object requestPacket)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("em");
            if (field == null)
                return 0;
            return field.getByte(requestPacket);
        }
        catch (Exception e)
        {
            Logger.e(e);
            return 0;
        }
    }

    public static void SetEncryptMode(Object requestPacket, byte value)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("em");
            if (field != null)
                field.setByte(requestPacket, value);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
    }

    public static byte GetIsNeedRetry(Object requestPacket)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("inr");
            if (field == null)
                return 0;
            return field.getByte(requestPacket);
        }
        catch (Exception e)
        {
            Logger.e(e);
            return 0;
        }
    }

    public static void SetIsNeedRetry(Object requestPacket, byte value)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("inr");
            if (field != null)
                field.setByte(requestPacket, value);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
    }

    public static long GetRequestTimeout(Object requestPacket)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("rto");
            if (field == null)
                return 0L;
            return field.getLong(requestPacket);
        }
        catch (Exception e)
        {
            Logger.e(e);
            return 0L;
        }
    }

    public static void SetRequestTimeout(Object requestPacket, long value)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("rto");
            if (field != null)
                field.setLong(requestPacket, value);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
    }

    public static byte[] GetParams(Object requestPacket)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("params");
            if (field == null)
                return null;
            return (byte[]) field.get(requestPacket);
        }
        catch (Exception e)
        {
            Logger.e(e);
            return null;
        }
    }

    public static void SetParams(Object requestPacket, byte[] value)
    {
        checkType(requestPacket);
        loadFields();
        try
        {
            Field field = fieldMap.get("params");
            if (field != null)
                field.set(requestPacket, value);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
    }

    private static void loadFields()
    {
        if (ctorNoParams != null)
            return;
        try
        {
            Class<?> requestPacketClass = Class.forName("com.zing.zalocore.connection.socket.RequestPacket", false, Utils.GetClassLoader());
            ctorNoParams = requestPacketClass.getConstructor();
            ctorWithParams = requestPacketClass.getConstructor(int.class, int.class, int.class, int.class, int.class, int.class, int.class, byte.class, byte.class, byte.class, byte.class, byte.class, byte.class, short.class, long.class, byte[].class);
            for (Field field : requestPacketClass.getDeclaredFields())
                field.setAccessible(true);
            fieldMap.put("params", requestPacketClass.getDeclaredField("params"));
            Object dummyInstance = ctorWithParams.newInstance(1000, 1001, 1002, 1003, 1004, 1005, 1006, (byte)1007, (byte)1008, (byte)1009, (byte)1010, (byte)1011, (byte)1012, (short)1013, 1014L, new byte[]{1,2,3});
            for (Method method : requestPacketClass.getDeclaredMethods())
            {
                if (method.getParameterCount() != 0 || method.getReturnType() == void.class)
                    continue;
                String methodName = method.getName();
                if (!methodName.startsWith("g") || methodName.equals("glp") || methodName.length() <= 1)
                    continue;
                method.setAccessible(true);
                Object value = method.invoke(dummyInstance);
                if (value == null)
                    continue;
                String fieldKey = methodName.substring(1);
                for (Field field : requestPacketClass.getDeclaredFields())
                {
                    Object fieldValue = field.get(dummyInstance);
                    if (!Objects.equals(value, fieldValue))
                        continue;
                    fieldMap.put(fieldKey, field);
                    break;
                }
            }
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
    }

    private static void checkType(Object requestPacket)
    {
        if (requestPacket == null)
            throw new IllegalArgumentException("requestPacket is null");
        if (!"com.zing.zalocore.connection.socket.RequestPacket".equals(requestPacket.getClass().getName()))
            throw new IllegalArgumentException("requestPacket is not of type RequestPacket");
    }
}