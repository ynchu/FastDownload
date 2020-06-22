package org.fastdownload.server.thread;

import lombok.extern.log4j.Log4j2;
import org.fastdownload.server.entity.User;
import org.fastdownload.server.service.UserService;
import org.fastdownload.server.service.impl.UserServiceImpl;
import org.fastdownload.server.util.JsonUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

/**
 * 校验登录线程
 *
 * @author Administrator
 */
@Log4j2
public class CheckLoginThread extends Thread {
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


    public CheckLoginThread(DatagramPacket packet) {
        this.clientInetAddress = packet.getAddress();
        this.clientPort = packet.getPort();
        initServerSocket();
        try {
            this.server.setSoTimeout(60000);
        } catch (SocketException e) {
            log.warn("超时断开连接");
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
        log.info("线程 " + Thread.currentThread().getName() + " 开始...");
        try {
            byte[] sendBuffer;

            // 接收到客户端，发送接收信息
            sendBuffer = "AllowLoginRequest".getBytes();
            DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length, clientInetAddress, clientPort);
            server.send(packet);

            // 接收客户端发送的ID、密码等数据并进行校验
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            server.receive(receivePacket);

            String s = new String(receivePacket.getData(), StandardCharsets.UTF_8).trim();
            User user = JsonUtils.toObject(s, User.class);
            UserService userService = new UserServiceImpl();
            User findUser = userService.findById(user.getId());

            // 信息相同，则返回true
            if (user.equals(findUser)) {
                String msg = "" + findUser.getType();
                sendBuffer = msg.getBytes();
                log.info(msg);
            } else {
                sendBuffer = "-1".getBytes();
                log.error("-1");
            }

            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientInetAddress, clientPort);
            server.send(sendPacket);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        log.info("线程 " + Thread.currentThread().getName() + " 结束!");
    }
}
