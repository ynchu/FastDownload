package org.fastdownload.server.thread;


import lombok.extern.log4j.Log4j2;
import org.fastdownload.server.entity.FileBlock;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * 文件发送的线程类
 *
 * @author Administrator
 */
@Log4j2
public class FileSendThread extends Thread {
    private final static int BUFFER_LENGTH = 4096;

    private int threadId;
    private FileBlock fileBlock;
    private final CountDownLatch startSignal;
    private final CountDownLatch doneSignal;

    public FileSendThread(int threadId, FileBlock fileBlock, CountDownLatch startSignal, CountDownLatch doneSignal) {
        this.threadId = threadId;
        this.fileBlock = fileBlock;
        this.startSignal = startSignal;
        this.doneSignal = doneSignal;
    }

    @Override
    public void run() {
        try {
            startSignal.await();
            doWork();
        } catch (InterruptedException ignored) {
        } finally {
            doneSignal.countDown();
        }
    }

    void doWork() {
        long length = fileBlock.getEnd() - fileBlock.getStart();
        long times = (long) Math.ceil(length / BUFFER_LENGTH);
        byte[] sendBuf = new byte[BUFFER_LENGTH];
        try {
            fileBlock.getRaf().seek(fileBlock.getStart());
            // 开始一部分一部分地发送
            for (int i = 0; i < times; i++) {
                // 剩余长度
                int l = (int) (length - i * BUFFER_LENGTH);
                if (l < BUFFER_LENGTH && l > 0) {
                    sendBuf = new byte[(int) (length - i * BUFFER_LENGTH)];
                } else if (l < 0) {
                    System.err.println("文件读取结束");
                    break;
                }

                fileBlock.getRaf().read(sendBuf);

                // TODO 与之前发送部分相同，需要重新设计一下包，直接加一个第几个线程的字段


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
