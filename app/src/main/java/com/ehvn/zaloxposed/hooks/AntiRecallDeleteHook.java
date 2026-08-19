package com.ehvn.zaloxposed.hooks;

import com.ehvn.zaloxposed.utilities.Config;
import com.ehvn.zaloxposed.utilities.Logger;
import com.ehvn.zaloxposed.utilities.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public class AntiRecallDeleteHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        Class<?> clazz = Class.forName("com.zing.zalocore.connection.socket.NativeSocket", false, classLoader);
        Method method = clazz.getDeclaredMethod("onReceivePackage", int.class, Object.class);
        Logger.i("Hooking: " + method);
        module.hook(method).intercept(chain ->
        {
            if (!Config.getEnableAntiRecall() && !Config.getEnableAntiDelete())
                return chain.proceed();
            Object arg1 = chain.getArg(1);
            Field paramsField = arg1.getClass().getDeclaredField("params");
            paramsField.setAccessible(true);
            String json = new String((byte[]) paramsField.get(arg1), StandardCharsets.UTF_8);
            if (!json.contains(",\"msg\":[{\"text\":{\"type\":\""))
                return chain.proceed();
            boolean modified = false;
            JSONObject jsonObject = new JSONObject(json);
            JSONArray msgArray = jsonObject.getJSONArray("msg");
            for (int i = 0; i < msgArray.length(); i++)
            {
                JSONObject msgObject = msgArray.getJSONObject(i).getJSONObject("text");
                if (!msgObject.has("type"))
                    continue;
                JSONObject dataObject = msgObject.getJSONObject("data");
                String type = msgObject.getString("type");
                if (type.equals("chat.undo") && Config.getEnableAntiRecall())
                {
                    if (!Config.getAntiRecallIncludeMe()) 
                    {
                        long senderId = dataObject.getLong("fromU");
                        if ((senderId + "").equals(Utils.GetCurrentUserID()))
                            continue;
                    }
                    msgObject.put("type", "webchat");
                    dataObject.put("msg", "Message recalled");
                    JSONObject attachObj = dataObject.getJSONObject("attach");
                    dataObject.put("attach", "");
                    JSONObject fakeQuote = new JSONObject();
                    fakeQuote.put("ownerId", dataObject.getLong("fromU"));
                    fakeQuote.put("gOwnerId", JSONObject.NULL);
                    fakeQuote.put("cliMsgId", attachObj.getLong("cliMsgId"));
                    fakeQuote.put("globalMsgId", attachObj.getLong("globalMsgId"));
                    fakeQuote.put("cliMsgType", 1);
                    fakeQuote.put("ts", dataObject.getLong("ts"));
                    fakeQuote.put("msg", "Jump to message");
                    fakeQuote.put("attach", "{\"properties\":{\"color\":0,\"size\":0,\"type\":0,\"subType\":0,\"ext\":\"{\\\"shouldParseLinkOrContact\\\":0}\"},\"msgBubbleLayoutType\":0,\"generatedBy\":\"AntiRecallDeleteHook\"}");
                    fakeQuote.put("fromD", "ZaloXposed by ElectroHeavenVN");
                    fakeQuote.put("ttl", 0);
                    dataObject.put("quote", fakeQuote);
                    JSONObject paramsExt = new JSONObject();
                    paramsExt.put("notifyActionCate", 0);
                    paramsExt.put("containType", 0);
                    paramsExt.put("forceNotify", 0);
                    paramsExt.put("notifyTTL", 0);
                    paramsExt.put("countUnread", 1);
                    paramsExt.put("platformType", 0);
                    paramsExt.put("refactorType", 0);
                    dataObject.put("paramsExt", paramsExt);
                    modified = true;
                }
                else if (type.equals("chat.delete") && Config.getEnableAntiDelete())
                {
                    JSONObject attachObj = dataObject.getJSONObject("attach");
                    JSONArray contents = attachObj.getJSONArray("contents");
                    JSONObject content = contents.getJSONObject(contents.length() - 1);
                    if (!Config.getAntiDeleteIncludeMyDeletion()) 
                    {
                        long deleterId = dataObject.getLong("fromU");
                        if ((deleterId + "").equals(Utils.GetCurrentUserID()))
                            continue;
                    }
                    msgObject.put("type", "webchat");
                    if (contents.length() > 1)
                        dataObject.put("msg", contents.length() + " messages deleted");
                    else
                        dataObject.put("msg", "Message deleted");
                    dataObject.put("attach", "");
                    JSONObject fakeQuote = new JSONObject();
                    fakeQuote.put("ownerId", content.getLong("uidFrom"));
                    fakeQuote.put("gOwnerId", JSONObject.NULL);
                    fakeQuote.put("cliMsgId", content.getLong("clientDelMsgId"));
                    fakeQuote.put("globalMsgId", content.getLong("globalDelMsgId"));
                    fakeQuote.put("cliMsgType", 1);
                    fakeQuote.put("ts", content.getLong("clientDelMsgId"));
                    if (contents.length() > 1)
                        fakeQuote.put("msg", "Jump to first deleted message");
                    else
                        fakeQuote.put("msg", "Jump to message");
                    fakeQuote.put("attach", "{\"properties\":{\"color\":0,\"size\":0,\"type\":0,\"subType\":0,\"ext\":\"{\\\"shouldParseLinkOrContact\\\":0}\"},\"msgBubbleLayoutType\":0,\"generatedBy\":\"AntiRecallDeleteHook\"}");
                    fakeQuote.put("fromD", "ZaloXposed by ElectroHeavenVN");
                    fakeQuote.put("ttl", 0);
                    dataObject.put("quote", fakeQuote);
                    JSONObject paramsExt = new JSONObject();
                    paramsExt.put("notifyActionCate", 0);
                    paramsExt.put("containType", 0);
                    paramsExt.put("forceNotify", 0);
                    paramsExt.put("notifyTTL", 0);
                    paramsExt.put("countUnread", 1);
                    paramsExt.put("platformType", 0);
                    paramsExt.put("refactorType", 0);
                    dataObject.put("paramsExt", paramsExt);
                    modified = true;
                }
            }
            if (modified)
            {
                String modifiedJson = jsonObject.toString();
                paramsField.set(arg1, modifiedJson.getBytes(StandardCharsets.UTF_8));
            }
            return chain.proceed();
        });
    }
}