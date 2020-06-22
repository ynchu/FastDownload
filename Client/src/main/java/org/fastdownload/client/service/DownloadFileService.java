package org.fastdownload.client.service;

import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.fastdownload.client.Client;
import org.fastdownload.client.entity.FileBasicInfo;
import org.fastdownload.client.entity.FileTable;
import org.fastdownload.client.entity.FileTableEntity;
import org.fastdownload.client.entity.User;
import org.fastdownload.client.gui.controller.DownloadWindowController;
import org.fastdownload.client.gui.controller.MainWindowController;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 下载服务总的类
 *
 * @author Administrator
 */
@Log4j2
public class DownloadFileService extends Thread {
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

    private DownloadWindowController controller;

    /**
     * 进度条控制线程
     */
    private ScheduledService<Double> service;

    /**
     * 线程是否运行完成
     */
    @Getter
    private volatile boolean isRun = false;

    private List<FileDownloadThread> threads = new ArrayList<>();

    private String fileName;

    private MainWindowController mwc = (MainWindowController) Client.controllers.get(MainWindowController.class.getName());
    private int fileTableIndex;

    public DownloadFileService(String fileName, DownloadWindowController controller) {
        this.fileName = fileName;
        try {
            this.client = new DatagramSocket();
            this.client.setSoTimeout(20000);
        } catch (SocketException e) {
            log.error(e.getMessage());
        }
        this.controller = controller;
    }

    @Override
    public void run() {
        if (!checkConnect()) {
            log.error("连接失败!");
            // 连接失败继续执行连接
        }
        log.info("连接成功");

        // 如果重传数大于5次，直接放弃
        int c = 1;
        while (!doWork()) {
            c++;
            if (c > 5) {
                break;
            }
        }
    }

    public boolean doWork() {
        try {
            // 1. 发送需要下载的文件名
            byte[] sendBuffer = fileName.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverInetAddress, serverPort);
            client.send(sendPacket);

            // 接收返回的信息
            byte[] receiveBuf = new byte[4096];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
            client.receive(receivePacket);
            String msg = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8).trim();
            System.out.println(msg);
            if ("NoSuchFile".equals(msg)) {
                System.err.println("完成");
                Platform.runLater(() -> {
                    controller.update(1, "服务端没有这个文件");
                });
                return true;
            }
            log.info("开始上传文件内容");

            // 2. 接收服务端发送的文件基本信息
            byte[] receiveBuffer = new byte[4192];
            receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            client.receive(receivePacket);
            msg = new String(receivePacket.getData(), StandardCharsets.UTF_8).trim();
            FileBasicInfo fileBasicInfo = JsonUtils.toObject(msg, FileBasicInfo.class);
            long fileLength = fileBasicInfo.getSize();

            // 3， 接收服务端发送的文件内容，这里需要开线程接收
            final int N = (int) fileBasicInfo.getBlockNum();
            CountDownLatch startSignal = new CountDownLatch(1);
            CountDownLatch doneSignal = new CountDownLatch(N);

            // 添加到主页面
            SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
            FileTable fileTable = new FileTable(fileName, fileLength, "正在下载", sdf.format(Calendar.getInstance().getTime()));
            Platform.runLater(() -> fileTableIndex = mwc.addData(fileTable));

            int count = 0;
            while (count < N) {
                receiveBuffer = new byte[4192];
                receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                client.receive(receivePacket);
                msg = new String(receivePacket.getData(), StandardCharsets.UTF_8).trim();
                // 接收到下载标志，创建下载线程
                if ("FileSendThread".equals(msg)) {
                    count++;
                    FileDownloadThread thread = new FileDownloadThread(receivePacket, fileBasicInfo.getName(), startSignal, doneSignal);
                    threads.add(thread);
                    thread.start();
                }
            }

            service = new ScheduledService<Double>() {
                @Override
                protected Task<Double> createTask() {
                    return new Task<Double>() {
                        @Override
                        protected Double call() throws Exception {
                            long curSize = 0;
                            for (FileDownloadThread t : threads) {
                                curSize += t.size;
                            }
                            return (double) curSize / fileLength;
                        }

                        @Override
                        protected void updateValue(Double value) {
                            if (value >= 1) {
                                service.cancel();
                            }
                            // TODO 修改进度条任务
                            Platform.runLater(() -> {
                                controller.update(value, "" + value);
                                fileTable.setFileState("已下载 " + String.format("%.2f", value * 100) + "%");
                                mwc.update(fileTableIndex, fileTable);
                            });
                        }
                    };
                }
            };
            service.setDelay(Duration.millis(0));
            service.setPeriod(Duration.millis(0));
            service.start();

            startSignal.countDown();
            try {
                doneSignal.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 结束时校验等
            Platform.runLater(() -> {
                fileTable.setFileState("校验中... ");
                mwc.update(fileTableIndex, fileTable);
            });

            // 校验MD5码,可能出现合并错误,所以循环几次
            String md5;
            boolean equals;
            int c = 1;
            while (true) {
                // 合并临时文件
                FileUtils.mergeFile(fileBasicInfo.getName(), fileBasicInfo.getBlockNum());
                // 计算MD5码
                md5 = FileUtils.getMD5(new File(FileUtils.getDefaultDirectory() + File.separator + fileName));
                System.out.println(md5);
                System.out.println(fileBasicInfo.getMd5());
                assert md5 != null;
                equals = md5.equals(fileBasicInfo.getMd5());
                System.out.println(equals);
                c++;
                if (c > 5 || equals) {
                    break;
                }
            }

            if (!equals) {
                return false;
            }

            boolean finalEquals = equals;
            Platform.runLater(() -> {
                if (finalEquals) {
                    fileTable.setFileState("下载完成");
                } else {
                    fileTable.setFileState("校验出错");
                }
                mwc.update(fileTableIndex, fileTable);

                // 将下载记录写入文件
                MainWindowController controller = (MainWindowController) Client.controllers.get(MainWindowController.class.getName());
                try {
                    List<FileTableEntity> list = new ArrayList<>();
                    for (FileTable ft : controller.getData()) {
                        FileTableEntity fileTableEntity = ft.toFileTableEntity();
                        list.add(fileTableEntity);
                    }
                    User user = (User) org.fastdownload.client.util.FileUtils.readObject(new File("docs/account.obj"));
                    String path = "docs/" + user.getId() + "_record.json";
                    org.apache.commons.io.FileUtils.writeStringToFile(new File(path), JsonUtils.toJson(list), "utf-8");
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });

            // 删除临时文件
            for (int i = 0; i < fileBasicInfo.getBlockNum(); i++) {
                String filePath = FileUtils.getDefaultTempDirectory() + File.separator + fileName + "_" + i + ".temp";
                boolean b = FileUtils.deleteFile(filePath);
                // 没有删除再次删除
                if (!b) {
                    i--;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        isRun = true;

        log.info("下载完成");
        return true;
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
            } else {
                return false;
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
