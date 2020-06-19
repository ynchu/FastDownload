package org.fastdownload.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileRead {
    private int threadSize;//线程条数
    private ExecutorService executorService;//线程池
    private long fileLength;//文件长度
    private File file;//读取的文件
    private RandomAccessFile rAccessFile;
    private Set<StartEndPair> startEndPairs;//记录文件分割的首尾位置
    private CyclicBarrier cyclicBarrier;
    private int sum = 0;//求和结果
    private int cancel;//用户记录正在执行的线程条数
    private long startTime;//读取文件开始时间


    public static void main(String[] args) {
        new FileRead("C:\\Users\\666\\Desktop\\1.txt", 4).start();
    }


    /**
     * 构造函数
     *
     * @param afile      想要读取的文件路径
     * @param threadSize 想要开启线程的条数
     */
    private FileRead(String afile, int threadSize) {
        this.file = new File(afile);
        this.fileLength = file.length();
        this.threadSize = threadSize;
        this.cancel = threadSize;
        try {
            this.rAccessFile = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.executorService = Executors.newFixedThreadPool(threadSize);//线程池
        startEndPairs = new HashSet<StartEndPair>();
    }

    /**
     * 启动
     * 1.记录读取文件开始时间<br>
     * 2.根据文件的长度和跑的线程条数，计算出各线程需要读取文件的初始长度
     * 3.把初始长度传入calculateStartEnd(),计算出各个片段的始末位置，把位置储存到startEndPairs
     * 4.等待所有线程齐了再开始
     * 5.遍历startEndPairs，根据记录下来的始末位置分配给各线程读取
     */
    public void start() {
        //1.记录读取文件开始时间
        startTime = System.currentTimeMillis();
        System.out.println(System.currentTimeMillis() + "开始读取文件");//打印提示到控制台
        //2.根据文件的长度和跑的线程条数，计算出各线程需要读取文件的初始长度
        long everySize = this.fileLength / this.threadSize;
        try {
            //3.把初始长度传入calculateStartEnd(),计算出各个片段的始末位置，把位置储存到startEndPairs
            calculateStartEnd(0, everySize);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
//       4.等待所有线程齐了再开始
        cyclicBarrier = new CyclicBarrier(startEndPairs.size());

//       5. 遍历startEndPairs，根据记录下来的始末位置分配给各线程读取

        for (StartEndPair pair : startEndPairs) {
            this.executorService.execute(new FileRead.SliceReaderTask(pair));

        }


    }

    /**
     * 切割文件，获取各个线程读取的始末位置
     * 1.如果起始位置在文件末位后面，则退出此程序
     * 2.new一个pair记录始末位置
     * 3.把开始位置传入pair
     * 4.根据每个线程初始分配的大小，尝试给出此片段的末位，接下来在此末位的基础上找出行尾
     * 5.开始尝试找行尾，如果片段末位在文件末位或者文件末位后面，则这个末位为文件末位，把这个位置记录到pair，
     * 这个文件只需要一个线程读取，退出后面的计算
     * 6.如果5的情况未出现，则用seek()找到给定尝试末位的位置，并获取该位置数据tmp
     * 7.只要这个末位不是换行符或者回车符，则表示不是该行行尾，往后移动一个位置，依此循环直到找到行尾
     * 8.做步骤5
     * 9.把找到的第一个片段的末位位置传入pair，至此，找到了第一个片段的始末
     * 10.把第一个片段的始末传入calculateStartEnd继续寻找下一个片段的始末位置
     *
     * @param start 读取文件的开始位置
     * @param size  每条线程读取文件的长度，此为传入的参考值，最终值依据这个值计算输出
     * @throws IOException
     */

    private void calculateStartEnd(long start, long size) throws IOException {

        if (start > fileLength - 1) {
//            1.如果起始位置在文件末位后面，则退出此程序
            return;
        }
//        2.new一个pair记录始末位置
        StartEndPair pair = new StartEndPair();
//        3.把开始位置传入pair
        pair.start = start;
//        4.根据每个线程初始分配的大小，尝试给出此片段的末位，接下来在此末位的基础上找出行尾
        long endPosition = start + size - 1;
        if (endPosition >= fileLength - 1) {
//            5.开始尝试找行尾，如果片段末位在文件末位或者文件末位后面，则这个末位为文件末位，把这个位置记录到pair，
//              这个文件只需要一个线程读取，退出后面的计算
            pair.end = fileLength - 1;
            startEndPairs.add(pair);
            return;
        }
//        6.如果5的情况未出现，则用seek()找到给定尝试末位的位置，并获取该位置数据tmp
        rAccessFile.seek(endPosition);
        byte tmp = (byte) rAccessFile.read();
        while (tmp != '\n' && tmp != '\r') {
//            7.只要这个末位不是换行符或者回车符，则表示不是该行行尾，往后移动一个位置，依此循环直到找到行尾
            endPosition++;
            if (endPosition >= fileLength - 1) {
//                8.做步骤5
                endPosition = fileLength - 1;
                break;
            }
            rAccessFile.seek(endPosition);
            tmp = (byte) rAccessFile.read();

        }
//        9.把找到的第一个片段的末位位置传入pair，至此，找到了第一个片段的始末
        pair.end = endPosition;
        startEndPairs.add(pair);
//        10.把第一个片段的始末传入calculateStartEnd继续寻找下一个片段的始末位置
        calculateStartEnd(endPosition + 1, size);//回调


    }

    /**
     * 用于记录切割片段的始末位置
     */
    private static class StartEndPair {
        public long start;
        public long end;
    }

    /**
     * 读取文件的过程
     * 1.传入读取的始末位置
     * 2.内存映射，读取文件
     * 3.按字节流读取，遍历每个字节，找出纯数字，计算加入片段总和
     * 4.片段总和计算出来后，加入文件数值总和sum
     * 5.标记此线程完成工作
     * 6.当所有线程完成工作后，打印出文件数值总和以及总共耗时，关闭线程和文件流
     */
    private class SliceReaderTask implements Runnable {
        private long start;
        private long sliceSize;

        //1.传入读取的始末位置
        public SliceReaderTask(StartEndPair pair) {
            this.start = pair.start;
            this.sliceSize = pair.end - pair.start + 1;
        }

        @Override
        public void run() {

            int asum = 0;//此片段数值求和
            int j = 0;//用于记录该字符是十位还是个位，0表示十位，1表示个位
            try {
//                2.内存映射，读取文件
                MappedByteBuffer mapBuffer = rAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, start, this.sliceSize);
                while (mapBuffer.hasRemaining()) {
//                    3.按字节流读取，遍历每个字节，找出纯数字，计算加入片段总和
                    byte by = mapBuffer.get();
                    if (by != '\n' && by != '\r' && by != ' ') {
                        char c = (char) by;
                        int result = Character.getNumericValue((int) c);
                        if (j == 0) {
//                            该字符为十位时，乘10加片段入总数
                            asum += (result * 10);
                            j = 1;
                        } else {
//                            该字符为个位时，直接加入片段总数
                            asum += result;
                            j = 0;
                        }
                    }

                }

//                4.片段总和计算出来后，加入文件数值总和sum
                sum += asum;
            } catch (Exception e) {
                e.printStackTrace();
            }
//            5.标记此线程完成工作
            cancel--;//记录此线程任务执行完毕
//            6.当所有线程完成工作后，打印出文件数值总和以及总共耗时，关闭线程
            if (cancel == 0) {
                final long endTime = System.currentTimeMillis();
                System.out.println(System.currentTimeMillis() + "读取文件完毕");
                System.out.println("求和结果为" + sum);
                //关闭文件流
                try {
                    rAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                关闭线程池
                executorService.shutdown();
                System.out.println("运行完毕，共耗时：" + (endTime - startTime) + "毫秒");
            }
        }

    }


}