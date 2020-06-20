package org.fastdownload.server.thread;

import org.junit.jupiter.api.Test;

import java.net.DatagramPacket;

import static org.junit.jupiter.api.Assertions.*;

class SendFileServerTest {

    @Test
    void test() {
        Thread thread = new SendFileServer();
        thread.start();
    }
}