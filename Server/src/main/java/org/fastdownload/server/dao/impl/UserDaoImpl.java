package org.fastdownload.server.dao.impl;

import lombok.extern.log4j.Log4j2;
import org.fastdownload.server.dao.UserDao;
import org.fastdownload.server.entity.User;
import org.fastdownload.server.util.DBUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 用户类Dao层实现类
 *
 * @author Administrator
 */
@Log4j2
public class UserDaoImpl implements UserDao {
    @Override
    public User findById(String id) {
        User user = new User();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = "select *\n" +
                "from user\n" +
                "where id = ?;";
        try {
            connection = DBUtils.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, id);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String userId = resultSet.getString("id");
                String pwd = resultSet.getString("pwd");
                int type = resultSet.getInt("type");

                user.setId(userId);
                user.setPwd(pwd);
                user.setType(type);
            }
            log.info("根据ID查询用户成功");
        } catch (SQLException e) {
            log.error(" 根据ID查询用户失败, " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtils.close(resultSet, preparedStatement, connection);
        }
        return user;
    }

    @Override
    public Collection<User> findByAll() {
        Collection<User> list = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        String sql = "select *\n" +
                "from user;";
        try {
            connection = DBUtils.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String userId = resultSet.getString("id");
                String pwd = resultSet.getString("pwd");
                int type = resultSet.getInt("type");

                User user = new User();
                user.setId(userId);
                user.setPwd(pwd);
                user.setType(type);
                list.add(user);
            }
            log.info("查询全部用户成功");
        } catch (SQLException e) {
            log.error(" 查询全部用户失败, " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtils.close(resultSet, statement, connection);
        }
        return list;
    }

    @Override
    public int addUser(User user) {
        int returnValue = -1;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String sql = "insert into user\n" +
                "values (?, ?, ?);";
        try {
            connection = DBUtils.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, user.getId());
            preparedStatement.setString(2, user.getPwd());
            preparedStatement.setInt(3, user.getType());
            returnValue = preparedStatement.executeUpdate();
            log.info("添加用户成功!");
        } catch (Exception e) {
            log.error("添加用户失败, " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtils.close(null, preparedStatement, connection);
        }
        return returnValue;
    }

    @Override
    public int updateUser(User oldUser, User newUser) {
        int returnValue = -1;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String sql = "update user\n" +
                "set pwd  = ?,\n" +
                "    type = ?\n" +
                "where id = ?;";
        try {
            connection = DBUtils.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, newUser.getPwd());
            preparedStatement.setInt(2, newUser.getType());
            preparedStatement.setString(3, oldUser.getId());
            returnValue = preparedStatement.executeUpdate();
            log.info("修改用户数据成功!");
        } catch (Exception e) {
            log.error("修改用户数据失败, " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtils.close(null, preparedStatement, connection);
        }
        return returnValue;
    }

    @Override
    public int deleteUser(String id) {
        int returnValue = -1;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String sql = "delete\n" +
                "from user\n" +
                "where id = ?;";
        try {
            connection = DBUtils.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, id);
            returnValue = preparedStatement.executeUpdate();
            log.info("删除用户数据成功!");
        } catch (Exception e) {
            log.error("删除用户数据失败, " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtils.close(null, preparedStatement, connection);
        }
        return returnValue;
    }
}
