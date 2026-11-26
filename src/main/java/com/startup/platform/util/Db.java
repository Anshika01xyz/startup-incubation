package com.startup.platform.util;

import com.startup.platform.config.*;

import java.sql.*;
import com.startup.platform.config.PlatformConfig;

public final class Db {
    private static final PlatformConfig CONFIG = PlatformConfig.load();

    private Db() {}
    public static Connection get() throws SQLException { return DriverManager.getConnection(CONFIG.databaseUrl(), CONFIG.databaseUser(), CONFIG.databasePassword()); }
    public static <T> T transaction(SqlWork<T> work) throws Exception {
        try (Connection c = get()) {
            c.setAutoCommit(false);
            try { T value = work.run(c); c.commit(); return value; }
            catch (Exception e) { c.rollback(); throw e; }
        }
    }
    @FunctionalInterface public interface SqlWork<T> { T run(Connection c) throws Exception; }
}
