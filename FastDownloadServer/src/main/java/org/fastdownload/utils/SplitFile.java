package org.fastdownload.utils;

import java.io.*;

public class SplitFile {

    public void splitFile(String fileName) {
        File resFile = new File(fileName);
        if (!resFile.exists()) {
            System.err.println("文件不存在！");
            return;
        }

        try {
            // 拆分
            InputStream in = new FileInputStream(resFile);
            OutputStream out = null;

//            // 定义缓冲区为1M，当缓冲区填满时，一次性刷出成一个文件
//            byte[] buf = new byte[8192];
//            int len = -1;
//            int count = 1;
//            while ((len = in.read(buf)) != -1) {
//                out = new FileOutputStream(new File(splitDir, (count++) + ".mp3"));
//                out.write(buf, 0, len);
//
//                // 关闭流，关闭之前，强行清理缓冲区
//                out.close();
//
//                // 清理缓冲区
////            out.flush();
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
