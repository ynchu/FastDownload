package org.fastdownload.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;


/**
 * properties file read and write class<br>
 *
 * @author Administrator
 */
public class PropertiesUtils {
    /**
     * 配置文件的默认位置
     */
    private static final String DEFAULT_CONFIG_PATH = "docs" + File.separator + "init.properties";

    /**
     * 配置文件位置（可更改）
     */
    private static String fileName = "docs/init.properties";

    /**
     * 将键值对写入文件
     *
     * @param key      键
     * @param value    值
     * @param comments 说明
     * @param fileName 文件名
     */
    public static void writeToProperties(String key, String value, String comments, String fileName) {
        boolean flag = false;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                boolean isNewFile = file.createNewFile();
                if (isNewFile) {

                    System.out.println("文件不存在，已创建");
                } else {
                    System.err.println("文件不存在，文件创建失败!");
                }
            }
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            properties.setProperty(key, value);
            if (comments != null) {
                properties.store(new FileOutputStream(file), new String(comments.getBytes(StandardCharsets.UTF_8), "8859_1"));
            } else {
                properties.store(new FileOutputStream(file), null);
            }
            flag = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (flag) {
            System.out.println("写入文件成功");
        } else {
            System.err.println("写入文件失败");
        }
    }

    /**
     * 根据键找值
     *
     * @param key      键
     * @param fileName 文件名
     * @return String
     */
    public static String readFromProperties(String key, String fileName) {
        String result = null;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                throw new IOException("文件不存在");
            }
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            result = properties.getProperty(key);
            if (result == null) {
                throw new NullPointerException("找不到键为 " + key + " 的值");
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getFileName() {
        return fileName;
    }

    public static void setFileName(String fileName) {
        PropertiesUtils.fileName = fileName;
    }

    //    public static void main(String[] args) {
////         PropertiesUtil.writeToProperties(new KeyValue("test", "test"), "测试写入test", "docs/test.properties");
////        PropertiesUtil.writeToProperties(new KeyValue("test1", "test1"), "测试写入test1", "docs/test.properties");
////        PropertiesUtil.writeToProperties(new KeyValue("test2", "test2"), "测试写入test2", "docs/test.properties");
//
////        PropertiesUtil.writeToProperties("width", "800", "程序初始化。。。", "docs/init.properties");
//        PropertiesUtils.writeToProperties("localStorageAddress", "C:\\FastDownload\\Data", null, "docs/init.properties");
////        String width = PropertiesUtils.readFromProperties("width", "docs/init.properties");
////        System.out.println(width);
//    }
}
