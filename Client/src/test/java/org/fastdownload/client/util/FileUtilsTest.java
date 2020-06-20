package org.fastdownload.client.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class FileUtilsTest {

    @Test
    void getMD5() {
        try {
            File file1 = new File("C:\\FastDownload\\Data\\1.mp3");
            String md51 = FileUtils.getMD5(file1);
            System.out.println(md51);

            File file2 = new File("D:\\Users\\Administrator\\Documents\\FastDownload\\Data\\1.txt");
            String md52 = FileUtils.getMD5(file2);
            System.out.println(md52);

            assert md51 != null;
            System.out.println(md51.equals(md52));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void md5Encode() {
    }

    @Test
    void getDefaultDirectory() {
        String directory = FileUtils.getDefaultDirectory();
        System.out.println(directory);
    }

    @Test
    void getDefaultTempDirectory() {
        String tempDirectory = FileUtils.getDefaultTempDirectory();
        System.out.println(tempDirectory);
    }

    @Test
    void getFileName() {
        String fileName = "1.flv.flv";
        String name = FileUtils.getFileNameWithSuffix(fileName);
        System.out.println(name);
    }
}