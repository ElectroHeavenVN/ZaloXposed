package io.github.electroheavenvn.zaloxposed.hooks.functional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import io.github.electroheavenvn.zaloxposed.MorpheConstants;
import io.github.electroheavenvn.zaloxposed.hooks.BaseHook;
import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.CoreUtilityHelper;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Logger;

public class AntiRecallDeleteHook extends BaseHook
{
    @Override
    public void hook() throws Throwable
    {
        List<Method> methods = DexKitCache.GetMethods("AntiRecallDelete_ChatPacketHandler_OnPayload", () -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                .returnType("void")
                .paramCount(8)
                .paramTypes("java.lang.String", "int", "int", "org.json.JSONObject", "int", "boolean", "long", null)
                .addUsingString("currentUserUid", StringMatchType.Equals)
                .addUsingString("jsonData", StringMatchType.Equals)
                .addUsingString("source", StringMatchType.Equals)
                .addUsingString("PullMessage", StringMatchType.Equals)
                .addUsingString("ChatPacketHandler", StringMatchType.Equals)
                .addUsingNumber(15000)
            )), classLoader);
        if (methods.isEmpty())
        {
            Logger.e("Target method not found");
            return;
        }
        for (Method method : methods)
        {
            Logger.i("Hooking: " + method);
            module.hook(method).intercept(chain ->
            {
                if (!Config.getEnableAntiRecall() && !Config.getEnableAntiDelete())
                    return chain.proceed();
                Object arg3 = chain.getArg(3);
                if (!(arg3 instanceof JSONObject))
                    return chain.proceed();
                if (!processMsgPayload((JSONObject)arg3))
                    return chain.proceed();
                return chain.proceed();
            });
        }
    }

    private static boolean processMsgPayload(JSONObject msgPayload)
    {
        boolean modified = false;
        try 
        {
            if (!msgPayload.has("msg"))
            {
                Logger.e("msgPayload does not contain 'msg' key");
                return false;
            }
            JSONArray msgArray = msgPayload.getJSONArray("msg");
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
                        if ((senderId + "").equals(CoreUtilityHelper.GetCurrentUserID()))
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
                    fakeQuote.put("fromD", MorpheConstants.getModuleName() + " by ElectroHeavenVN");
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
                        if ((deleterId + "").equals(CoreUtilityHelper.GetCurrentUserID()))
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
                    fakeQuote.put("fromD", MorpheConstants.getModuleName() + " by ElectroHeavenVN");
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
        }
        catch (Exception e) { Logger.e(e); }
        return modified;
    }
}