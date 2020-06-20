package org.fastdownload.server.util;

import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;
import org.fastdownload.server.entity.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    @Test
    void toJson() {
    }

    @Test
    void toObject() {
    }

    @Test
    void testToObject() {
        List<User> list = new ArrayList<>();
        list.add(new User("1234", "1234", 1));
        list.add(new User("1235", "1235", 2));
        list.add(new User("1236", "1236", 3));
        list.add(new User("1237", "1237", 4));
        list.add(new User("1238", "1238", 5));
        String s = JsonUtils.toJson(list);
        System.out.println(s);

        List<User> o = JsonUtils.toObject(s, new TypeToken<List<User>>() {
        }.getType());
        System.out.println(o);
    }
}