package org.fastdownload.client.service;

import javafx.application.Platform;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.fastdownload.client.entity.FileBasicInfo;
import org.fastdownload.client.entity.FileBlock;
import org.fastdownload.client.gui.controller.UploadWindowController;
import org.fastdownload.client.util.DataPackage;
import org.fastdownload.client.util.FileUtils;
import org.fastdownload.client.util.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

/**
 * 上传文件服务
 *
 * @author Administrator
 */
@Log4j2
public class UploadFileService extends Thread {
    /**
     * 服务器IP信息
     */
    public final static String SERVER_HOST = "127.0.0.1";

    /**
     * 服务器端口信息
     */
    public final static int SERVER_PORT = 8888;

    /**
     * 客户端端Socket
     */
    private DatagramSocket client;

    /**
     * 服务端IP信息
     */
    private InetAddress serverInetAddress;

    /**
     * 服务端端口信息
     */
    private int serverPort;

    private String fileName;

    private final static int BUFFER_LENGTH = 4096;
    private final static Object lock = new Object();

    /**
     * 线程是否运行完成
     */
    @Getter
    private volatile boolean isRun = false;


    private UploadWindowController controller;

    public UploadFileService(String fileName, UploadWindowController controller) {
        this.controller = controller;
        this.fileName = fileName;
        try {
            this.client = new DatagramSocket();
            this.client.setSoTimeout(20000);
        } catch (SocketException ignored) {
            log.error("超时断开连接");
        }
    }

    @Override
    public void run() {
        doWork();
    }

    private void doWork() {
        // 1. 连接服务端
        if (!checkConnect()) {
            log.error("连接失败!");
            // 连接失败继续执行连接
        }
        System.err.println("连接成功");

        try {
            // 2. 发送文件基本信息
            byte[] sendBuffer;

            File file = new File(fileName);
            long fileLength = file.length();
            FileBasicInfo basicInfo = new FileBasicInfo(file.getName(), fileLength, FileUtils.getMD5(file), 1);
            String jsonData = JsonUtils.toJson(basicInfo);
            sendBuffer = jsonData.getBytes(StandardCharsets.UTF_8);
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverInetAddress, serverPort);
            client.send(sendPacket);

            // 接收返回的信息
            byte[] receiveBuf = new byte[4096];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
            client.receive(receivePacket);
            String msg = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8).trim();
            if ("FileAlreadyExist".equals(msg)) {
                log.info("完成");
                Platform.runLater(() -> {
                    controller.update(1);
                });
                return;
            }
            log.info("开始上传文件内容");

            // 3. 单线程发送文件
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            FileBlock fileBlock = new FileBlock(0, fileLength, raf);
            DataPackage dataPackage = new DataPackage();

            long start = fileBlock.getStart();
            long end = fileBlock.getEnd();
            // 当前读取位置
            long i = start;
            // 读取的字节数
            int n;
            // 读取次数
            int c = 0;
            while (i < end) {
                // 剩余长度
                int surplus = (int) (end - i);
                if (surplus < BUFFER_LENGTH && surplus > 0) {
                    sendBuffer = new byte[surplus];
                }
                synchronized (lock) {
                    fileBlock.getRaf().seek(i);
                    n = fileBlock.getRaf().read(sendBuffer);
                }
                if (n < 0) {
                    break;
                }

                // 发送
                byte[] packet = dataPackage.createPacket((c + "").getBytes(), sendBuffer);
                sendPacket = new DatagramPacket(packet, packet.length, serverInetAddress, serverPort);
                client.send(sendPacket);

                // 接收返回的信息
                receiveBuf = new byte[4096];
                receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
                client.receive(receivePacket);

                String returnMsg = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8).trim();
                if (!"OK".equals(returnMsg)) {
                    log.info("没有收到 客户端接收了文件块" + c + " 的确认");
                }
                i = i + n;
                c++;

                long finalI = i;
                Platform.runLater(() -> {
                    // 更新进度条
                    controller.update((double) finalI / fileLength);
                });
            }

            // 判断客户端是否收到，需要封装成包
            sendBuffer = "SendOver".getBytes();
            byte[] packet = dataPackage.createPacket("".getBytes(), sendBuffer);
            sendPacket = new DatagramPacket(packet, packet.length, serverInetAddress, serverPort);
            client.send(sendPacket);
            log.info("发送发送完毕标志");

            receiveBuf = new byte[1024];
            receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
            client.receive(receivePacket);
            String returnMsg = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8).trim();
            if ("ReceiveOver".equals(returnMsg)) {
                System.err.println("收到 客户端接收了文件传输结束 的确认");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("上传完成");
    }

    /**
     * 检查是否连接上服务端下载线程，连接成功返回true
     *
     * @return boolean
     */
    private boolean checkConnect() {
        try {
            client = new DatagramSocket();
            // 发送下载请求
            byte[] sendBuffer = "UploadRequest".getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(SERVER_HOST), SERVER_PORT);
            client.send(sendPacket);

            // 接收到服务端返回的信息
            byte[] receiveBuffer = new byte[4192];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            client.receive(receivePacket);
            String msg = new String(receivePacket.getData(), StandardCharsets.UTF_8).trim();
            if ("UploadFileService".equals(msg)) {
                change(receivePacket);
                // 接收到服务端反馈之后便
                sendBuffer = "OK".getBytes();
                sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverInetAddress, serverPort);
                client.send(sendPacket);
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void change(DatagramPacket packet) {
        this.serverInetAddress = packet.getAddress();
        this.serverPort = packet.getPort();
    }
}
