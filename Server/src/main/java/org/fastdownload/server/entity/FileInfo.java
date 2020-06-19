package org.fastdownload.server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件信息实体类
 *
 * @author Administrator
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {
    private String md5;
    private String name;
    private String location;
    private long count;
    private String url;
}
