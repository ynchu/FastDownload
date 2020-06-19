package org.fastdownload.client.udp;

import javax.swing.filechooser.FileSystemView;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * UDP客户端
 *
 * @author Administrator
 */
public class UDPClient {
    private final static int UDP_RECEIVE_PORT = 32020;
    private final static int UDP_SEND_PORT = 22020;
    private final static String HOST = "127.0.0.1";
    private static DatagramSocket receiveSocket;
    private final static String OVER_FLAG = "over";

    public UDPClient() {
        init();
    }

    private void init() {
        try {
            receiveSocket = new DatagramSocket(UDP_RECEIVE_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void handle(String fileName) {
        System.out.println("start download\n");
        try {
            // 获取"我的文档"的路径
            FileSystemView fileSystemView = FileSystemView.getFileSystemView();
            File defaultDirectory = fileSystemView.getDefaultDirectory();
            String filePath = defaultDirectory.getAbsoluteFile() + File.separator + "FastDownload" + File.separator + "Data" + File.separator + fileName;

            ExecutorService pool = Executors.newFixedThreadPool(5);

            File file = new File(filePath);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                boolean mkdirs = parentFile.mkdirs();
                if (mkdirs) {
                    System.out.println("文件夹不存在，已创建");
                } else {
                    System.err.println("文件夹不存在，文件夹创建失败!");
                }
            }
            if (!file.exists()) {
                boolean isNewFile = file.createNewFile();
                if (isNewFile) {
                    System.out.println("文件不存在，已创建");
                } else {
                    System.err.println("文件不存在，文件创建失败!");
                }
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

            byte[] receiveBuf = new byte[1024];
            boolean isOver = false;
            while (!isOver) {
                // receive data from server
                DatagramPacket receivePackage = new DatagramPacket(receiveBuf, receiveBuf.length);
                receiveSocket.receive(receivePackage);

//                // write data to file
//                Runnable task = new WriteToFile(receivePackage, filePath);
//                pool.submit(task);

                byte[] buffer = receivePackage.getData();
                if (buffer.length != 1024) {
                    System.out.println(buffer.length);
                }
                bos.write(buffer, 0, buffer.length);
                bos.flush();

                String s = new String(receivePackage.getData(), 0, receivePackage.getLength(), StandardCharsets.UTF_8);
                if (s.equals(OVER_FLAG)) {
                    isOver = true;
                    bos.close();
                    System.out.println("is over!");
                }
            }
            System.out.println("退出循环");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String fileName = "1.flv";
        UDPClient client = new UDPClient();
        client.handle(fileName);
    }

}

class WriteToFile implements Runnable {
    private DatagramPacket datagramPacket;
    private String filePath;

    public WriteToFile(DatagramPacket datagramPacket, String filePath) {
        this.datagramPacket = datagramPacket;
        this.filePath = filePath;
    }

    @Override
    public void run() {
        try {
            File file = new File(filePath);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                boolean mkdirs = parentFile.mkdirs();
                if (mkdirs) {
                    System.out.println("文件夹不存在，已创建");
                } else {
                    System.err.println("文件夹不存在，文件夹创建失败!");
                }
            }
            if (!file.exists()) {
                boolean isNewFile = file.createNewFile();
                if (isNewFile) {
                    System.out.println("文件不存在，已创建");
                } else {
                    System.err.println("文件不存在，文件创建失败!");
                }
            }
            byte[] buffer = datagramPacket.getData();
            if (buffer.length != 1024) {
                System.out.println(buffer.length);
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(buffer, 0, buffer.length);
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
