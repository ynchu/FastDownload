package org.fastdownload.udp;

import java.util.concurrent.Callable;

public class SendFileTask implements Callable<Integer> {

    /**
     * 数据
     */
    private byte[] data;

    /**
     * 开始点
     */
    private long start;

    /**
     * 终止点
     */
    private long end;

    /**
     * 序号
     */
    private long sequence;

    public SendFileTask(byte[] data, long start, long end, long sequence) {
        this.data = data;
        this.start = start;
        this.end = end;
        this.sequence = sequence;
    }

    @Override
    public Integer call() throws Exception {
        for (long i = start; i <= end; i++) {

        }

        return null;
    }
}
