package org.oagi.score.gateway.http.common.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueDetailsRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListValueDetailsRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListValueManifestId;
import org.oagi.score.gateway.http.common.TestDataSource;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Issue #1825: the {@code used} flag of the values of a Code List and of an Agency ID List is an {@code EXISTS} in the value
 * query, not up to two {@code COUNT} queries per value. Against the development database (it needs data of both kinds):
 * <pre>
 *   mvn test -Dtest=ValueUsedFlagTest -DargLine=-Dscore.valueUsed=true
 * </pre>
 * Every value of every list is compared with the rules written out as SQL, and with the previous way of deciding it: the
 * two may differ only where the rules were changed on purpose. The rules that the data does not exercise (a replacement, a
 * context scheme) are checked on rows that are changed inside a transaction, which is rolled back.
 */
@EnabledIfSystemProperty(named = "score.valueUsed", matches = "true")
class ValueUsedFlagTest {

    private static TestDataSource dataSource;
    private static Connection connection;
    private static DSLContext dsl;
    private static RepositoryFactory factory;
    private static final ScoreUser REQUESTER = new ScoreUser(new UserId(BigInteger.ZERO), "sysadm", "sysadm",
            "sysadm@example.com", true, List.of(ScoreRole.ADMINISTRATOR));

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
    @AfterEach
    void discardChanges() throws SQLException {
        connection.rollback();
    }

    private static Set<Long> ids(String sql) {
        return dsl.fetch(sql).intoSet(0, Long.class);
    }

    private static Set<Long> minus(Set<Long> a, Set<Long> b) {
        Set<Long> r = new HashSet<>(a);
        r.removeAll(b);
        return r;
    }

    // ----- Agency ID List -----

    private static final String AIL_VALUES = "select agency_id_list_value_manifest_id from agency_id_list_value_manifest";

    /** The previous way: any list or Code List that points at the value, the value's own list included. */
    private static Set<Long> ailUsedBefore() {
        Set<Long> used = ids("select agency_id_list_value_manifest_id from agency_id_list_manifest"
                + " where agency_id_list_value_manifest_id is not null");
        used.addAll(ids("select agency_id_list_value_manifest_id from code_list_manifest"
                + " where agency_id_list_value_manifest_id is not null"));
        return used;
    }

    /** The rules of the issue: a reference from another list or a Code List, or from a value based on it or replacing it. */
    private static Set<Long> ailUsedByRules() {
        Set<Long> used = ids("select l.agency_id_list_value_manifest_id from agency_id_list_manifest l"
                + " join agency_id_list_value_manifest v on v.agency_id_list_value_manifest_id = l.agency_id_list_value_manifest_id"
                + " where l.agency_id_list_manifest_id <> v.agency_id_list_manifest_id");
        used.addAll(ids("select agency_id_list_value_manifest_id from code_list_manifest"
                + " where agency_id_list_value_manifest_id is not null"));
        used.addAll(ids("select based_agency_id_list_value_manifest_id from agency_id_list_value_manifest"
                + " where based_agency_id_list_value_manifest_id is not null"));
        used.addAll(ids("select replacement_agency_id_list_value_manifest_id from agency_id_list_value_manifest"
                + " where replacement_agency_id_list_value_manifest_id is not null"));
        return used;
    }

    private static Set<Long> ailUsedByRepository() {
        var repository = factory.agencyIdListQueryRepository(REQUESTER);
        Set<Long> used = new HashSet<>();
        Set<Long> seen = new HashSet<>();
        for (Long list : ids("select agency_id_list_manifest_id from agency_id_list_manifest")) {
            for (AgencyIdListValueDetailsRecord value : repository.getAgencyIdListValueDetailsList(
                    new AgencyIdListManifestId(BigInteger.valueOf(list)))) {
                long id = value.agencyIdListValueManifestId().value().longValue();
                seen.add(id);
                if (value.used()) {
                    used.add(id);
                }
            }
        }
        assertEquals(ids(AIL_VALUES), seen, "every value is read, once");
        return used;
    }

    @Test
    void agencyIdListValuesAreUsedByTheRules() {
        assertEquals(ailUsedByRules(), ailUsedByRepository());
    }

    @Test
    void agencyIdListValuesDifferFromBeforeOnlyWhereTheRulesChanged() {
        Set<Long> before = ailUsedBefore();
        Set<Long> now = ailUsedByRepository();

        Set<Long> noLongerUsed = minus(before, now);
        Set<Long> newlyUsed = minus(now, before);

        // The only reference to the value is from its own list.
        Set<Long> ownListOnly = minus(ids("select l.agency_id_list_value_manifest_id from agency_id_list_manifest l"
                + " join agency_id_list_value_manifest v on v.agency_id_list_value_manifest_id = l.agency_id_list_value_manifest_id"
                + " where l.agency_id_list_manifest_id = v.agency_id_list_manifest_id"), ailUsedByRules());
        assertEquals(ownListOnly, noLongerUsed);   // may be empty in the data: anAgencyIdListValueIsNotUsedByItsOwnList... covers it

        // A value that something is based on or replaced by was not used before, and is now.
        Set<Long> basedOrReplaced = ids("select based_agency_id_list_value_manifest_id from agency_id_list_value_manifest"
                + " where based_agency_id_list_value_manifest_id is not null"
                + " union select replacement_agency_id_list_value_manifest_id from agency_id_list_value_manifest"
                + " where replacement_agency_id_list_value_manifest_id is not null");
        assertTrue(basedOrReplaced.containsAll(newlyUsed));
        assertTrue(minus(basedOrReplaced, before).containsAll(newlyUsed));
    }

    @Test
    void anAgencyIdListValueReplacedByAnotherValueIsUsed() {
        long replaced = firstUnusedAilValue();
        long replacing = dsl.fetchOne("select agency_id_list_value_manifest_id from agency_id_list_value_manifest"
                + " where agency_id_list_value_manifest_id <> ? limit 1", replaced).get(0, Long.class);
        assertFalse(ailValueUsed(replaced));

        dsl.execute("update agency_id_list_value_manifest set replacement_agency_id_list_value_manifest_id = ?"
                + " where agency_id_list_value_manifest_id = ?", replaced, replacing);

        assertTrue(ailValueUsed(replaced));
    }

    @Test
    void anAgencyIdListValueAnotherValueIsBasedOnIsUsed() {
        long based = firstUnusedAilValue();
        long deriving = dsl.fetchOne("select agency_id_list_value_manifest_id from agency_id_list_value_manifest"
                + " where agency_id_list_value_manifest_id <> ? limit 1", based).get(0, Long.class);

        dsl.execute("update agency_id_list_value_manifest set based_agency_id_list_value_manifest_id = ?"
                + " where agency_id_list_value_manifest_id = ?", based, deriving);

        assertTrue(ailValueUsed(based));
    }

    @Test
    void anAgencyIdListValueIsNotUsedByItsOwnListButIsByAnotherList() {
        long value = firstUnusedAilValue();
        long ownList = dsl.fetchOne("select agency_id_list_manifest_id from agency_id_list_value_manifest"
                + " where agency_id_list_value_manifest_id = ?", value).get(0, Long.class);

        dsl.execute("update agency_id_list_manifest set agency_id_list_value_manifest_id = ? where agency_id_list_manifest_id = ?",
                value, ownList);
        assertFalse(ailValueUsed(value), "the list of the value itself does not use it");

        long otherList = dsl.fetchOne("select agency_id_list_manifest_id from agency_id_list_manifest"
                + " where agency_id_list_manifest_id <> ? limit 1", ownList).get(0, Long.class);
        dsl.execute("update agency_id_list_manifest set agency_id_list_value_manifest_id = ? where agency_id_list_manifest_id = ?",
                value, otherList);
        assertTrue(ailValueUsed(value), "another list does");
    }

    private static long firstUnusedAilValue() {
        Set<Long> used = ailUsedByRepository();
        return dsl.fetch(AIL_VALUES + " order by 1").intoSet(0, Long.class).stream()
                .filter(id -> !used.contains(id)).findFirst().orElseThrow();
    }

    private static boolean ailValueUsed(long valueManifestId) {
        return factory.agencyIdListQueryRepository(REQUESTER)
                .getAgencyIdListValueDetails(new AgencyIdListValueManifestId(BigInteger.valueOf(valueManifestId))).used();
    }

    // ----- Code List -----

    private static final String CL_VALUES = "select code_list_value_manifest_id from code_list_value_manifest";

    /** The previous way: a value that another value is based on, or a value of a Code List that a context scheme names. */
    private static Set<Long> clUsedBefore() {
        Set<Long> used = ids("select based_code_list_value_manifest_id from code_list_value_manifest"
                + " where based_code_list_value_manifest_id is not null");
        used.addAll(ids("select v.code_list_value_manifest_id from ctx_scheme s"
                + " join code_list c on c.code_list_id = s.code_list_id"
                + " join code_list_manifest m on m.code_list_id = c.code_list_id"
                + " join code_list_value_manifest v on v.code_list_manifest_id = m.code_list_manifest_id"));
        return used;
    }

    /** The rules of the issue: a value that another value is based on or is replaced by. */
    private static Set<Long> clUsedByRules() {
        Set<Long> used = ids("select based_code_list_value_manifest_id from code_list_value_manifest"
                + " where based_code_list_value_manifest_id is not null");
        used.addAll(ids("select replacement_code_list_value_manifest_id from code_list_value_manifest"
                + " where replacement_code_list_value_manifest_id is not null"));
        return used;
    }

    private static Set<Long> clUsedByRepository() {
        var repository = factory.codeListQueryRepository(REQUESTER);
        Set<Long> used = new HashSet<>();
        Set<Long> seen = new HashSet<>();
        for (Long list : ids("select distinct code_list_manifest_id from code_list_value_manifest")) {
            for (CodeListValueDetailsRecord value : repository.getCodeListValueDetailsList(
                    new CodeListManifestId(BigInteger.valueOf(list)))) {
                long id = value.codeListValueManifestId().value().longValue();
                seen.add(id);
                if (value.used()) {
                    used.add(id);
                }
            }
        }
        assertEquals(ids(CL_VALUES), seen, "every value is read, once");
        return used;
    }

    @Test
    void codeListValuesAreUsedByTheRules() {
        assertEquals(clUsedByRules(), clUsedByRepository());
    }

    @Test
    void codeListValuesDifferFromBeforeOnlyWhereTheRulesChanged() {
        Set<Long> before = clUsedBefore();
        Set<Long> now = clUsedByRepository();

        Set<Long> based = ids("select based_code_list_value_manifest_id from code_list_value_manifest"
                + " where based_code_list_value_manifest_id is not null");
        Set<Long> replacing = ids("select replacement_code_list_value_manifest_id from code_list_value_manifest"
                + " where replacement_code_list_value_manifest_id is not null");

        // Only a context scheme that names the Code List made a value used before and not now; a base stays used.
        assertTrue(minus(before, now).stream().noneMatch(based::contains));
        assertTrue(now.containsAll(based));
        // Only a replacement makes a value used now and not before.
        assertTrue(replacing.containsAll(minus(now, before)));
    }

    @Test
    void aCodeListValueReplacedByAnotherValueIsUsed() {
        long replaced = firstUnusedClValue();
        long replacing = dsl.fetchOne("select code_list_value_manifest_id from code_list_value_manifest"
                + " where code_list_value_manifest_id <> ? limit 1", replaced).get(0, Long.class);
        assertFalse(clValueUsed(replaced));

        dsl.execute("update code_list_value_manifest set replacement_code_list_value_manifest_id = ?"
                + " where code_list_value_manifest_id = ?", replaced, replacing);

        assertTrue(clValueUsed(replaced));
    }

    @Test
    void aCodeListValueIsNotUsedBecauseAContextSchemeNamesItsList() {
        long value = firstUnusedClValue();
        long codeList = dsl.fetchOne("select m.code_list_id from code_list_value_manifest v"
                + " join code_list_manifest m on m.code_list_manifest_id = v.code_list_manifest_id"
                + " where v.code_list_value_manifest_id = ?", value).get(0, Long.class);

        dsl.execute("insert into ctx_scheme (guid, scheme_id, scheme_agency_id, scheme_version_id, ctx_category_id, code_list_id,"
                        + " created_by, last_updated_by, creation_timestamp, last_update_timestamp)"
                        + " select 'f1825f1825f1825f1825f1825f1825f1', 'T1825', 'T1825', '1', min(ctx_category_id), ?, 0, 0,"
                        + " current_timestamp(6), current_timestamp(6) from ctx_category",
                codeList);
        assertEquals(1, dsl.fetchOne("select count(*) from ctx_scheme where code_list_id = ?", codeList).get(0, Integer.class));

        assertFalse(clValueUsed(value));
        assertTrue(clUsedBefore().contains(value), "the previous way did count it");
    }

    private static long firstUnusedClValue() {
        Set<Long> used = clUsedByRepository();
        return dsl.fetch(CL_VALUES + " order by 1 limit 1000").intoSet(0, Long.class).stream()
                .filter(id -> !used.contains(id)).findFirst().orElseThrow();
    }

    private static boolean clValueUsed(long valueManifestId) {
        return factory.codeListQueryRepository(REQUESTER)
                .getCodeListValueDetails(new CodeListValueManifestId(BigInteger.valueOf(valueManifestId))).used();
    }
}
