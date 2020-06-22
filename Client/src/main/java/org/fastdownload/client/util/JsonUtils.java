package org.fastdownload.client.util;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * JSON处理类
 *
 * @author Administrator
 */
public class JsonUtils {
    /**
     * 对象转JSON字符串<br>
     * 例如：<br>
     * Object obj = new Object(); <br>
     * String s = JsonUtils.toJson(obj);
     *
     * @param object 对象
     * @return JSON字符串
     */
    public static String toJson(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }

    /**
     * JSON字符串转对象<br>
     * 例如：<br>
     * 转换为一般对象<br>
     * Object object = JsonUtils.toObject(jsonString, Object.class);<br>
     * 或者<br>
     * 转换为list<br>
     * <p>
     * <code>List<Person> persons = JsonUtils.toObject(jsonString, new TypeToken<<List<Person>>(){}.getType());</code>
     * </p>
     *
     * @param json  JSON字符串
     * @param clazz 需要转的类的class
     * @param <T>   需要转的类
     * @return T 需要转的类
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        Gson gson = new Gson();
        return gson.fromJson(json, clazz);
    }

    /**
     * JSON字符串转List对象<br>
     * 例如：<br>
     * 转换为list<br>
     * <pre>
     * Type typeOfT = new TypeToken&lt;Collection&lt;Foo&gt;&gt;(){}.getType();
     * </pre>
     *
     * @param json    JSON字符串
     * @param typeOfT 需要转的类
     * @param <T>     需要转的类
     * @return T 需要转的类
     */
    public static <T> T toObject(String json, Type typeOfT) {
        Gson gson = new Gson();
        return gson.fromJson(json, typeOfT);
    }
}
