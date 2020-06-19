package org.fastdownload.utils;

import java.util.Objects;

/**
 * 文件片段的起始位置和终止位置
 */
class FilePair {
    /**
     * 文件片段的开始点
     */
    private long startPoint;

    /**
     * 文件片段的终止位置
     */
    private long endPoint;

    public FilePair() {
    }

    public FilePair(long startPoint, long endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    public long getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(long startPoint) {
        this.startPoint = startPoint;
    }

    public long getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(long endPoint) {
        this.endPoint = endPoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilePair filePair = (FilePair) o;
        return startPoint == filePair.startPoint &&
                endPoint == filePair.endPoint;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPoint, endPoint);
    }

    @Override
    public String toString() {
        return "FilePair{" +
                "startPoint=" + startPoint +
                ", endPoint=" + endPoint +
                '}';
    }
}