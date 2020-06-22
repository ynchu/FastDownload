package org.fastdownload.server.thread;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.fastdownload.server.entity.FileBasicInfo;
import org.fastdownload.server.entity.FileBlock;
import org.fastdownload.server.entity.FileInfo;
import org.fastdownload.server.service.FileInfoService;
import org.fastdownload.server.service.impl.FileInfoServiceImpl;
import org.fastdownload.server.util.FileUtils;
import org.fastdownload.server.util.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 接管文件发送的线程
 *
 * @author Administrator
 */
@Log4j2
public class SendFileService extends Thread {
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
    @Getter
    private volatile boolean isRun = false;

    /**
     * 初始化类的数据
     *
     * @param packet 客户端传入的包，包含IP、端口等信息
     */
    public SendFileService(DatagramPacket packet) {
        this.clientInetAddress = packet.getAddress();
        this.clientPort = packet.getPort();
        initServerSocket();
        try {
            this.server.setSoTimeout(20000);
        } catch (SocketException e) {
            log.error("超时断开连接");
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
    }

    private void change(DatagramPacket packet) {
        this.clientInetAddress = packet.getAddress();
        this.clientPort = packet.getPort();
    }

    @Override
    public void run() {
        if (!checkConnect()) {
            log.error("连接失败!");
            return;
        }
        log.info("连接成功");

        // 如果重传数大于5次，直接放弃
        int c = 1;
        while (!doWork()) {
            c++;
            if (c > 5) {
                break;
            }
        }
    }

    private boolean doWork() {
        log.info("线程 " + Thread.currentThread().getName() + " 开始...");


        byte[] sendBuffer;
        byte[] receiveBuffer = new byte[4192];

        /*
         * 文件收发阶段
         */

        // 线程数
        final int N = 10;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(N);
        // 文件分块
        FileBlock[] fileBlocks = new FileBlock[N];

        try {
            // 1. 接收文件名
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            server.receive(receivePacket);
            String fileName = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8).trim();
            log.info("从客户端接收到的文件名: " + fileName);

            // 2. 根据文件名从数据库中查询文件信息
            FileInfoService fileInfoService = new FileInfoServiceImpl();
            List<FileInfo> fileInfos = (List<FileInfo>) fileInfoService.findByName(fileName);

            if (fileInfos.size() == 0) {
                // 发送完不需要关闭，超时会自动关闭
                sendBuffer = "NoSuchFile".getBytes(StandardCharsets.UTF_8);
            } else {
                sendBuffer = "StartSend".getBytes(StandardCharsets.UTF_8);
            }
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientInetAddress, clientPort);
            server.send(sendPacket);

            // TODO 先默认只发送第一本书,之后会根据链接查询,这样就有唯一性
            FileInfo fileInfo = fileInfos.get(0);
            String location = fileInfo.getLocation();
            File file = new File(location);
            long fileLength = file.length();

            // 3. 拆分文件，并且封装如FileBlock中
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            long blockLength = fileLength / N;
            for (int i = 0; i < N; i++) {
                long start = i * blockLength;
                long end = (i + 1) * blockLength;
                if (i == N - 1) {
                    // 最后的全部放在最后一个中
                    if (fileLength % N != 0) {
                        end = fileLength;
                    }
                }
                fileBlocks[i] = new FileBlock(start, end, raf);
            }

            // 4. 发送查询到的文件基本信息，然后直接封装成JSON发送

            // 文件名,文件大小,文件MD5码,文件块数目
            FileBasicInfo basicInfo = new FileBasicInfo(file.getName(), fileLength, FileUtils.getMD5(file), N);
            String jsonData = JsonUtils.toJson(basicInfo);
            sendBuffer = jsonData.getBytes(StandardCharsets.UTF_8);
            sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientInetAddress, clientPort);
            server.send(sendPacket);

            // 5. 发送文件的具体内容
            for (int i = 0; i < N; i++) {
                // 开线程分段发送数据
                Thread thread = new FileSendThread(i, fileBlocks[i], receivePacket, startSignal, doneSignal);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        startSignal.countDown();
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        isRun = false;
        log.info("线程 " + Thread.currentThread().getName() + " 下载服务端线程关闭");
        log.info("线程 " + Thread.currentThread().getName() + " 结束!");
        return true;
    }

    /**
     * 检查是否连接上客户端线程，连接成功返回true
     *
     * @return boolean
     */
    private boolean checkConnect() {
        try {
            // 接收到客户端，发送接收信息
            byte[] sendBuffer = ("SendFileServer").getBytes();
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
}
