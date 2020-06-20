package org.fastdownload.client.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DownloadFileServerTest {

    @Test
    void doWork() {
        DownloadFileServer server = new DownloadFileServer();
        server.doWork();
    }
}