package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class HikariBasic {

    public static void main(String[] args) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=2;");
        config.setUsername("sa");
        config.setPassword("sa");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        DataSource ds = new HikariDataSource(config);

        try(Connection conn = ds.getConnection()) {
            log.info("{}, {}", conn.getCatalog(), conn.getAutoCommit() ? "AUTOCOMMIT=TRUE" : "");
            conn.createStatement().execute(conn.nativeSQL("CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR);"));
            conn.createStatement().execute(conn.nativeSQL("INSERT INTO TEST VALUES(1, 'Hello World');\n"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try(Connection conn2 = ds.getConnection()) {
            PreparedStatement stmt = conn2.prepareStatement("SELECT * FROM TEST WHERE ID=?");
            stmt.setInt(1, 1);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                log.info(rs.getString("NAME"));
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
