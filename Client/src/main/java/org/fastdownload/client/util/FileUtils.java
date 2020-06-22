package org.fastdownload.client.util;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * 文件工具类
 *
 * @author Administrator
 */
@Log4j2
public class FileUtils {
    public static synchronized String getMD5(File file) throws IOException {
        // 开始时的当前时间
        long startTime = System.currentTimeMillis();

        if (file.isDirectory()) {
            return null;
        }
        HashCode hc = Files.hash(file, Hashing.md5());

        // 结束的时间
        long endTime = System.currentTimeMillis();
        log.info("计算MD5码结束，运行的时间为：" + (endTime - startTime) + "毫秒");

        return (hc != null) ? (hc.toString()) : (null);
    }

    /**
     * 将字符数组转为MD5码
     *
     * @param bytes 字符数组
     * @return MD5码
     */
    public static synchronized String md5Encode(byte[] bytes) {
        return DigestUtils.md5Hex(bytes);
    }

    /**
     * 获取下载文件存储的默认文件夹
     *
     * @return String
     */
    public static synchronized String getDefaultDirectory() {
        FileSystemView fileSystemView = FileSystemView.getFileSystemView();
        File defaultDirectory = fileSystemView.getDefaultDirectory();
        // 创建软件下载文件夹
        File parentFile = new File(defaultDirectory.getAbsolutePath() + File.separator + "FastDownload" + File.separator + "Data");
        if (!parentFile.exists()) {
            boolean mkdirs = parentFile.mkdirs();
            if (mkdirs) {
                log.warn("文件夹不存在，已创建");
                return parentFile.getAbsolutePath();
            } else {
                log.info("文件夹不存在，文件夹创建失败!");
                return null;
            }
        }
        return parentFile.getAbsolutePath();
    }

    /**
     * 获取下载临时文件存储的默认文件夹
     *
     * @return String
     */
    public static synchronized String getDefaultTempDirectory() {
        // 创建软件下载文件夹
        File parentFile = new File(getDefaultDirectory() + File.separator + "Temp");
        if (!parentFile.exists()) {
            boolean mkdirs = parentFile.mkdirs();
            if (mkdirs) {
                log.warn("文件夹不存在，已创建");
                return parentFile.getAbsolutePath();
            } else {
                log.info("文件夹不存在，文件夹创建失败!");
                return null;
            }
        }
        return parentFile.getAbsolutePath();
    }

    public static synchronized String getFileNameWithSuffix(String fileName) {
        int i = fileName.lastIndexOf(".");
        if (i < 0) {
            return null;
        }
        return fileName.substring(0, i);
    }

    /**
     * 合并文件
     *
     * @param fileName  文件名包括后缀，不需要加路径（路径默认）
     * @param partCount 文件被分为几个部分
     * @throws IOException 文件未找到
     */
    public static synchronized void mergeFile(String fileName, long partCount) throws IOException {
        List<FileInputStream> inputs = new ArrayList<>();
        for (int i = 0; i < partCount; i++) {
            String sourcePath = getDefaultTempDirectory() + File.separator + fileName + "_" + i + ".temp";
            inputs.add(new FileInputStream(sourcePath));
        }

        Enumeration<FileInputStream> en = Collections.enumeration(inputs);
        // 多个流 -> 一个流
        SequenceInputStream sin = new SequenceInputStream(en);

        // 指定合并后的文件输出流
        String outPath = getDefaultDirectory() + File.separator + fileName;
        OutputStream out = new FileOutputStream(outPath);

        // sin -> 输出
        byte[] buf = new byte[1024];
        int len = -1;
        while ((len = sin.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
        out.close();
        sin.close();
    }

    /**
     * 删除文件
     *
     * @param path 文件绝对路径
     * @return boolean，删除成功，返回true
     */
    public static synchronized boolean deleteFile(String path) {
        File file = new File(path);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 将对象写入文件
     *
     * @param object 需要写的对象
     * @param file   需要写入的文件
     * @throws IOException IO异常
     */
    public static synchronized void writeObject(Object object, File file) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
        oos.writeObject(object);
        oos.close();
    }

    /**
     * 从文件中读取对象
     *
     * @param file 需要写入的文件
     * @throws IOException IO异常
     */
    public static synchronized Object readObject(File file) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        Object object = ois.readObject();
        ois.close();
        return object;
    }

}
