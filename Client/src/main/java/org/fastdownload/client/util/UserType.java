package org.fastdownload.client.util;

public enum UserType {
    /**
     * 游客
     */
    VISITOR("游客", 0),

    /**
     * 管理员
     */
    ADMINISTRATOR("管理员", 1),

    /**
     * 一般用户
     */
    GENERAL("一般用户", 2);

    String message;
    int code;

    UserType(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return this.message;
    }
}
