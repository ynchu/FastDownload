package org.fastdownload.client.entity;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 显示所用的文件表
 *
 * @author Administrator
 */
@Getter
@NoArgsConstructor
public class FileTable {
    private SimpleStringProperty fileName = new SimpleStringProperty();
    private SimpleLongProperty fileSize = new SimpleLongProperty();
    private SimpleStringProperty fileState = new SimpleStringProperty();
    private SimpleStringProperty connectTime = new SimpleStringProperty();

    public FileTable(String fileName, long fileSize, String fileState, String connectTime) {
        this.fileName.set(fileName);
        this.fileSize.set(fileSize);
        this.fileState.set(fileState);
        this.connectTime.set(connectTime);
    }

    public FileTable(FileTableEntity fileTableEntity) {
        this.fileName.set(fileTableEntity.getFileName());
        this.fileSize.set(fileTableEntity.getFileSize());
        this.fileState.set(fileTableEntity.getFileState());
        this.connectTime.set(fileTableEntity.getConnectTime());
    }

    public FileTableEntity toFileTableEntity() {
        FileTableEntity fileTableEntity = new FileTableEntity();
        fileTableEntity.setFileName(this.getFileName().getValue());
        fileTableEntity.setFileSize(this.getFileSize().getValue());
        fileTableEntity.setFileState(this.getFileState().getValue());
        fileTableEntity.setConnectTime(this.getConnectTime().getValue());
        return fileTableEntity;
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public void setFileSize(long fileSize) {
        this.fileSize.set(fileSize);
    }

    public void setFileState(String fileState) {
        this.fileState.set(fileState);
    }

    public void setConnectTime(String connectTime) {
        this.connectTime.set(connectTime);
    }
}
