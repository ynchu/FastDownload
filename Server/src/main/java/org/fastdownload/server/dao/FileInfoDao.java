package org.fastdownload.server.dao;

import org.fastdownload.server.entity.FileInfo;

import java.util.Collection;

/**
 * 文件信息类Dao层
 *
 * @author Administrator
 */
public interface FileInfoDao {
    /**
     * 根据MD5码查询文件信息
     *
     * @param md5 MD5码
     * @return FileInfo
     */
    FileInfo findByMD5(String md5);

    /**
     * 根据文件名模糊查询文件信息
     *
     * @param name 文件名
     * @return Collection<FileInfo>
     */
    Collection<FileInfo> findByName(String name);

    /**
     * 查询所有文件信息
     *
     * @return Collection<FileInfo>
     */
    Collection<FileInfo> findByAll();

    /**
     * 根据文件链接查询文件信息
     *
     * @param url 文件链接
     * @return Collection<FileInfo>
     */
    FileInfo findByUrl(String url);

    /**
     * 添加文件信息进入数据库
     *
     * @param fileInfo FileInfo实体类
     * @return int
     */
    int addFileInfo(FileInfo fileInfo);

    /**
     * 修改更新文件信息数据
     *
     * @param oldFileInfo FileInfo实体类，旧数据
     * @param newFileInfo FileInfo实体类，新数据
     * @return int
     */
    int updateFileInfo(FileInfo oldFileInfo, FileInfo newFileInfo);

    /**
     * 根据MD5码（主键）删除数据库中文件信息
     *
     * @param md5 MD5码
     * @return int
     */
    int deleteFileInfo(String md5);
}
