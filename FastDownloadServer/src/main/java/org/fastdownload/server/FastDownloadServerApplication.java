package org.fastdownload.server;

import org.fastdownload.udp.ServerThread;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

/**
 * 主程序
 *
 * @author Administrator
 */
public class FastDownloadServerApplication {

    public static void main(String[] args) {
        try {
            DatagramSocket server = new DatagramSocket(8888);
            System.out.println("等待客户端连接...");
            int count = 0;
            while (true) {
                byte[] data = new byte[4192];
                DatagramPacket packet = new DatagramPacket(data, data.length);//这里不清楚DatagramPacket和data的关系，为什么每次都///要创建新的byte？
                server.receive(packet);

                count++;
                System.out.println("序号为： " + count);

                String s = new String(packet.getData(), StandardCharsets.UTF_8);
                if ("我是客户端".equals(s.trim())) {
                    System.out.println("客户端连接");
                    Thread thread = new Thread(new ServerThread(packet));
                    thread.start();
                } else {
                    System.err.println("无效连接");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

