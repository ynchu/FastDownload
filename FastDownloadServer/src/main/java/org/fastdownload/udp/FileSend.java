package org.fastdownload.udp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class FileSend implements Runnable {
    private DatagramSocket serverSocket;

    /**
     * 超时发送的次数
     */
    private final static int TIMEOUT_COUNT = 5;

    public FileSend() {
        try {
            serverSocket = new DatagramSocket(22020);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("start send");
        try {
            // 从客户端接收第一个数据
            byte[] receiveBuf = new byte[4096];
            DatagramPacket receivePackage = new DatagramPacket(receiveBuf, receiveBuf.length);
            serverSocket.receive(receivePackage);
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

            long fileLength = file.length();
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            byte[] sendBuf = new byte[4096];
            DatagramPacket sendPackage = new DatagramPacket(sendBuf, sendBuf.length, receivePackage.getAddress(), receivePackage.getPort());
            long times = fileLength / sendBuf.length + 1;
            System.err.println(times);
            double t = times / 100.0 * 100;

            synchronized (this) {
                for (int i = 0; i < times; i++) {
                    // 剩余长度
                    int l = (int) (fileLength - i * 4096);
                    if (l < 4096 && l > 0) {
                        System.out.println("last : " + (fileLength - i * 4096));
                        sendBuf = new byte[(int) (fileLength - i * 4096)];
                        System.out.println(sendBuf.length);
                    } else if (l < 0) {
                        System.err.println("文件读取结束");
                        break;
                    }
                    int n = bis.read(sendBuf);

                    // 发送片段大小
                    sendMessage("" + n, receivePackage.getAddress(), receivePackage.getPort());

                    serverSocket.send(sendPackage);

                    System.out.println("已发送 " + (i + 1) + "/" + times + ", " + String.format("%.2f", i / t * 100) + "%, n=" + n);
                    sendMessage("" + (i + 1) + "/" + times, receivePackage.getAddress(), receivePackage.getPort());

                    // 返回的信息
                    if (!"OK".equals(getMessage())) {
                        System.err.println("没有收到 客户端接收了文件块" + i + " 的确认");
                        // TODO 暂不处理重传
                    }
                }
                if (checkSend("send over", "receive over", receivePackage)) {
                    System.err.println("没有收到 客户端接收了文件传输结束 的确认");
                    return;
                }
            }
            bis.close();
            /*
             * 文件发送相关操作结束
             */


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("end send");
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
        serverSocket.send(sendPackage);
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
        serverSocket.send(sendPackage);
        System.out.println("send to client: " + message);

        // receive data from client
        byte[] receiveBuf = new byte[4096];
        DatagramPacket receivePackage = new DatagramPacket(receiveBuf, receiveBuf.length);
        serverSocket.receive(receivePackage);
        return new String(receivePackage.getData(), 0, receivePackage.getLength(), StandardCharsets.UTF_8);
    }

    private String[] sendMessage(String message) throws IOException {
        // receive data from server
        byte[] receiveBuf = new byte[4096];
        DatagramPacket receivePackage = new DatagramPacket(receiveBuf, receiveBuf.length);
        serverSocket.receive(receivePackage);
        String s = new String(receivePackage.getData(), 0, receivePackage.getLength(), StandardCharsets.UTF_8);
        System.out.println("receive from client: " + s);

        // send data to server
        byte[] sendBuf = message.getBytes(StandardCharsets.UTF_8);
        DatagramPacket sendPackage = new DatagramPacket(sendBuf, sendBuf.length, receivePackage.getAddress(), receivePackage.getPort());
        serverSocket.send(sendPackage);
        System.out.println("send to client: " + message);

        return new String[]{String.valueOf(receivePackage.getAddress()), String.valueOf(receivePackage.getPort())};
    }

    private String getMessage() throws IOException {
        // receive data from client
        byte[] receiveBuf = new byte[4096];
        DatagramPacket receivePackage = new DatagramPacket(receiveBuf, receiveBuf.length);
        serverSocket.receive(receivePackage);
        return new String(receivePackage.getData(), 0, receivePackage.getLength(), StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        new Thread(new FileSend()).start();
    }

}
