package org.fastdownload.udp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * UDP 服务类
 *
 * @author Administrator
 */
public class UDPServer {
    private final static int UDP_SERVER_PORT = 22020;
    private final static int UDP_CLIENT_PORT = 32020;
    private final static String HOST = "127.0.0.1";
    private static DatagramSocket sendSocket;
    private final static String OVER_FLAG = "over";

    public UDPServer() {
        init();
    }

    private void init() {
        try {
            sendSocket = new DatagramSocket(UDP_SERVER_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void handle(String sendFileName) {
        System.out.println("start send...");
        try {
            InputStream inputStream = new FileInputStream(new File(sendFileName));
            byte[] sendBuf = new byte[1024];
            synchronized (this) {
                while ((inputStream.read(sendBuf)) != -1) {
                    DatagramPacket sendPackage = new DatagramPacket(sendBuf, sendBuf.length, InetAddress.getByName(HOST), UDP_CLIENT_PORT);
                    sendSocket.send(sendPackage);
                }
                sendBuf = OVER_FLAG.getBytes();
                DatagramPacket sendPackage = new DatagramPacket(sendBuf, sendBuf.length, InetAddress.getByName(HOST), UDP_CLIENT_PORT);
                sendSocket.send(sendPackage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("send over!");
    }

    public static void main(String[] args) {
        String fileName = "C:\\FastDownload\\Data\\1.flv";
        UDPServer server = new UDPServer();
        server.handle(fileName);
    }
}
