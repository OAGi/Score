package org.oagi.score.gateway.http.common.lock;

import org.jooq.SQLDialect;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Testcontainers(disabledWithoutDocker = true)
class MariaDbDatabaseLockTest extends DatabaseLockTest {

    @Container
    static final MariaDBContainer MARIADB = new MariaDBContainer("mariadb:11.8");

    @Override
    SQLDialect sqlDialect() {
        return SQLDialect.MARIADB;
    }

    @Override
    Connection connect() throws SQLException {
        return DriverManager.getConnection(MARIADB.getJdbcUrl(), MARIADB.getUsername(), MARIADB.getPassword());
    }
}
