package org.fastdownload.client.thread;

import lombok.extern.log4j.Log4j2;
import org.fastdownload.client.util.DataPackage;
import org.fastdownload.client.util.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

/**
 * 文件块下载的线程
 *
 * @author Administrator
 */
@Log4j2
public class FileDownloadThread extends Thread {
    private final CountDownLatch startSignal;
    private final CountDownLatch doneSignal;

    /**
     * 文件名，之后创建文件块文件时需要用到
     */
    private final String fileName;

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


    /**
     * 初始化类的数据
     *
     * @param packet 客户端传入的包，包含IP、端口等信息
     */
    public FileDownloadThread(DatagramPacket packet, String fileName, CountDownLatch startSignal, CountDownLatch doneSignal) {
        this.serverInetAddress = packet.getAddress();
        this.serverPort = packet.getPort();
        this.fileName = fileName;
        this.startSignal = startSignal;
        this.doneSignal = doneSignal;
        try {
            this.client = new DatagramSocket();
        } catch (SocketException ignored) {
        }
    }

    @Override
    public void run() {
        log.info("线程 " + Thread.currentThread().getName() + " 开始...");
        try {
            startSignal.await();
            doWork();
        } catch (InterruptedException ignored) {
        } finally {
            doneSignal.countDown();
        }
        log.info("线程 " + Thread.currentThread().getName() + " 结束");
    }

    private void doWork() {
        if (!checkConnect()) {
            log.error("连接失败!");
            return;
        }
        System.err.println("连接成功");

        try {
            // 接收线程服务端ID，方便创建临时文件名
            byte[] receiveBuffer = new byte[4192];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            client.receive(receivePacket);
            String id = new String(receivePacket.getData(), StandardCharsets.UTF_8).trim();

            System.out.println(new String(receivePacket.getData(), StandardCharsets.UTF_8) + ".temp");

            // 创建临时文件
            String tempDirectory = FileUtils.getDefaultTempDirectory();
            String tempFileName = tempDirectory + File.separator + FileUtils.getFileNameWithSuffix(fileName) + "_" + id + ".temp";
            File tempFile = new File(tempFileName.trim());

            if (!tempFile.exists()) {
                boolean newFile = tempFile.createNewFile();
                if (newFile) {
                    log.warn("文件不存在，已创建");
                } else {
                    log.info("文件不存在，文件创建失败!");
                }
            }

            // 写文件
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile));
            DataPackage dataPackage = new DataPackage();
            // 接收块大小
            byte[] receiveBuf;
            // TODO 暂时没加入重传功能
            int i = 0;
            while (true) {
                // 接收文件内容
                receiveBuf = new byte[4192];
                receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
                client.receive(receivePacket);

                // 解析下载的包
                dataPackage.getPackageData(receivePacket.getData());

                String msg = new String(dataPackage.getData(), StandardCharsets.UTF_8).trim();
                if ("SendOver".equals(msg)) {
                    break;
                }

                // MD5码校验
                boolean equals = FileUtils.md5Encode(dataPackage.getData()).trim().equals(new String(dataPackage.getCheck(), StandardCharsets.UTF_8).trim());
                System.out.println(Thread.currentThread().getName() + "发送的MD5: " + new String(dataPackage.getCheck(), StandardCharsets.UTF_8).trim());
                System.out.println(Thread.currentThread().getName() + "计算的MD5: " + FileUtils.md5Encode(dataPackage.getData()).trim());
                if (!equals) {
                    // TODO 发送错误信息
                    System.err.println("内容错误");
                }

                // 写入文件
                String s = new String(dataPackage.getLength(), StandardCharsets.UTF_8).trim();
                int len = Integer.parseInt(s);
                System.err.println("i = " + i + "\tlen = " + len);
                synchronized (this) {
                    bos.write(dataPackage.getData(), 0, len);
                    bos.flush();
                }

                // 发送OK，表示这个部分接收完毕
                byte[] sendBuffer = "OK".getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverInetAddress, serverPort);
                client.send(sendPacket);

                System.out.println();

                i++;
            }

            bos.close();

            byte[] sendBuffer = "ReceiveOver".getBytes();
            byte[] packet = dataPackage.createPacket("over".getBytes(), sendBuffer);
            DatagramPacket sendPacket = new DatagramPacket(packet, packet.length, serverInetAddress, serverPort);
            client.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查是否连接上服务端下载线程，连接成功返回true
     *
     * @return boolean
     */
    private boolean checkConnect() {
        try {
            // 发送下载请求
            byte[] sendBuffer = "FileDownloadThread".getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverInetAddress, serverPort);
            client.send(sendPacket);
            return true;
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
