package org.fastdownload.server.util;

import lombok.extern.log4j.Log4j2;

import java.sql.*;

/**
 * DB工具类
 *
 * @author Administrator
 */
@Log4j2
public class DBUtils {
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String DATABASE_NAME = "fastdownload";
    private static final String URL = "jdbc:mysql://localhost:3306/" + DATABASE_NAME + "?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "123456";

    // 加载数据库驱动程序
    static {
        try {
            //通过反射加载驱动程序
            Class.forName(DRIVER);
            log.info("驱动程序加载成功");
        } catch (ClassNotFoundException e) {
            log.error("驱动程序(找不到)加载失败! " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 通过驱动管理器获得数据库连接对象
     *
     * @return Connection 数据库连接对象
     */
    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASS);
            log.info("获取连接成功");
        } catch (SQLException e) {
            log.error("获取连接失败! " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * 数据库连接对象的连接状态
     *
     * @return boolean
     * @throws SQLException 数据库异常
     */
    public static boolean getConnectionStatus() throws SQLException {
        return !getConnection().isClosed();
    }

    /**
     * 关闭数据库结果集、操作对象、连接对象
     *
     * @param resultSet  结果集
     * @param statement  操作对象
     * @param connection 连接对象
     */
    public static void close(ResultSet resultSet, Statement statement, Connection connection) {
        // 1. 关闭结果集
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        // 2. 关闭操作对象
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        // 3. 关闭连接对象
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
