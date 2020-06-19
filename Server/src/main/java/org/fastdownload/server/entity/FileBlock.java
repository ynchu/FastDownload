package org.fastdownload.server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.RandomAccessFile;

/**
 * 文件块
 *
 * @author Administrator
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileBlock extends Thread {
    /**
     * 定义读取的起始点
     */
    private long start;

    /**
     * 定义读取的结束点
     */
    private long end;

    /**
     * 将读取到的字节输出到raf中  randomAccessFile可以理解为文件流，即文件中提取指定的一部分的包装对象
     */
    private RandomAccessFile raf;
}
