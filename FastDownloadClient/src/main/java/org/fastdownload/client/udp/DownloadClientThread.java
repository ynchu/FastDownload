package org.fastdownload.client.udp;

import org.fastdownload.client.gui.DownloadWindowController;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DownloadClientThread extends Thread {
    /**
     * 服务器IP信息
     */
    public final static String SERVER_HOST = "127.0.0.1";

    /**
     * 服务器端口信息
     */
    public final static int SERVER_PORT = 8888;

    /**
     * 线程是否运行
     */
    private volatile boolean isRun = true;

    private List<Thread> threadList = new ArrayList<>();

    private String fileName;
    private DownloadWindowController controller;

    public DownloadClientThread(String fileName, DownloadWindowController controller) {
        this.fileName = fileName;
        this.controller = controller;
    }

    @Override
    public void run() {
        try {
            DatagramSocket client = new DatagramSocket();

            byte[] data = "我是客户端".getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(SERVER_HOST), SERVER_PORT);
            client.send(packet);

            byte[] receiveBuffer = new byte[4192];
            DatagramPacket receivePackage = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            client.receive(receivePackage);

            String s = new String(receivePackage.getData(), StandardCharsets.UTF_8);
            if ("我是服务器线程".equals(s.trim())) {
                System.out.println("连接成功");
                Thread thread = new Thread(new ClientThread(receivePackage, fileName, controller));
                threadList.add(thread);
                thread.start();
            } else {
                System.err.println("无效连接");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        for (Thread t : threadList) {
            t.stop();
//            if (!t.isInterrupted()) {
//                boolean interrupted = interrupted();
//                if (!interrupted) {
//                    System.err.println(t.getName() + "终止失败!!!");
//                }
//            }
        }
        threadList.clear();
        this.stop();
//        this.interrupt();
    }
}
