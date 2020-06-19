package org.fastdownload.client.service;


import org.fastdownload.client.entity.User;
import org.fastdownload.client.util.JsonUtils;
import org.junit.jupiter.api.Test;

class CheckLoginTest {

    @Test
    void handle() {
        CheckLogin checkLogin = new CheckLogin();
        User user = new User("123456", "123456", 1);
        System.err.println(JsonUtils.toJson(user));
        int handle = checkLogin.handle(user);
        System.err.println(handle);
    }
}