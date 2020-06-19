package org.fastdownload.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class LogUtils {

    private final static String LOG_FILE_NAME = "conf" + File.separator + "log4j.properties";

    public static void main(String[] args) {
        System.out.println("err");
        Logger log = LoggerFactory.getLogger(LogUtils.class);
        log.warn("this is message {}", 1);
        Exception ex = new Exception("this is a message.");
        log.error("a new exeception", ex);
        log.trace("trace message.");
        log.info("info message.");
        for (int i = 0; i < 120; i++) {
            log.debug("debug message:{}={}", "line", i);
        }

    }


}
