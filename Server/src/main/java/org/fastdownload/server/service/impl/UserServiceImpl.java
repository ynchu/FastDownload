package org.fastdownload.server.service.impl;

import org.fastdownload.server.dao.UserDao;
import org.fastdownload.server.dao.impl.UserDaoImpl;
import org.fastdownload.server.entity.User;
import org.fastdownload.server.service.UserService;

import java.util.Collection;

/**
 * 用户服务层实现类
 *
 * @author Administrator
 */
public class UserServiceImpl implements UserService {
    private UserDao userDao = new UserDaoImpl();

    @Override
    public User findById(String id) {
        return userDao.findById(id);
    }

    @Override
    public Collection<User> findByAll() {
        return userDao.findByAll();
    }

    @Override
    public Boolean addUser(User user) {
        return userDao.addUser(user) != -1;
    }

    @Override
    public Boolean updateUser(User oldUser, User newUser) {
        return userDao.updateUser(oldUser, newUser) != -1;
    }

    @Override
    public Boolean deleteUser(String id) {
        return userDao.deleteUser(id) != -1;
    }
}
