package org.fastdownload.server;

import lombok.extern.log4j.Log4j2;
import org.fastdownload.server.thread.CheckLoginThread;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

/**
 * 服务端启动程序
 *
 * @author Administrator
 */
@Log4j2
public class Server {
    private final static int PORT = 8888;
    private final static int MAX_CLIENT_COUNT = 20;

    public static void main(String[] args) {
        DatagramSocket server = null;
        try {
            server = new DatagramSocket(PORT);
        } catch (SocketException e) {
            log.error("服务器启动失败, " + e.getMessage());
            return;
        }

        int count = 0;
        while (count < MAX_CLIENT_COUNT) {
            try {
                log.warn("等待客户端连接...");
                byte[] data = new byte[4192];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                server.receive(packet);

                String s = new String(packet.getData(), StandardCharsets.UTF_8).trim();
                if ("我是客户端".equals(s.trim())) {
                    log.info("客户端连接");
                    count++;
                    log.info("第 " + count + " 个客户端连接");
                    // TODO 每个下载添加一个线程
//                    Thread thread = new Thread(new ServerThread(packet));
//                    thread.start();
                } else if ("LoginRequest".equals(s)) {
                    // 客户端发出登录请求，开启校验登录线程并执行
                    log.info("客户端登录校验");
                    Thread thread = new CheckLoginThread(packet);
                    thread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
