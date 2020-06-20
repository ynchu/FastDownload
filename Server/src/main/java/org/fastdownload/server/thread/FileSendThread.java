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

    private int threadId;
    private FileBlock fileBlock;
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
            doWork();
        } catch (InterruptedException ignored) {
        } finally {
            doneSignal.countDown();
        }
        log.info("线程 " + threadId + " 结束");
    }

    void doWork() {
        // 1. 确保与客户端接收线程连接
        if (!checkConnect()) {
            log.error(threadId + " 连接失败!");
            return;
        }
        log.info(threadId + " 连接成功");


        try {
            // 2. 发送线程ID，方便客户端创建临时文件
            byte[] sendBuffer = (threadId + "").getBytes();
            DatagramPacket firstPackage = new DatagramPacket(sendBuffer, sendBuffer.length, clientInetAddress, clientPort);
            server.send(firstPackage);

            // 3. 发送文件块内容
            long length = fileBlock.getEnd() - fileBlock.getStart();


            System.err.println(Thread.currentThread().getName() + "\tstart = " + fileBlock.getStart() + "\tend = " + fileBlock.getEnd());

            long times = (long) Math.ceil(length / BUFFER_LENGTH);
            fileBlock.getRaf().seek(fileBlock.getStart());

            DataPackage dataPackage = new DataPackage();

            byte[] sendBuf = new byte[BUFFER_LENGTH];
            int n;

            System.err.println(Thread.currentThread().getName() + "\tlength = " + length);


            for (int i = 0; i < times; i++) {
                // TODO 与之前发送部分相同，需要重新设计一下包，直接加一个第几个线程的字段
                n = fileBlock.getRaf().read(sendBuf);
                if (n < BUFFER_LENGTH) {
                    byte[] bytes = new byte[n];
                    System.arraycopy(sendBuf, 0, bytes, 0, n);
                    sendBuf = bytes;
                }

                System.err.println(Thread.currentThread().getName() + "\ti = " + i + "\tn = " + n);

                // 发送
                byte[] packet = dataPackage.createPacket((i + "").getBytes(), sendBuf);
                DatagramPacket sendPacket = new DatagramPacket(packet, packet.length, clientInetAddress, clientPort);
                server.send(sendPacket);

                byte[] receiveBuf = new byte[4096];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
                server.receive(receivePacket);
                String returnMsg = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8).trim();

                if (!"OK".equals(returnMsg)) {
                    System.err.println("没有收到 客户端接收了文件块" + i + " 的确认");
                    // TODO 暂不处理重传
                }
            }

//            int i = 0;
//            while ((n = fileBlock.getRaf().read(sendBuf)) != -1) {
//                // TODO 与之前发送部分相同，需要重新设计一下包，直接加一个第几个线程的字段
//                if (n < BUFFER_LENGTH) {
//                    byte[] bytes = new byte[n];
//                    System.arraycopy(sendBuf, 0, bytes, 0, n);
//                    sendBuf = bytes;
//                }
//
//                // 发送
//                byte[] packet = dataPackage.createPacket((i + "").getBytes(), sendBuf);
//                DatagramPacket sendPacket = new DatagramPacket(packet, packet.length, clientInetAddress, clientPort);
//                server.send(sendPacket);
//
//                byte[] receiveBuf = new byte[4096];
//                DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
//                server.receive(receivePacket);
//                String returnMsg = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8).trim();
//
//                if (!"OK".equals(returnMsg)) {
//                    System.err.println("没有收到 客户端接收了文件块" + i + " 的确认");
//                    // TODO 暂不处理重传
//                }
//                i++;
//            }


            // 判断客户端是否收到
            sendBuffer = "SendOver".getBytes();
            byte[] packet = dataPackage.createPacket("over".getBytes(), sendBuffer);
            DatagramPacket sendPacket = new DatagramPacket(packet, packet.length, clientInetAddress, clientPort);
            server.send(sendPacket);

            byte[] receiveBuf = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
            server.receive(receivePacket);
            String returnMsg = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8).trim();
            if ("ReceiveOver".equals(returnMsg)) {
                System.err.println("收到 客户端接收了文件传输结束 的确认");
            }

        } catch (IOException e) {
//            e.printStackTrace();
            System.err.println(e.getMessage());
        }
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
