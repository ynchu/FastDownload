package org.fastdownload.server.dao.impl;

import org.fastdownload.server.dao.UserDao;
import org.fastdownload.server.entity.User;
import org.junit.jupiter.api.Test;

import java.util.Collection;

class UserDaoImplTest {

    @Test
    void findById() {
        UserDao userDao = new UserDaoImpl();
        User user = userDao.findById("123456");
        System.out.println(user);
    }

    @Test
    void findByAll() {
        UserDao userDao = new UserDaoImpl();
        Collection<User> users = userDao.findByAll();
        users.forEach(System.out::println);
    }

    @Test
    void addUser() {
        UserDao userDao = new UserDaoImpl();
        User user = new User();
        user.setId("123458");
        user.setPwd("123456");
        int i = userDao.addUser(user);
        System.out.println(i != -1);
    }

    @Test
    void updateUser() {
        UserDao userDao = new UserDaoImpl();
        User oldUser = userDao.findById("123458");

        User newUser = new User();
        newUser.setId(oldUser.getId());
        newUser.setPwd("123450");
        newUser.setType(2);
        int i = userDao.updateUser(oldUser, newUser);
        System.out.println(i != -1);
    }

    @Test
    void deleteUser() {
        UserDao userDao = new UserDaoImpl();
        int i = userDao.deleteUser("123458");
        System.out.println(i != -1);
    }
}