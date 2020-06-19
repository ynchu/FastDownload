package org.fastdownload.utils;

import java.io.File;
import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        String fileName1 = "C:\\FastDownload\\Data\\1.flv";
        String fileName2 = "D:\\Users\\Administrator\\Documents\\FastDownload\\Data\\1.flv";

//        String fileName1 = "C:\\FastDownload\\Data\\1.png";
//        String fileName2 = "D:\\Users\\Administrator\\Documents\\FastDownload\\Data\\1.png";

//        String fileName1 = "F:\\COMN-master\\1.png";
//        String fileName2 = "F:\\COMN-master\\2.png";

        String md51 = FileUtils.getMD5(new File(fileName1));
        System.out.println(md51);

        String md52 = FileUtils.getMD5(new File(fileName2));
        System.out.println(md52);

        assert md51 != null;
        System.out.println(md51.equals(md52));


//        Logger log = LoggerFactory.getLogger(Test.class);
//        log.warn("this is message {}", 1);
//        Exception ex = new Exception("this is a message.");
//        log.error("a new exeception", ex);
//        log.trace("trace message.");
//        log.info("info message.");
//        for (int i = 0; i < 120; i++) {
//            log.debug("debug message:{}={}", "line", i);
//        }
    }
}
