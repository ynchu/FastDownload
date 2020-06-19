package org.fastdownload.utils;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;

public class FileUtils {
    public static String getMD5(File file) throws IOException {
        // 开始时的当前时间
        long starttime = System.currentTimeMillis();

        if (file.isDirectory()) {
            return null;
        }
        HashCode hc = Files.hash(file, Hashing.md5());

        // 结束的时间
        long endtime = System.currentTimeMillis();
        System.out.println("计算MD5码结束，运行的时间为：" + (endtime - starttime) + "毫秒");

        return (hc != null) ? (hc.toString()) : (null);
    }

    /**
     * 将字符数组转为MD5码
     *
     * @param bytes 字符数组
     * @return MD5码
     */
    public static String md5Encode(byte[] bytes) {
        return DigestUtils.md5Hex(bytes);
    }

}
