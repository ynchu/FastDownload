package org.fastdownload.server.dao;


import org.fastdownload.server.entity.User;

import java.util.Collection;

/**
 * 用户实体类Dao层
 *
 * @author Administrator
 */
public interface UserDao {
    /**
     * 根据id查询用户
     *
     * @param id 用户id
     * @return User
     */
    User findById(String id);

    /**
     * 查询所有用户
     *
     * @return Collection<User>
     */
    Collection<User> findByAll();

    /**
     * 添加用户
     *
     * @param user User用户实体类
     * @return int
     */
    int addUser(User user);

    /**
     * 修改更新用户数据
     *
     * @param oldUser User用户实体类，旧数据
     * @param newUser User用户实体类，新数据
     * @return int
     */
    int updateUser(User oldUser, User newUser);

    /**
     * 根据id删除用户
     *
     * @param id 用户id
     * @return int
     */
    int deleteUser(String id);
}
