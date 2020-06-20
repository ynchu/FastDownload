package org.fastdownload.client.service;

import lombok.extern.log4j.Log4j2;
import org.fastdownload.client.entity.FileBasicInfo;
import org.fastdownload.client.thread.FileDownloadThread;
import org.fastdownload.client.util.FileUtils;
import org.fastdownload.client.util.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

/**
 * 下载服务总的类
 *
 * @author Administrator
 */
@Log4j2
public class DownloadFileServer {
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

    public DownloadFileServer() {
        try {
            this.client = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void doWork() {
        if (!checkConnect()) {
            log.error("连接失败!");
            // 连接失败继续执行连接
            doWork();
        }
        try {

            // 1. 发送需要下载的文件名 TODO 之后通过参数传递进入,直接获取输入框中的数据
            String fileName = "1.txt";
            byte[] sendBuffer = fileName.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverInetAddress, serverPort);
            client.send(sendPacket);

            // 2. 接收服务端发送的文件基本信息
            byte[] receiveBuffer = new byte[4192];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            client.receive(receivePacket);
            String msg = new String(receivePacket.getData(), StandardCharsets.UTF_8).trim();
            FileBasicInfo fileBasicInfo = JsonUtils.toObject(msg, FileBasicInfo.class);

            // TODO 3， 接收服务端发送的文件内容，这里需要开线程接收
            final int N = (int) fileBasicInfo.getBlockNum();
            CountDownLatch startSignal = new CountDownLatch(1);
            CountDownLatch doneSignal = new CountDownLatch(N);

            int count = 0;
            while (count < N) {
                receivePacket.setLength(0);
                receiveBuffer = new byte[4192];
                receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                client.receive(receivePacket);
                msg = new String(receivePacket.getData(), StandardCharsets.UTF_8).trim();
                // 接收到下载标志，创建下载线程
                if ("FileSendThread".equals(msg)) {
                    count++;
                    Thread thread = new FileDownloadThread(receivePacket, fileBasicInfo.getName(), startSignal, doneSignal);
                    thread.start();
                }
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
            System.out.println();
            System.err.println("完成");
            System.out.println(fileBasicInfo.getName());
            System.out.println(fileBasicInfo.getBlockNum());
            FileUtils.mergeFile(fileBasicInfo.getName(), fileBasicInfo.getBlockNum());
            // 计算MD5码
            String md5 = FileUtils.getMD5(new File(FileUtils.getDefaultDirectory() + File.separator + fileName));
            System.out.println(md5);
            System.out.println(fileBasicInfo.getMd5());
            assert md5 != null;
            System.out.println(md5.equals(fileBasicInfo.getMd5()));

            // 删除临时文件
//            for (int i = 0; i < fileBasicInfo.getBlockNum(); i++) {
//                String filePath = FileUtils.getDefaultTempDirectory() + File.separator + FileUtils.getFileNameWithSuffix(fileName) + "_" + i + ".temp";
//                FileUtils.deleteFile(filePath);
//            }

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
            client = new DatagramSocket();
            // 发送下载请求
            byte[] sendBuffer = "DownloadRequest".getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(SERVER_HOST), SERVER_PORT);
            client.send(sendPacket);

            // 接收到服务端返回的信息
            byte[] receiveBuffer = new byte[4192];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            client.receive(receivePacket);
            String msg = new String(receivePacket.getData(), StandardCharsets.UTF_8).trim();
            if ("SendFileServer".equals(msg)) {
                change(receivePacket);
                // 接收到服务端反馈之后便
                sendBuffer = "OK".getBytes();
                sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverInetAddress, serverPort);
                client.send(sendPacket);
                return true;
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
