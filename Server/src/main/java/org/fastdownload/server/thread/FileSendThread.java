package org.fastdownload.server.thread;


import lombok.extern.log4j.Log4j2;
import org.fastdownload.server.entity.FileBlock;
import org.fastdownload.server.util.DataPackage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

/**
 * 文件发送的线程类
 *
 * @author Administrator
 */
@Log4j2
public class FileSendThread extends Thread {
    private final static int BUFFER_LENGTH = 4096;

    private final int threadId;
    private final FileBlock fileBlock;
    private final CountDownLatch startSignal;
    private final CountDownLatch doneSignal;

    /**
     * 客户端IP信息
     */
    private InetAddress clientInetAddress;

    /**
     * 客户端端口信息
     */
    private int clientPort;

    /**
     * 服务端Socket
     */
    private DatagramSocket server;

    private final static Object lock = new Object();

    public FileSendThread(int threadId, FileBlock fileBlock, DatagramPacket packet, CountDownLatch startSignal, CountDownLatch doneSignal) {
        this.threadId = threadId;
        this.fileBlock = fileBlock;
        this.startSignal = startSignal;
        this.doneSignal = doneSignal;
        this.clientInetAddress = packet.getAddress();
        this.clientPort = packet.getPort();
        initServerSocket();
        try {
            this.server.setSoTimeout(20000);
        } catch (SocketException e) {
            log.error("超时断开连接");
        }
    }

    @Override
    public void run() {
        log.info("线程 " + threadId + " 开始...");
        try {
            startSignal.await();

            // 1. 确保与客户端接收线程连接
            if (!checkConnect()) {
                log.error(threadId + " 连接失败!");
                return;
            }
            log.info(threadId + " 连接成功");

            // 如果重传数大于5次，直接放弃
            int c = 1;
            while (!doWork()) {
                c++;
                if (c > 5) {
                    break;
                }
            }

        } catch (InterruptedException ignored) {
        } finally {
            doneSignal.countDown();
        }
        log.info("线程 " + threadId + " 结束");
    }

    private boolean doWork() {
        try {
            // 2. 发送线程ID，方便客户端创建临时文件
            byte[] sendBuffer = (threadId + "").getBytes();
            DatagramPacket firstPackage = new DatagramPacket(sendBuffer, sendBuffer.length, clientInetAddress, clientPort);
            server.send(firstPackage);

            // 3. 发送文件块内容
            fileBlock.getRaf().seek(fileBlock.getStart());
            DataPackage dataPackage = new DataPackage();
            byte[] sendBuf = new byte[BUFFER_LENGTH];
            long start = fileBlock.getStart();
            long end = fileBlock.getEnd();
            // 读取的字符长度
            int n;
            // 读取次数
            long c = 0;
            // 当前读取的位置
            long i = start;
            while (i < end) {
                // 剩余长度
                int surplus = (int) (end - i);
                if (surplus < BUFFER_LENGTH && surplus > 0) {
                    sendBuf = new byte[surplus];
                }
                synchronized (lock) {
                    fileBlock.getRaf().seek(i);
                    n = fileBlock.getRaf().read(sendBuf);
                }
                if (n < 0) {
                    System.err.println("线程" + threadId + " n < 0, n = " + n);
                    break;
                }

                // 发送
                byte[] packet = dataPackage.createPacket((c + "").getBytes(), sendBuf);
                DatagramPacket sendPacket = new DatagramPacket(packet, packet.length, clientInetAddress, clientPort);
                server.send(sendPacket);

                byte[] receiveBuf = new byte[4096];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
                server.receive(receivePacket);

                String returnMsg = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8).trim();
                if (!"OK".equals(returnMsg)) {
                    System.err.println("没有收到 客户端接收了文件块" + i + " 的确认");
                    return false;
                }
                i = i + n;
                c++;
            }

            // 判断客户端是否收到，需要封装成包
            sendBuffer = "SendOver".getBytes();
            byte[] packet = dataPackage.createPacket("".getBytes(), sendBuffer);
            DatagramPacket sendPacket = new DatagramPacket(packet, packet.length, clientInetAddress, clientPort);
            server.send(sendPacket);
            log.info("发送发送完毕标志");

            byte[] receiveBuf = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
            server.receive(receivePacket);
            String returnMsg = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8).trim();
            if ("ReceiveOver".equals(returnMsg)) {
                System.err.println("收到 客户端接收了文件传输结束 的确认");
                return true;
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return false;
    }

    /**
     * 检查是否连接上客户端线程，连接成功返回true
     *
     * @return boolean
     */
    private boolean checkConnect() {
        try {
            // 接收到客户端，发送接收信息
            byte[] sendBuffer = "FileSendThread".getBytes();
            DatagramPacket firstPackage = new DatagramPacket(sendBuffer, sendBuffer.length, clientInetAddress, clientPort);
            server.send(firstPackage);

            // 接收到客户端线程端口
            byte[] receiveBuffer = new byte[4192];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            server.receive(receivePacket);
            change(receivePacket);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void change(DatagramPacket packet) {
        this.clientInetAddress = packet.getAddress();
        this.clientPort = packet.getPort();
    }

    private void initServerSocket() {
        for (int port = 1024; port <= 65535; port++) {
            try {
                this.server = new DatagramSocket(port);
                break;
            } catch (SocketException ignored) {
            }
        }
    }
}
