package org.fastdownload.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileMultiThreadReader {
    /**
     * 待读取的文件
     */
    private File file;

    /**
     * 文件随机访问
     */
    private RandomAccessFile randomAccessFile;

    /**
     * 线程数
     */
    private int threadSize;

    /**
     * 记录文件分割的起始位置和终止位置
     */
    private Set<FilePair> pairSet;

    /**
     * 线程池
     */
    private ExecutorService executorService;

    public FileMultiThreadReader(String fileName, int threadSize) {
        this.file = new File(fileName);
        this.threadSize = threadSize;
        try {
            this.randomAccessFile = new RandomAccessFile(this.file, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.executorService = Executors.newFixedThreadPool(threadSize);
        this.pairSet = new HashSet<>();
    }

    private void getPair() {
        long fileLength = file.length();

        long pairLength = 0;
        if (fileLength % threadSize != 0) {
            pairLength = fileLength / threadSize + 1;
        } else {
            pairLength = fileLength / threadSize;
        }

        System.out.println(fileLength + " - " + pairLength);

        new Thread(new ReadTask(randomAccessFile, new FilePair(6, pairLength))).start();

//        try {
//            System.out.println("输入内容：" + randomAccessFile.getFilePointer());
//            //移动文件记录指针的位置
//            randomAccessFile.seek(10);
//
//            byte[] b = new byte[1024];
//            int hasRead = 0;
//            //循环读取文件
//            while ((hasRead = randomAccessFile.read(b)) > 0) {
//                //输出文件读取的内容
//                System.out.print(new String(b, 0, hasRead));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private class ReadTask implements Runnable {
        private RandomAccessFile randomAccessFile;
        private FilePair pair;
//        private final static int PACKAGE_SIZE = 8192;
//        private final static int BUFFER_SIZE = 1024;
        private final static int PACKAGE_SIZE = 15;
        private final static int BUFFER_SIZE = 3;

        public ReadTask(RandomAccessFile randomAccessFile, FilePair pair) {
            this.randomAccessFile = randomAccessFile;
            this.pair = pair;
        }

        @Override
        public void run() {
            try {
                randomAccessFile.seek(pair.getStartPoint());
                byte[] buffer = new byte[BUFFER_SIZE];
                int n = 0;
                int c = 1;
                //循环读取文件
                while ((n = randomAccessFile.read(buffer)) > 0 && (c * BUFFER_SIZE <= PACKAGE_SIZE)) {
                    //输出文件读取的内容
                    System.out.println("\nc = " + c);
                    System.out.println(randomAccessFile.getFilePointer());
                    System.out.print(new String(buffer, 0, n));
                    c++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void start() {
        // TODO
    }

    public static void main(String[] args) {
        FileMultiThreadReader fileMultiThreadReader = new FileMultiThreadReader("docs\\init.properties", 5);
        fileMultiThreadReader.getPair();
    }
}
