package org.fastdownload.server.thread;


import lombok.extern.log4j.Log4j2;
import org.fastdownload.server.entity.FileBasicInfo;
import org.fastdownload.server.entity.FileInfo;
import org.fastdownload.server.service.FileInfoService;
import org.fastdownload.server.service.impl.FileInfoServiceImpl;
import org.fastdownload.server.util.DataPackage;
import org.fastdownload.server.util.FileUtils;
import org.fastdownload.server.util.JsonUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

/**
 * 接收文件上传的线程
 *
 * @author Administrator
 */
@Log4j2
public class UploadFileService extends Thread {
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
     * 初始化类的数据
     *
     * @param packet 客户端传入的包，包含IP、端口等信息
     */
    public UploadFileService(DatagramPacket packet) {
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
        doWork();
    }

    private void doWork() {
        // 1. 连接客户端
        if (!checkConnect()) {
            log.error("连接失败!");
            return;
        }
        log.info("连接成功");

        try {
            // 2. 接收客户端发送的文件基本信息
            byte[] receiveBuffer = new byte[4192];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            server.receive(receivePacket);
            String msg = new String(receivePacket.getData(), StandardCharsets.UTF_8).trim();
            FileBasicInfo fileBasicInfo = JsonUtils.toObject(msg, FileBasicInfo.class);

            // 3. 根据MD5码查看文件是否存在，存在即不需要再上传
            String md5 = fileBasicInfo.getMd5();
            FileInfoService service = new FileInfoServiceImpl();
            FileInfo byMD5 = service.findByMD5(md5);
            System.out.println(byMD5);
            byte[] sendBuffer;
            if (byMD5.getMd5() != null) {
                // 发送完不需要关闭，超时会自动关闭
                sendBuffer = "FileAlreadyExist".getBytes();
            } else {
                sendBuffer = "ReadyToDownload".getBytes();
            }
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientInetAddress, clientPort);
            server.send(sendPacket);

            // 4. 接收文件，这里只设置单线程 C:\FastDownload\Data\
            String outFileName = "C:" + File.separator + "FastDownload" + File.separator + "Data" + File.separator + fileBasicInfo.getName();

            File tempFile = new File(outFileName);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile));
            DataPackage dataPackage = new DataPackage();
            // 接收缓冲区大小
            byte[] receiveBuf;
            while (true) {
                // 接收文件内容
                receiveBuf = new byte[4192];
                receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
                server.receive(receivePacket);

                // 解析下载的包
                dataPackage.getPackageData(receivePacket.getData());

                msg = new String(dataPackage.getData(), StandardCharsets.UTF_8).trim();
                if ("SendOver".equals(msg)) {
                    break;
                }

                // MD5码校验
                boolean equals = FileUtils.md5Encode(dataPackage.getData()).trim().equals(new String(dataPackage.getCheck(), StandardCharsets.UTF_8).trim());
                if (!equals) {
                    // TODO 发送错误信息
                    System.err.println("内容错误");
                }

                // 写入文件
                String s = new String(dataPackage.getLength(), StandardCharsets.UTF_8).trim();
                int len = Integer.parseInt(s);
                synchronized (this) {
                    bos.write(dataPackage.getData(), 0, len);
                    bos.flush();
                }

                // 发送OK，表示这个部分接收完毕
                sendBuffer = "OK".getBytes();
                sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientInetAddress, clientPort);
                server.send(sendPacket);
            }

            bos.close();
            sendBuffer = "ReceiveOver".getBytes();
            sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientInetAddress, clientPort);
            server.send(sendPacket);

            // 5. 校验文件
            // 计算MD5码
            String newFileMD5 = FileUtils.getMD5(new File(outFileName));
            System.out.println(newFileMD5);
            System.out.println(md5);
            assert newFileMD5 != null;
            boolean equals = newFileMD5.equals(fileBasicInfo.getMd5());
            System.out.println(equals);

            // 6. 文件没有错误时存入数据库
            if (equals) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setMd5(newFileMD5);
                fileInfo.setName(tempFile.getName());
                fileInfo.setLocation(tempFile.getAbsolutePath());
                fileInfo.setUrl("fastdownload://" + newFileMD5 + "" + tempFile.getName());
                service.addFileInfo(fileInfo);
            }

            log.info("上传 " + fileBasicInfo.getName() + "(" + fileBasicInfo.getMd5() + ") 文件完成");
        } catch (IOException e) {
            e.printStackTrace();
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
            byte[] sendBuffer = ("UploadFileService").getBytes();
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
