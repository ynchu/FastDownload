package org.fastdownload.client.util;

import com.google.gson.Gson;
import org.fastdownload.client.entity.User;
import org.junit.jupiter.api.Test;

class JsonUtilsTest {

    @Test
    void toJson() {
        User user = new User("123456", "123456", 1);
        String s = JsonUtils.toJson(user);
        System.out.println(s);
    }

    @Test
    void toObject() {
        User user = new User("123456", "123456", 1);
        String s = JsonUtils.toJson(user);

        User object = JsonUtils.toObject(s, User.class);
        System.out.println(object);
    }
}