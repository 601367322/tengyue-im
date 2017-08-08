package com.tengyue.im.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.sf.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MyJSONUtil {

    public static <T> T jsonToBean(String json, Class<T> clazz) {
        return (T) JSONObject.toBean(JSONObject.fromObject(json), clazz);
    }

    public static <T> ArrayList<T> jsonToList(String json, Class<T> classOfT) {
        Type type = new TypeToken<ArrayList<JsonObject>>() {
        }.getType();
        Gson gson = getGson();
        ArrayList<JsonObject> jsonObjs = gson.fromJson(json, type);
        ArrayList<T> listOfT = null;
        try {
            listOfT = new ArrayList<T>();
            for (JsonObject jsonObj : jsonObjs) {
                listOfT.add(gson.fromJson(jsonObj, classOfT));
            }
            return listOfT;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String beanToJson(Object bean) {
        return getGson().toJson(bean);
    }

    public static Gson getGson() {
        return new GsonBuilder().disableHtmlEscaping().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    }
}
