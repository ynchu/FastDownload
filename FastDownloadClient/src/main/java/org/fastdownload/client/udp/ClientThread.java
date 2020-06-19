package org.fastdownload.client.udp;


import javafx.application.Platform;
import org.fastdownload.client.gui.DownloadWindowController;
import org.fastdownload.client.utils.DataPackage;
import org.fastdownload.client.utils.FileUtils;

import javax.swing.filechooser.FileSystemView;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 客户端线程，相当于客户端上每个下载线程
 */
public class ClientThread implements Runnable {
    /**
     * 服务器IP信息
     */
    private final InetAddress serverInetAddress;

    /**
     * 服务器端口信息
     */
    private final int serverPort;

    /**
     * 客户端Socket
     */
    private DatagramSocket client;

    /**
     * 线程是否运行
     */
    private volatile boolean isRun = true;

    /**
     * 超时发送的次数
     */
    private final static int TIMEOUT_COUNT = 5;
    private String fileName;
    private DownloadWindowController controller;

    /**
     * 初始化类的数据
     *
     * @param packet 客户端传入的包，包含IP、端口等信息
     */
    public ClientThread(DatagramPacket packet, String fileName, DownloadWindowController controller) {
        this.serverInetAddress = packet.getAddress();
        this.serverPort = packet.getPort();
        this.fileName = fileName;
        this.controller = controller;
        initServerSocket();
    }

    private void initServerSocket() {
        for (int port = 1024; port <= 65535; port++) {
            try {
                this.client = new DatagramSocket(port);
                break;
            } catch (SocketException ignored) {
            }
        }
        System.out.println(client.getLocalAddress() + "/" + client.getPort());
    }

    @Override
    public void run() {
        System.err.println("线程 " + Thread.currentThread().getName() + " 开始...");

        byte[] sendBuffer;
        byte[] receiveBuffer = new byte[4192];

        try {
            // 发送给服务端新端口
            sendBuffer = ("我是客户端线程").getBytes();
            DatagramPacket firstPackage = new DatagramPacket(sendBuffer, sendBuffer.length, serverInetAddress, serverPort);
            client.send(firstPackage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scanner scanner = new Scanner(System.in);

        // TODO 具体通讯
//        System.out.println("开始通讯...");
//        int count = 1;
//        while (isRun) {
//            try {
////                String msgSend = "我是客户端" + client.getLocalSocketAddress() + ", 这是第 " + count + " 条信息.";
//                System.err.println("请输入:");
//                String msgSend = scanner.nextLine().trim();
//                sendBuffer = msgSend.getBytes();
//                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverInetAddress, serverPort);
//                client.send(sendPacket);
//
//                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
//                client.receive(receivePacket);
//                String msgReceive = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
//                System.out.println("客户端(" + receivePacket.getSocketAddress() + ")说: " + msgReceive);
//
//                if (count >= 30) {
//                    close();
//                }
//                count++;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        handle();

        System.err.println("线程 " + Thread.currentThread().getName() + " 结束");
    }


    private void handle() {
        try {
            System.out.println("start download");

            // 发送所需要下载的文件名，如果对方接收到了，返回 "OK"
            if (checkSend(fileName, "OK")) {
                return;
            }
            System.out.println("连接成功");

            /*
             * TODO 之后开始接收文件，先单线程，之后再多线程吧
             * 文件接收相关操作开始
             */
            String nameAndLength = getMessage();
            sendMessageWithoutResult("OK");
            System.out.println(nameAndLength);

            String name = nameAndLength.split("/")[0];
            long length = Long.parseLong(nameAndLength.split("/")[1]);

            // 获取"我的文档"的路径
            FileSystemView fileSystemView = FileSystemView.getFileSystemView();
            File defaultDirectory = fileSystemView.getDefaultDirectory();

            // 创建软件下载文件夹
            File parentFile = new File(defaultDirectory.getAbsolutePath() + File.separator + "FastDownload" + File.separator + "Data");
            if (!parentFile.exists()) {
                boolean mkdirs = parentFile.mkdirs();
                if (mkdirs) {
                    System.out.println("文件夹不存在，已创建");
                } else {
                    System.err.println("文件夹不存在，文件夹创建失败!");
                }
            }

            // 下载的文件位置
            String filePath = parentFile.getAbsolutePath() + File.separator + name;
            File file = new File(filePath);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

            String times = getMessage();
            double total = Double.parseDouble(times);

            DataPackage dataPackage = new DataPackage();

            // 接收块大小
            byte[] receiveBuf = new byte[4192];
            while (true) {
                //判断是否被中断
                if (Thread.currentThread().isInterrupted()) {
                    //处理中断逻辑
                    return;
                }

                // 接收文件内容
                DatagramPacket receivePackage = new DatagramPacket(receiveBuf, receiveBuf.length);
                client.receive(receivePackage);

                dataPackage.getPackageData(receivePackage.getData());

                // MD5码校验
                boolean equals = FileUtils.md5Encode(dataPackage.getData()).equals(new String(dataPackage.getCheck(), StandardCharsets.UTF_8));
                if (!equals) {
                    // TODO 发送错误信息
                    System.err.println("内容错误");
                }

                String s = new String(dataPackage.getLength(), StandardCharsets.UTF_8);
                int len = Integer.parseInt(s);
                // 接收并计算进度
                double c = Double.parseDouble(new String(dataPackage.getSequence()));

//                System.out.println("已下载 " + c + "/" + total + ", " + String.format("%.2f", c / total * 100) + "%, n=" + new String(dataPackage.getLength()));

                Platform.runLater(() -> {
                    controller.update(c, total, new String(dataPackage.getLength()).trim());
                });

                // 写入文件
                synchronized (this) {
                    bos.write(dataPackage.getData(), 0, len);
                    bos.flush();
                }

                // 接收到发送信息，返回 "OK"
                sendMessageWithoutResult("OK");

                if (len < 4096) {
                    break;
                }
            }
            // 结束时确认
            if ("send over".equals(getMessage())) {
                bos.close();
                System.out.println("is over!");
                // 接收到发送完毕信息，返回 "receive over"
                sendMessageWithoutResult("receive over");
            }

            // 计算MD5码
            System.out.println();
            String md5 = FileUtils.getMD5(new File(filePath));
            System.out.println(md5);

            Platform.runLater(() -> {
                controller.getMessage().setText("下载完成!!!");
                System.out.println(controller.getProgressIndicator().getHeight());
            });

            /*
             * 文件接收相关操作结束
             */

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean checkSend(String message, String returnMsg) throws IOException, InterruptedException {
        // 发送收到，如果没有收到回复，则一直发送
        int count = 0;
        while (!returnMsg.equals(sendMessage(message))) {
            count++;
            // 超过5次则退出
            if (count >= TIMEOUT_COUNT) {
                System.err.println("超时连接...");
                return true;
            }
            Thread.sleep(500);
        }
        return false;
    }

    private String sendMessage(String message) throws IOException {
        // 由于服务器是确定的，所以直接省去已知的参数传递
        return sendMessageWithReturn(message, serverInetAddress, serverPort);
    }

    private void sendMessageWithoutResult(String message) throws IOException {
        sendMessage(message, serverInetAddress, serverPort);
    }

    /**
     * 给客户端发送信息，不需要客户端的反馈
     *
     * @param message     信息内容
     * @param inetAddress 客户端地址
     * @param port        客户端端口
     * @throws IOException IO异常
     */
    private void sendMessage(String message, InetAddress inetAddress, int port) throws IOException {
        // send data to server
        byte[] sendBuf = message.getBytes(StandardCharsets.UTF_8);
        DatagramPacket sendPackage = new DatagramPacket(sendBuf, sendBuf.length, inetAddress, port);
        client.send(sendPackage);
    }

    /**
     * 给客户端发送信息，需要客户端的反馈
     *
     * @param message     信息内容
     * @param inetAddress 客户端地址
     * @param port        客户端端口
     * @throws IOException IO异常
     */
    private String sendMessageWithReturn(String message, InetAddress inetAddress, int port) throws IOException {
        // send data to client
        byte[] sendBuf = message.getBytes(StandardCharsets.UTF_8);
        DatagramPacket sendPackage = new DatagramPacket(sendBuf, sendBuf.length, inetAddress, port);
        client.send(sendPackage);
        System.out.println("send to client: " + message);

        // receive data from client
        byte[] receiveBuf = new byte[4096];
        DatagramPacket receivePackage = new DatagramPacket(receiveBuf, receiveBuf.length);
        client.receive(receivePackage);
        return new String(receivePackage.getData(), 0, receivePackage.getLength(), StandardCharsets.UTF_8);
    }

    private String getMessage() throws IOException {
        // receive data from client
        byte[] receiveBuf = new byte[4096];
        DatagramPacket receivePackage = new DatagramPacket(receiveBuf, receiveBuf.length);
        client.receive(receivePackage);
        return new String(receivePackage.getData(), 0, receivePackage.getLength(), StandardCharsets.UTF_8);
    }

    public void close() {
        this.isRun = false;
    }

}
