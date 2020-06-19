package org.fastdownload.server.thread;

import lombok.extern.log4j.Log4j2;
import org.fastdownload.server.entity.FileBasicInfo;
import org.fastdownload.server.entity.FileBlock;
import org.fastdownload.server.util.JsonUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

/**
 * 接管文件发送的线程
 *
 * @author Administrator
 */
@Log4j2
public class SendFileServer extends Thread {
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

    /**
     * 线程是否运行
     */
    private volatile boolean isRun = true;

    /**
     * 超时发送的次数
     */
    private final static int TIMEOUT_COUNT = 5;

    /**
     * 初始化类的数据
     *
     * @param packet 客户端传入的包，包含IP、端口等信息
     */
    public SendFileServer(DatagramPacket packet) {
        this.clientInetAddress = packet.getAddress();
        this.clientPort = packet.getPort();
        initServerSocket();
        try {
            this.server.setSoTimeout(20000);
        } catch (SocketException e) {
            System.err.println("超时断开连接");
        }
    }

    private void initServerSocket() {
        for (int port = 1024; port <= 65535; port++) {
            try {
                this.server = new DatagramSocket(port);
                break;
            } catch (SocketException ignored) {
            }
        }
        System.out.println(server.getLocalAddress() + "/" + server.getPort());
    }

    private void change(DatagramPacket packet) {
        this.clientInetAddress = packet.getAddress();
        this.clientPort = packet.getPort();
    }

    @Override
    public void run() {
        log.info("线程 " + Thread.currentThread().getName() + " 开始...");

        if (!checkConnect()) {
            log.error("连接失败!");
            return;
        }

        byte[] sendBuffer;
        byte[] receiveBuffer = new byte[4192];


        final int N = 10;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(N);
        FileBlock[] fileBlocks = new FileBlock[N];

        // TODO 发送文件基本信息
        boolean isOk = sendFileBasicInfo();
        if (isOk) {
            // create and start threads
            // TODO 开线程分段发送数据
//            for (int i = 0; i < N; ++i) {
//                Thread thread = new FileSendThread(i, fileBlocks[i], startSignal, doneSignal);
//                thread.start();
//            }
        }

        // let all threads proceed
        startSignal.countDown();
        try {
            // wait for all to finish
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // TODO 结束时校验等

        log.info("线程 " + Thread.currentThread().getName() + " 结束!");
    }

    /**
     * 检查是否连接上客户端线程，连接成功返回true
     *
     * @return boolean
     */
    private boolean checkConnect() {
        try {
            // 接收到客户端，发送接收信息
            byte[] sendBuffer = ("我是服务器线程").getBytes();
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

    /**
     * 发送文件基本信息
     *
     * @param info FileBasicInfo文件基本信息实体类对象
     * @return boolean
     */
    private boolean sendFileBasicInfo(FileBasicInfo info) {
        try {
            String message = JsonUtils.toJson(info);
            byte[] sendBuffer = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientInetAddress, clientPort);
            server.send(sendPacket);

            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            server.receive(receivePacket);

            // 发送消息后，接收到 "OK" 后,便停止表示确认能收到
            String s = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
            if ("ReceiveFileBasicInformation".equals(s.trim())) {
                return true;
            }
            // TODO 多次发送校验
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
