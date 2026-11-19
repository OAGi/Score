package org.oagi.score.gateway.http.common.lock;

import org.jooq.SQLDialect;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Testcontainers(disabledWithoutDocker = true)
class MySqlDatabaseLockTest extends DatabaseLockTest {

    /** The server restarts once when it is made, so it is ready the second time that it says so. */
    @Container
    static final GenericContainer<?> MYSQL = new GenericContainer<>("mysql:8.4")
            .withEnv("MYSQL_ROOT_PASSWORD", "root").withEnv("MYSQL_DATABASE", "test").withExposedPorts(3306)
            .waitingFor(Wait.forLogMessage(".*ready for connections.*", 2));

    @Override
    SQLDialect sqlDialect() {
        return SQLDialect.MYSQL;
    }

    @Override
    Connection connect() throws SQLException {
        String url = "jdbc:mariadb://" + MYSQL.getHost() + ":" + MYSQL.getMappedPort(3306) + "/test?allowPublicKeyRetrieval=true";
        // The server says that it is ready, and for a while after it still closes the connections.
        long until = System.nanoTime() + java.time.Duration.ofSeconds(60).toNanos();
        while (true) {
            try {
                return DriverManager.getConnection(url, "root", "root");
            } catch (SQLException e) {
                if (System.nanoTime() > until) {
                    throw e;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
    }
}
