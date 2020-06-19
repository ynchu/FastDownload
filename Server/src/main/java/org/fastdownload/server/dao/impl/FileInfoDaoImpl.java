package org.fastdownload.server.dao.impl;

import lombok.extern.log4j.Log4j2;
import org.fastdownload.server.dao.FileInfoDao;
import org.fastdownload.server.entity.FileInfo;
import org.fastdownload.server.util.DBUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 文件信息Dao层实现类
 *
 * @author Administrator
 */
@Log4j2
public class FileInfoDaoImpl implements FileInfoDao {
    @Override
    public FileInfo findByMD5(String md5) {
        FileInfo fileInfo = new FileInfo();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = "select *\n" +
                "from `file_info`\n" +
                "where md5 = ?;";
        try {
            connection = DBUtils.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, md5);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String fileMd5 = resultSet.getString("md5");
                String fileName = resultSet.getString("name");
                String location = resultSet.getString("location");
                long count = resultSet.getLong("count");
                String url = resultSet.getString("url");

                fileInfo.setMd5(fileMd5);
                fileInfo.setName(fileName);
                fileInfo.setLocation(location);
                fileInfo.setCount(count);
                fileInfo.setUrl(url);
            }
            log.info("查询成功");
        } catch (SQLException e) {
            log.error(" 查询失败, " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtils.close(resultSet, preparedStatement, connection);
        }
        return fileInfo;
    }

    @Override
    public Collection<FileInfo> findByName(String name) {
        Collection<FileInfo> list = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = "select *\n" +
                "from `file_info`\n" +
                "where name like ?;";
        try {
            connection = DBUtils.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, "%" + name + "%");

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String md5 = resultSet.getString("md5");
                String fileName = resultSet.getString("name");
                String location = resultSet.getString("location");
                long count = resultSet.getLong("count");
                String url = resultSet.getString("url");

                FileInfo fileInfo = new FileInfo();
                fileInfo.setMd5(md5);
                fileInfo.setName(fileName);
                fileInfo.setLocation(location);
                fileInfo.setCount(count);
                fileInfo.setUrl(url);
                list.add(fileInfo);
            }
            log.info("查询成功");
        } catch (SQLException e) {
            log.error(" 查询失败, " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtils.close(resultSet, preparedStatement, connection);
        }
        return list;
    }

    @Override
    public Collection<FileInfo> findByAll() {
        Collection<FileInfo> list = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        String sql = "select *\n" +
                "from file_info;";
        try {
            connection = DBUtils.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String md5 = resultSet.getString("md5");
                String fileName = resultSet.getString("name");
                String location = resultSet.getString("location");
                long count = resultSet.getLong("count");
                String url = resultSet.getString("url");

                FileInfo fileInfo = new FileInfo();
                fileInfo.setMd5(md5);
                fileInfo.setName(fileName);
                fileInfo.setLocation(location);
                fileInfo.setCount(count);
                fileInfo.setUrl(url);
                list.add(fileInfo);
            }
            log.info("查询全部成功");
        } catch (SQLException e) {
            log.error(" 查询全部失败, " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtils.close(resultSet, statement, connection);
        }
        return list;
    }

    @Override
    public FileInfo findByUrl(String url) {
        FileInfo fileInfo = new FileInfo();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = "select *\n" +
                "from `file_info`\n" +
                "where url = ?;";
        try {
            connection = DBUtils.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, url);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String md5 = resultSet.getString("md5");
                String fileName = resultSet.getString("name");
                String location = resultSet.getString("location");
                long count = resultSet.getLong("count");
                String fileUrl = resultSet.getString("url");

                fileInfo.setMd5(md5);
                fileInfo.setName(fileName);
                fileInfo.setLocation(location);
                fileInfo.setCount(count);
                fileInfo.setUrl(fileUrl);
            }
            log.info("查询成功");
        } catch (SQLException e) {
            log.error("查询失败, " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtils.close(resultSet, preparedStatement, connection);
        }
        return fileInfo;
    }

    @Override
    public int addFileInfo(FileInfo fileInfo) {
        int returnValue = -1;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String sql = "insert into `file_info`\n" +
                "values (?, ?, ?, ?, ?);";
        try {
            connection = DBUtils.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, fileInfo.getMd5());
            preparedStatement.setString(2, fileInfo.getName());
            preparedStatement.setString(3, fileInfo.getLocation());
            preparedStatement.setLong(4, fileInfo.getCount());
            if (fileInfo.getUrl() == null) {
                preparedStatement.setNull(5, Types.NULL);
            } else {
                preparedStatement.setString(5, fileInfo.getUrl());
            }
            returnValue = preparedStatement.executeUpdate();
            log.info("保存数据成功!");
        } catch (Exception e) {
            log.error("修改数据失败, " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtils.close(null, preparedStatement, connection);
        }
        return returnValue;
    }

    @Override
    public int updateFileInfo(FileInfo oldFileInfo, FileInfo newFileInfo) {
        int returnValue = -1;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String sql = "update file_info\n" +
                "set name     = ?,\n" +
                "    location = ?,\n" +
                "    count    = ?,\n" +
                "    url      = ?\n" +
                "where md5 = ?;";
        try {
            connection = DBUtils.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, newFileInfo.getName());
            preparedStatement.setString(2, newFileInfo.getLocation());
            preparedStatement.setLong(3, newFileInfo.getCount());
            if (newFileInfo.getUrl() == null) {
                preparedStatement.setNull(4, Types.NULL);
            } else {
                preparedStatement.setString(4, newFileInfo.getUrl());
            }
            preparedStatement.setString(5, oldFileInfo.getMd5());
            returnValue = preparedStatement.executeUpdate();
            log.info("修改数据成功!");
        } catch (Exception e) {
            log.error("修改数据失败, " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtils.close(null, preparedStatement, connection);
        }
        return returnValue;
    }

    @Override
    public int deleteFileInfo(String md5) {
        int returnValue = -1;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String sql = "delete\n" +
                "from file_info\n" +
                "where md5 = ?;";
        try {
            connection = DBUtils.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, md5);
            returnValue = preparedStatement.executeUpdate();
            log.info("删除数据成功!");
        } catch (Exception e) {
            log.error("删除数据失败, " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtils.close(null, preparedStatement, connection);
        }
        return returnValue;
    }
}
