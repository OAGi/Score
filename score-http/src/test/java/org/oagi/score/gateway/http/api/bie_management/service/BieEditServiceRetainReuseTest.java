package org.oagi.score.gateway.http.api.bie_management.service;

import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.RemoveReusedBIERequest;
import org.oagi.score.gateway.http.common.TestDataSource;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.oagi.score.gateway.http.common.util.ScoreDigestUtils.sha256;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.ABIE;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.ASBIE;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.ASBIEP;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.TOP_LEVEL_ASBIEP;

/**
 * Issue #157: retaining a reused BIE copies its ABIEs into the BIE that reused it under a new path, and the hash path of a
 * copy must be the hash of that new path, as it is for every other row of the copy. Against the development database (it
 * needs a work-in-progress BIE that reuses another one):
 * <pre>
 *   mvn test -Dtest=BieEditServiceRetainReuseTest -DargLine=-Dscore.retainReuse=true
 * </pre>
 * The retention runs inside a transaction that is rolled back.
 */
@EnabledIfSystemProperty(named = "score.retainReuse", matches = "true")
class BieEditServiceRetainReuseTest {

    private static TestDataSource dataSource;
    private static Connection connection;
    private static DSLContext dsl;

    @BeforeAll
    static void connect() throws SQLException {
        dataSource = TestDataSource.open(2);
        connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        dsl = DSL.using(connection, SQLDialect.MARIADB);
    }

    @AfterAll
    static void disconnect() throws SQLException {
        connection.rollback();
        connection.close();
        dataSource.close();
    }

    @AfterEach
    void discardChanges() throws SQLException {
        connection.rollback();
    }

    @Test
    void theRetainedAbiesAreFoundByTheHashOfTheirNewPath() {
        // an ASBIE of a work-in-progress BIE whose target ASBIEP belongs to another BIE, i.e. a reuse
        Record3<org.jooq.types.ULong, String, org.jooq.types.ULong> reuse = dsl
                .select(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID, ASBIE.HASH_PATH, TOP_LEVEL_ASBIEP.OWNER_USER_ID)
                .from(ASBIE)
                .join(ASBIEP).on(ASBIEP.ASBIEP_ID.eq(ASBIE.TO_ASBIEP_ID))
                .join(TOP_LEVEL_ASBIEP).on(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID))
                .where(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.ne(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID))
                .and(TOP_LEVEL_ASBIEP.STATE.eq("WIP"))
                .limit(1)
                .fetchOne();
        assertNotNull(reuse, "the database has no work-in-progress BIE that reuses another BIE");

        BigInteger host = reuse.value1().toBigInteger();
        UserId ownerId = new UserId(reuse.value3().toBigInteger());
        ScoreUser owner = new ScoreUser(ownerId, "owner", "Owner", "owner@example.com", true, List.of(ScoreRole.END_USER));
        String asbiePath = dsl.select(ASBIE.PATH).from(ASBIE).where(ASBIE.HASH_PATH.eq(reuse.value2())
                .and(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(reuse.value1()))).fetchOne(ASBIE.PATH);

        SessionService sessionService = mock(SessionService.class);
        when(sessionService.getScoreUserByUserId(ownerId)).thenReturn(owner);
        BieEditService service = new BieEditService();
        ReflectionTestUtils.setField(service, "dslContext", dsl);
        ReflectionTestUtils.setField(service, "repositoryFactory", new RepositoryFactory(dsl));
        ReflectionTestUtils.setField(service, "sessionService", sessionService);

        RemoveReusedBIERequest request = new RemoveReusedBIERequest();
        request.setTopLevelAsbiepId(new TopLevelAsbiepId(host));
        request.setAsbieHashPath(reuse.value2());
        service.retainReuseBIE(owner, request);

        // the ABIEs that the retention copied into the host BIE are the ones under the path of the retained ASBIE
        var copies = dsl.select(ABIE.PATH, ABIE.HASH_PATH).from(ABIE)
                .where(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(reuse.value1()))
                .and(ABIE.PATH.startsWith(asbiePath + ">"))
                .fetch();
        assertFalse(copies.isEmpty(), "the retention copied no ABIE");
        copies.forEach(copy -> assertEquals(sha256(copy.value1()), copy.value2(),
                "the hash path of the retained ABIE is not the hash of its path " + copy.value1()));
    }
}
