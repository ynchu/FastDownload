package org.fastdownload.server.dao.impl;

import org.fastdownload.server.dao.FileInfoDao;
import org.fastdownload.server.entity.FileInfo;
import org.fastdownload.server.util.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

class FileInfoDaoImplTest {

    @Test
    void findByMD5() {
        FileInfoDao fileInfoDao = new FileInfoDaoImpl();
        FileInfo fileInfo = fileInfoDao.findByMD5("b15d8e7fb46339a8375aff5b994db138");
        System.out.println(fileInfo);
    }

    @Test
    void findByName() {
        FileInfoDao fileInfoDao = new FileInfoDaoImpl();
        Collection<FileInfo> fileInfos = fileInfoDao.findByName("1");
        fileInfos.forEach(System.out::println);
    }

    @Test
    void findByAll() {
        FileInfoDao fileInfoDao = new FileInfoDaoImpl();
        Collection<FileInfo> fileInfos = fileInfoDao.findByAll();
        fileInfos.forEach(System.out::println);
    }

    @Test
    void findByUrl() {
        FileInfoDao fileInfoDao = new FileInfoDaoImpl();
        FileInfo fileInfo = fileInfoDao.findByUrl("fastdownload://d8b32c89258834568bc69545d6d84530%1.png");
        System.out.println(fileInfo);
    }

    @Test
    void addFileInfo() {
        FileInfoDao fileInfoDao = new FileInfoDaoImpl();
        FileInfo fileInfo = new FileInfo();
        fileInfo.setLocation("C:\\FastDownload\\Data\\1.flv");
//        fileInfo.setLocation("C:\\FastDownload\\Data\\1.png");
        File file = new File(fileInfo.getLocation());
        try {
            fileInfo.setMd5(FileUtils.getMD5(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileInfo.setName(file.getName());
        fileInfo.setUrl("fastdownload://" + fileInfo.getMd5() + "%" + fileInfo.getName());
        fileInfoDao.addFileInfo(fileInfo);
    }

    @Test
    void updateFileInfo() {
        FileInfoDao fileInfoDao = new FileInfoDaoImpl();
        FileInfo oldFileInfo = fileInfoDao.findByMD5("12534");
        System.out.println("旧的: " + oldFileInfo);
        FileInfo newFileInfo = new FileInfo();

        newFileInfo.setMd5(oldFileInfo.getMd5());
        newFileInfo.setName("drjhijhiortjhityjiojvjn");
        newFileInfo.setLocation("C:\\FastDownload\\Data\\1.png");
        newFileInfo.setCount(100);
        newFileInfo.setUrl("fastdownload://" + newFileInfo.getMd5() + "%" + newFileInfo.getName());
        System.out.println("新的: " + newFileInfo);
        System.out.println();
        int i = fileInfoDao.updateFileInfo(oldFileInfo, newFileInfo);
        System.out.println(i != -1);
    }

    @Test
    void deleteFileInfo() {
        FileInfoDao fileInfoDao = new FileInfoDaoImpl();
        int i = fileInfoDao.deleteFileInfo("12534");
        System.out.println(i != -1);
    }
}