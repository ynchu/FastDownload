package org.fastdownload.udp;

import org.fastdownload.utils.DataPackage;
import org.fastdownload.utils.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class ServerThread implements Runnable {
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
    public ServerThread(DatagramPacket packet) {
        this.clientInetAddress = packet.getAddress();
        this.clientPort = packet.getPort();
        initServerSocket();
        try {
            this.server.setSoTimeout(10000);
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
        System.err.println("线程 " + Thread.currentThread().getName() + " 开始...");

        byte[] sendBuffer;
        byte[] receiveBuffer = new byte[4192];

        //
        try {
            // 接收到客户端，发送接收信息
            sendBuffer = ("我是服务器线程").getBytes();
            DatagramPacket firstPackage = new DatagramPacket(sendBuffer, sendBuffer.length, clientInetAddress, clientPort);
            server.send(firstPackage);

            // 接收到客户端线程端口
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            server.receive(receivePacket);
            change(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
        }


        // TODO 具体通讯
//        int count = 1;
//        while (isRun) {
//            try {
//                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
//                server.receive(receivePacket);
//                String msgReceive = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
//                System.out.println("客户端(" + receivePacket.getSocketAddress() + ")说: " + msgReceive);
//
//                String msgSend = "我是服务器" + server.getLocalSocketAddress() + ", 这是第 " + count + " 条信息.";
//                sendBuffer = msgSend.getBytes();
//                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientInetAddress, clientPort);
//                server.send(sendPacket);
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


        System.err.println("线程 " + Thread.currentThread().getName() + " 结束!!!");
    }

    private void handle() {
        System.out.println("start send");
        try {
            // 从客户端接收第一个数据
            byte[] receiveBuf = new byte[4096];
            DatagramPacket receivePackage = new DatagramPacket(receiveBuf, receiveBuf.length);
            server.receive(receivePackage);
            String fileName = new String(receivePackage.getData(), 0, receivePackage.getLength(), StandardCharsets.UTF_8);
            System.out.println("receive from client: " + fileName);

            // 接收到信息之后，发送 "OK"
            sendMessage("OK", receivePackage.getAddress(), receivePackage.getPort());
            System.out.println("确定连接成功，开始发送文件");

            /*
             * TODO 之后开始发送文件，先单线程，之后再多线程吧
             * 文件发送相关操作开始
             */
            File file = new File(fileName);

            // 发送文件名和文件大小，格式: 文件名/大小.如果对方接收到了，返回 "OK"
            String msg = file.getName() + "/" + file.length();
            System.out.println(msg);
            if (checkSend(msg, "OK", receivePackage)) {
                System.err.println("没有收到 客户端接收文件名和文件大小 的确认");
                return;
            }

            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

            byte[] sendBuf = new byte[4096];
            long fileLength = file.length();
            long times = fileLength / sendBuf.length + 1;
            System.err.println(times);
            double t = times / 100.0 * 100;

            // 发送总块数
            sendMessage(times + "", receivePackage.getAddress(), receivePackage.getPort());

            DataPackage dataPackage = new DataPackage();

            synchronized (this) {
                for (int i = 0; i < times; i++) {
                    // 剩余长度
                    int l = (int) (fileLength - i * 4096);
                    if (l < 4096 && l > 0) {
                        sendBuf = new byte[(int) (fileLength - i * 4096)];
                    } else if (l < 0) {
                        System.err.println("文件读取结束");
                        break;
                    }
                    int n = bis.read(sendBuf);

                    // 发送
                    byte[] packet = dataPackage.createPacket((i + 1 + "").getBytes(), sendBuf);
                    assert packet != null;

                    DatagramPacket sendPackage = new DatagramPacket(packet, packet.length, receivePackage.getAddress(), receivePackage.getPort());
                    server.send(sendPackage);

//                    System.out.println("已发送 " + (i + 1) + "/" + times + ", " + String.format("%.2f", i / t * 100) + "%, n=" + n);

                    // 返回的信息
                    String returnMsg = getMessage();
                    if (!"OK".equals(returnMsg)) {
                        System.err.println("没有收到 客户端接收了文件块" + i + " 的确认");
                        // TODO 暂不处理重传
                    }

//                    if ("ERROR".equals(returnMsg)) {
//                        System.err.println("收到 客户端接收了文件块" + i + " 的确认，但是该文件块出错了。");
//                        // TODO 暂不处理重传
//                    }
                }
                if (checkSend("send over", "receive over", receivePackage)) {
                    System.err.println("没有收到 客户端接收了文件传输结束 的确认");
                    return;
                }
            }
            bis.close();

            // 计算MD5码
            System.out.println();
            String md5 = FileUtils.getMD5(new File(fileName));
            System.out.println(md5);

            /*
             * 文件发送相关操作结束
             */
        } catch (IOException |
                InterruptedException e) {
            e.printStackTrace();
        }

    }

    private boolean checkSend(String message, String returnMsg, DatagramPacket receivePackage) throws IOException, InterruptedException {
        // 发送收到，如果没有收到回复，则一直发送
        int count = 0;
        while (!returnMsg.equals(sendMessageWithReturn(message, receivePackage.getAddress(), receivePackage.getPort()))) {
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
        server.send(sendPackage);
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
        server.send(sendPackage);
        System.out.println("send to client: " + message);

        // receive data from client
        byte[] receiveBuf = new byte[4096];
        DatagramPacket receivePackage = new DatagramPacket(receiveBuf, receiveBuf.length);
        server.receive(receivePackage);
        return new String(receivePackage.getData(), 0, receivePackage.getLength(), StandardCharsets.UTF_8);
    }

    private String getMessage() throws IOException {
        // receive data from client
        byte[] receiveBuf = new byte[4096];
        DatagramPacket receivePackage = new DatagramPacket(receiveBuf, receiveBuf.length);
        server.receive(receivePackage);
        return new String(receivePackage.getData(), 0, receivePackage.getLength(), StandardCharsets.UTF_8);
    }

    public void close() {
        this.isRun = false;
    }
}
