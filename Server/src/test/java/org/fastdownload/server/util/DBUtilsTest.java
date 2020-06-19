package org.fastdownload.server.util;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DBUtilsTest {

    @Test
    void getConnection() {
        DBUtils.getConnection();
        try {
            boolean status = DBUtils.getConnectionStatus();
            assertTrue(status);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Test
    void getConnectionStatus() {
    }

    @Test
    void close() {
    }

    @Test
    void testClose() {
    }
}