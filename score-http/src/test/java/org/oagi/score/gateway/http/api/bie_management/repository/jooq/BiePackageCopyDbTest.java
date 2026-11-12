package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.common.TestDataSource;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Copying a BIE package against the real schema. The name, version ID and version name of a package are unique together,
 * so a copy that kept the name of its source was refused by the database. Everything is rolled back.
 */
@EnabledIfSystemProperty(named = "score.biePackageCopy", matches = "true")
class BiePackageCopyDbTest {

    private static TestDataSource dataSource;
    private static Connection connection;
    private static DSLContext dsl;
    private static RepositoryFactory factory;

    private ScoreUser requester;

    @BeforeAll
    static void connect() throws SQLException {
        dataSource = TestDataSource.open(2);
        connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        dsl = DSL.using(connection, SQLDialect.MARIADB);
        factory = new RepositoryFactory(dsl);
    }

    @AfterAll
    static void disconnect() throws SQLException {
        connection.rollback();
        connection.close();
        dataSource.close();
    }

    @BeforeEach
    void arrange() {
        BigInteger userId = dsl.fetchOne("select min(app_user_id) from app_user where is_enabled = 1").get(0, BigInteger.class);
        requester = new ScoreUser(new UserId(userId), "test", "test", "test@example.com", true, List.of(ScoreRole.ADMINISTRATOR));
    }

    @AfterEach
    void discardChanges() throws SQLException {
        connection.rollback();
    }

    private BiePackageId createPackage(String name) {
        BigInteger libraryId = dsl.fetchOne("select min(library_id) from library").get(0, BigInteger.class);
        return factory.biePackageCommandRepository(requester)
                .create(new LibraryId(libraryId), name, "v1", name, "description");
    }

    private BigInteger idOf(BiePackageId id) {
        return id.value();
    }

    @Test
    void copyIsANewPackageNamedAfterItsSource() {
        BiePackageId source = createPackage("Copy test source");

        BiePackageId copy = factory.biePackageCommandRepository(requester).copy(source);

        assertNotEquals(source, copy);
        var row = dsl.fetchOne("select name, version_id, state, prev_bie_package_id, source_bie_package_id, source_action"
                + " from bie_package where bie_package_id = ?", idOf(copy));
        assertEquals("Copy of Copy test source", row.get("name"));
        assertEquals("v1", row.get("version_id"));
        assertEquals("WIP", row.get("state"));
        assertNull(row.get("prev_bie_package_id"));
        assertEquals(idOf(source), row.get("source_bie_package_id", BigInteger.class));
        assertEquals("Copy", row.get("source_action"));
    }

    @Test
    void copyKeepsTheBiesOfItsSource() {
        BiePackageId source = createPackage("Copy test source with BIEs");
        var tops = dsl.fetch("select top_level_asbiep_id from top_level_asbiep limit 2");
        if (tops.isEmpty()) {
            return; // nothing to put in the package in this database
        }
        for (var top : tops) {
            dsl.execute("insert into bie_package_top_level_asbiep (bie_package_id, top_level_asbiep_id, created_by, creation_timestamp)"
                    + " values (?, ?, ?, now(6))", idOf(source), top.get(0), requester.userId().value());
        }

        BiePackageId copy = factory.biePackageCommandRepository(requester).copy(source);

        assertEquals(tops.size(), dsl.fetchCount(DSL.table("bie_package_top_level_asbiep"),
                DSL.field("bie_package_id").eq(idOf(copy))));
    }
}
