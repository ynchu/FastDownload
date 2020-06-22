package org.fastdownload.server;

import lombok.extern.log4j.Log4j2;
import org.fastdownload.server.thread.CheckLoginThread;
import org.fastdownload.server.thread.SendFileService;
import org.fastdownload.server.thread.UploadFileService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务端启动程序
 *
 * @author Administrator
 */
@Log4j2
public class Server {
    private final static int PORT = 8888;
    private final static int MAX_CLIENT_COUNT = 20;

    private static List<SendFileService> downloadList = new ArrayList<>();

    public static void main(String[] args) {
        DatagramSocket server = null;
        try {
            server = new DatagramSocket(PORT);
        } catch (SocketException e) {
            log.error("服务器启动失败, " + e.getMessage());
            return;
        }

        int count = 0;
        while (true) {
            try {
                log.warn("等待客户端连接...");
                byte[] data = new byte[4192];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                server.receive(packet);

                String s = new String(packet.getData(), StandardCharsets.UTF_8).trim();
                if ("DownloadRequest".equals(s.trim())) {
                    log.info("客户端连接");

                    for (int i = 0; i < downloadList.size(); i++) {
                        if (downloadList.get(i).isRun()) {
                            downloadList.remove(i);
                            count--;
                            i--;
                        }
                    }

                    // 超出两个不接收
                    if (downloadList.size() >= MAX_CLIENT_COUNT) {
                        data = "连接超出最大连接数".getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
                        server.send(sendPacket);
                        continue;
                    }

                    // 创建下载线程
                    SendFileService thread = new SendFileService(packet);
                    downloadList.add(thread);
                    thread.start();
                } else if ("LoginRequest".equals(s)) {
                    // 客户端发出登录请求，开启校验登录线程并执行
                    log.info("客户端登录校验");
                    CheckLoginThread thread = new CheckLoginThread(packet);
                    thread.start();
                } else if ("UploadRequest".equals(s)) {
                    // TODO 客户端发出上传文件请求，开启上传文件线程
                    log.info("客户端发出上传文件请求");
                    UploadFileService thread = new UploadFileService(packet);
                    thread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
