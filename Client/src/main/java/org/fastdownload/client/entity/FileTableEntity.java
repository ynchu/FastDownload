package org.fastdownload.client.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FileTable
 *
 * @author Administrator
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileTableEntity {
    private String fileName;
    private long fileSize;
    private String fileState;
    private String connectTime;
}
