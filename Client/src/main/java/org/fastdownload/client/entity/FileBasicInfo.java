package org.fastdownload.client.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送文件时的基本信息实体类
 *
 * @author Administrator
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileBasicInfo {
    /**
     * 文件名
     */
    private String name;

    /**
     * 文件大小
     */
    private long size;

    /**
     * 文件MD5码
     */
    private String md5;

    /**
     * 文件块数目
     */
    private long blockNum;
}
