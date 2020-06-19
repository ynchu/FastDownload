package org.fastdownload.client.utils;


import java.nio.charset.StandardCharsets;

/**
 * UDP数据包
 *
 * @author Administrator
 */
public class DataPackage {
    private final static int SEQUENCE_MAX_LENGTH = 16;
    private final static int DATA_LENGTH_MAX_LENGTH = 16;
    private final static int DATA_MAX_LENGTH = 4096;
    private final static int CHECK_MAX_LENGTH = 64;
    private final static int PACKAGE_LENGTH = 4192;

    /**
     * 顺序号，最大长度16
     */
    private byte[] sequence = new byte[SEQUENCE_MAX_LENGTH];

    /**
     * 数据长度，最大长度16
     */
    private byte[] length = new byte[DATA_LENGTH_MAX_LENGTH];

    /**
     * 数据，最大长度4096
     */
    private byte[] data = new byte[DATA_MAX_LENGTH];

    /**
     * 校验，最大长度64
     */
    private byte[] check = new byte[CHECK_MAX_LENGTH];

    /**
     * byte数组UDP包，长度为4192位
     */
    private byte[] result = new byte[PACKAGE_LENGTH];

    /**
     * 初始化数据
     */
    private void init() {
        sequence = new byte[SEQUENCE_MAX_LENGTH];
        length = new byte[DATA_LENGTH_MAX_LENGTH];
        data = new byte[DATA_MAX_LENGTH];
        check = new byte[CHECK_MAX_LENGTH];
        result = new byte[PACKAGE_LENGTH];
    }

    /**
     * 制作自己的UDP数据包，格式为:<br>
     * 顺序号，长度16<br>
     * 数据长度，长度16<br>
     * 数据，长度4096<br>
     * 校验，长度64
     *
     * @param sequence 序号，长度不超过16位
     * @param data     数据，长度不超过4096位
     * @return byte[] byte数组UDP包
     */
    public byte[] createPacket(byte[] sequence, byte[] data) {
        if (sequence.length > SEQUENCE_MAX_LENGTH) {
            System.err.println("sequence 字段长度大于" + SEQUENCE_MAX_LENGTH);
            return null;
        }
        if (data.length > DATA_MAX_LENGTH) {
            System.err.println("data 字段长度大于" + DATA_MAX_LENGTH);
            return null;
        }

        init();

        // 将 序号 填入数据包的 前16 个字符中
        System.arraycopy(sequence, 0, this.sequence, 0, sequence.length);
        System.arraycopy(this.sequence, 0, this.result, 0, this.sequence.length);

        byte[] temp = (data.length + "").getBytes();

        // 将 数据长度 填入数据包的 17-32 字符处
        System.arraycopy(temp, 0, this.length, 0, temp.length);
        System.arraycopy(this.length, 0, this.result, SEQUENCE_MAX_LENGTH, length.length);

        // 将 数据 填入数据包的 33-4128 字符处
        System.arraycopy(data, 0, this.data, 0, data.length);
        System.arraycopy(this.data, 0, this.result, SEQUENCE_MAX_LENGTH + DATA_LENGTH_MAX_LENGTH, this.data.length);

        // 将 校验 填入数据包的 4229-4192 字符处
        temp = FileUtils.md5Encode(this.data).getBytes();
        System.arraycopy(temp, 0, this.check, 0, temp.length);
        System.arraycopy(this.check, 0, this.result, SEQUENCE_MAX_LENGTH + DATA_LENGTH_MAX_LENGTH + DATA_MAX_LENGTH, this.check.length);

        return this.result;
    }

    /**
     * 分析byte数组UDP包的数据
     *
     * @param result byte数组UDP包
     */
    public void getPackageData(byte[] result) {
        if (result.length != PACKAGE_LENGTH) {
            System.err.println("UDP包 长度不是" + PACKAGE_LENGTH);
            return;
        }

        init();

        // 将整个UDP包数据存储到 RESULT 字段
        System.arraycopy(result, 0, this.result, 0, PACKAGE_LENGTH);

        // 拆解UDP包数据，将 顺序号 存储到 sequence 字段
        System.arraycopy(this.result, 0, this.sequence, 0, SEQUENCE_MAX_LENGTH);

        // 拆解UDP包数据，将 数据长度 存储到 length 字段
        System.arraycopy(this.result, SEQUENCE_MAX_LENGTH, this.length, 0, DATA_LENGTH_MAX_LENGTH);

        // 拆解UDP包数据，将 数据 存储到 data 字段
        System.arraycopy(this.result, SEQUENCE_MAX_LENGTH + DATA_LENGTH_MAX_LENGTH, this.data, 0, DATA_MAX_LENGTH);

        // 拆解UDP包数据，将 校验 存储到 check 字段
        System.arraycopy(this.result, SEQUENCE_MAX_LENGTH + DATA_LENGTH_MAX_LENGTH + DATA_MAX_LENGTH, this.check, 0, CHECK_MAX_LENGTH - 1);
    }

    public byte[] getSequence() {
        return new String(this.sequence, StandardCharsets.UTF_8).trim().getBytes();
    }

    public byte[] getLength() {
        return new String(this.length, StandardCharsets.UTF_8).trim().getBytes();
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getCheck() {
        return new String(this.check, StandardCharsets.UTF_8).trim().getBytes();
    }

    public byte[] getResult() {
        return result;
    }
}
