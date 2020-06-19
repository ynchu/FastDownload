package org.fastdownload.client.service;

import lombok.extern.log4j.Log4j2;
import org.fastdownload.client.entity.User;
import org.fastdownload.client.util.JsonUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * 校验登录
 *
 * @author Administrator
 */
@Log4j2
public class CheckLogin {
    /**
     * 服务器IP信息
     */
    public final static String SERVER_HOST = "127.0.0.1";

    /**
     * 服务器端口信息
     */
    public final static int SERVER_PORT = 8888;

    /**
     * 服务器校验登录的线程IP信息
     */
    private InetAddress serverInetAddress;

    /**
     * 服务器校验登录的线程的端口
     */
    private int serverPort;

    /**
     * 处理登录请求，登录校验成功，返回true
     *
     * @param user 用户信息
     * @return int
     */
    public int handle(User user) {
        log.info("线程 " + Thread.currentThread().getName() + " 开始...");
        try {
            DatagramSocket client = new DatagramSocket();

            byte[] data = "LoginRequest".getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(SERVER_HOST), SERVER_PORT);
            client.send(packet);

            byte[] receiveBuffer = new byte[1024];
            packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            client.receive(packet);
            change(packet);

            // 发送ID、密码等数据进行校验
            String s = JsonUtils.toJson(user);
            byte[] sendBuffer = s.trim().getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverInetAddress, serverPort);
            client.send(sendPacket);

            receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            client.receive(receivePacket);

            String returnMsg = new String(receivePacket.getData(), StandardCharsets.UTF_8).trim();

            log.info("线程 " + Thread.currentThread().getName() + "结束!");

            if ("1".equals(returnMsg.trim())) {
                return 1;
            } else if ("2".equals(returnMsg.trim())) {
                return 2;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void change(DatagramPacket packet) {
        this.serverInetAddress = packet.getAddress();
        this.serverPort = packet.getPort();
    }
}
