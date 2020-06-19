package org.fastdownload.server.service.impl;

import lombok.NoArgsConstructor;
import org.fastdownload.server.dao.FileInfoDao;
import org.fastdownload.server.dao.impl.FileInfoDaoImpl;
import org.fastdownload.server.entity.FileInfo;
import org.fastdownload.server.service.FileInfoService;

import java.util.Collection;

/**
 * 文件信息服务层实现类
 *
 * @author Administrator
 */
@NoArgsConstructor
public class FileInfoServiceImpl implements FileInfoService {
    private FileInfoDao fileInfoDao = new FileInfoDaoImpl();

    @Override
    public FileInfo findByMD5(String md5) {
        return fileInfoDao.findByMD5(md5);
    }

    @Override
    public Collection<FileInfo> findByName(String name) {
        return fileInfoDao.findByName(name);
    }

    @Override
    public FileInfo findByUrl(String url) {
        return fileInfoDao.findByUrl(url);
    }

    @Override
    public Boolean addFileInfo(FileInfo fileInfo) {
        return fileInfoDao.addFileInfo(fileInfo) != -1;
    }

    @Override
    public Boolean updateFileInfo(FileInfo oldFileInfo, FileInfo newFileInfo) {
        return fileInfoDao.updateFileInfo(oldFileInfo, newFileInfo) != -1;
    }

    @Override
    public Boolean deleteFileInfo(FileInfo fileInfo) {
        return fileInfoDao.deleteFileInfo(fileInfo.getMd5()) != -1;
    }
}
