package org.oagi.score.gateway.http.api.business_term_management.repository.jooq;

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
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.business_term_management.model.BusinessTermId;
import org.oagi.score.gateway.http.api.business_term_management.model.BusinessTermUsageNodeRecord;
import org.oagi.score.gateway.http.api.business_term_management.model.BusinessTermUsageRecord;
import org.oagi.score.gateway.http.common.TestDataSource;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The BIEs a business term is assigned to ({@code getBusinessTermUsageList}, the data of the Where Used dialog of the business
 * term list): which BIEs, how many nodes and assignments each has, the Preferred flag and Type Code of each assignment, the
 * owner, the state and the time of the last update of each BIE, and that the most recently updated BIE comes first.
 * Against the development database, with the rows that the test needs made inside a transaction that is rolled back:
 * <pre>
 *   mvn test -Drevision=$(cat VERSION) -Dtest=JooqBusinessTermUsageDbTest -DargLine=-Dscore.businessTermUsage=true
 * </pre>
 * The development database needs three top-level BIEs (with their ASBIEP, ASCCP and release) and two users; the foreign keys
 * are not checked while the test runs, as the rows it makes stand for the nodes of a BIE and not for a whole one.
 */
@EnabledIfSystemProperty(named = "score.businessTermUsage", matches = "true")
class JooqBusinessTermUsageDbTest {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private static TestDataSource dataSource;
    private static Connection connection;
    private static DSLContext dsl;

    private long ownerA;
    private long ownerB;
    private long oldest;
    private long middle;
    private long newest;
    private long businessTerm;

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

    @BeforeEach
    void prepare() throws SQLException {
        connection.rollback();
        dsl.execute("SET FOREIGN_KEY_CHECKS = 0");

        List<Long> users = dsl.fetch("select app_user_id from app_user order by app_user_id limit 2").getValues(0, Long.class);
        List<Long> topLevels = dsl.fetch("select top_level_asbiep_id from top_level_asbiep order by top_level_asbiep_id limit 3")
                .getValues(0, Long.class);
        assumeTrue(users.size() == 2 && topLevels.size() == 3, "the database needs two users and three top-level BIEs");
        ownerA = users.get(0);
        ownerB = users.get(1);
        oldest = topLevels.get(0);
        middle = topLevels.get(1);
        newest = topLevels.get(2);

        dsl.execute("delete from asbie_bizterm");
        dsl.execute("delete from bbie_bizterm");
        dsl.execute("delete from ascc_bizterm");
        dsl.execute("delete from bcc_bizterm");
        dsl.execute("delete from bbie");
        dsl.execute("delete from asbie");
        businessTerm = insertBusinessTerm("where-used-a");

        // The most recently updated is the one made last, whatever the order of their ids.
        updateTopLevel(oldest, ownerA, "WIP", "2026-01-10 09:00:00", ownerA);
        updateTopLevel(middle, ownerB, "Production", "2026-03-05 13:45:00", ownerA);
        updateTopLevel(newest, ownerA, "QA", "2026-02-01 08:30:00", ownerB);
    }

    @AfterEach
    void discard() throws SQLException {
        connection.rollback();
        dsl.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    private BusinessTermUsageRecord usageOf(List<BusinessTermUsageRecord> usages, long topLevel) {
        return usages.stream().filter(u -> u.topLevelAsbiepId().value().longValue() == topLevel).findFirst().orElseThrow();
    }

    private List<BusinessTermUsageRecord> usages(long businessTermId) {
        var requester = new ScoreUser(new UserId(BigInteger.valueOf(ownerA)), "tester", "Tester", "tester@example.com", true,
                List.of(ScoreRole.END_USER));
        return new JooqBusinessTermQueryRepository(dsl, requester, new RepositoryFactory(dsl))
                .getBusinessTermUsageList(new BusinessTermId(BigInteger.valueOf(businessTermId)));
    }

    // ----- rows -----

    private void updateTopLevel(long topLevel, long owner, String state, String updated, long updatedBy) {
        dsl.execute("update top_level_asbiep set owner_user_id = ?, state = ?, last_update_timestamp = ?, last_updated_by = ? "
                + "where top_level_asbiep_id = ?", owner, state, updated, updatedBy, topLevel);
    }

    private long insertBusinessTerm(String name) {
        dsl.execute("insert into business_term (guid, business_term, external_ref_uri, created_by, last_updated_by, "
                + "creation_timestamp, last_update_timestamp) values (?, ?, 'http://example.com/bt', ?, ?, now(6), now(6))",
                guid(), name + "-" + UUID.randomUUID(), ownerA, ownerA);
        return one("select last_insert_id()");
    }

    private static long one(String sql) {
        return dsl.fetchOne(sql).get(0, Long.class);
    }

    private static String guid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private long insertAsbie(long topLevel, String path) {
        dsl.execute("insert into asbie (guid, based_ascc_manifest_id, path, hash_path, from_abie_id, to_asbiep_id, cardinality_min, "
                + "cardinality_max, is_nillable, is_used, created_by, last_updated_by, creation_timestamp, last_update_timestamp, "
                + "seq_key, owner_top_level_asbiep_id) values (?, 1, ?, ?, 1, 1, 0, 1, 0, 1, ?, ?, now(6), now(6), 1, ?)",
                guid(), path, hashOf(path), ownerA, ownerA, topLevel);
        return one("select last_insert_id()");
    }

    private long insertBbie(long topLevel, String path) {
        dsl.execute("insert into bbie (guid, based_bcc_manifest_id, path, hash_path, from_abie_id, to_bbiep_id, cardinality_min, "
                + "cardinality_max, is_nillable, is_null, is_used, created_by, last_updated_by, creation_timestamp, "
                + "last_update_timestamp, owner_top_level_asbiep_id) values (?, 1, ?, ?, 1, 1, 0, 1, 0, 0, 1, ?, ?, now(6), now(6), ?)",
                guid(), path, hashOf(path), ownerA, ownerA, topLevel);
        return one("select last_insert_id()");
    }

    /** The hash path of a node stands for its path; the tests only need it to differ from node to node. */
    private static String hashOf(String path) {
        return String.format("%064x", new BigInteger(1, path.getBytes()));
    }

    /** Assigns the business term to a node: the BCC/ASCC link of the term, then the assignment on the BIE node. */
    private void assignBbie(long businessTermId, long bbieId, boolean preferred, String typeCode) {
        dsl.execute("insert into bcc_bizterm (business_term_id, bcc_id, created_by, last_updated_by, creation_timestamp, "
                + "last_update_timestamp) values (?, 1, ?, ?, now(6), now(6))", businessTermId, ownerA, ownerA);
        long link = one("select last_insert_id()");
        dsl.execute("insert into bbie_bizterm (bcc_bizterm_id, bbie_id, primary_indicator, type_code, created_by, last_updated_by, "
                + "creation_timestamp, last_update_timestamp) values (?, ?, ?, ?, ?, ?, now(6), now(6))",
                link, bbieId, preferred ? 1 : 0, typeCode, ownerA, ownerA);
    }

    private void assignAsbie(long businessTermId, long asbieId, boolean preferred, String typeCode) {
        dsl.execute("insert into ascc_bizterm (business_term_id, ascc_id, created_by, last_updated_by, creation_timestamp, "
                + "last_update_timestamp) values (?, 1, ?, ?, now(6), now(6))", businessTermId, ownerA, ownerA);
        long link = one("select last_insert_id()");
        dsl.execute("insert into asbie_bizterm (ascc_bizterm_id, asbie_id, primary_indicator, type_code, created_by, last_updated_by, "
                + "creation_timestamp, last_update_timestamp) values (?, ?, ?, ?, ?, ?, now(6), now(6))",
                link, asbieId, preferred ? 1 : 0, typeCode, ownerA, ownerA);
    }

    /** Three BIEs have the term; the one that was updated last is not the one with the greatest id. */
    private void assignInThreeBies() {
        assignBbie(businessTerm, insertBbie(oldest, "ASCCP-1>ACC-1>BCC-1"), true, "Synonym");
        assignAsbie(businessTerm, insertAsbie(middle, "ASCCP-1>ACC-1>ASCC-2"), false, null);
        assignBbie(businessTerm, insertBbie(newest, "ASCCP-1>ACC-1>BCC-3"), false, "Legal");
    }

    // ----- the BIEs of the list -----

    @Test
    void aTermThatIsNotAssignedHasNoBie() {
        assignInThreeBies();

        assertTrue(usages(insertBusinessTerm("unassigned")).isEmpty());
    }

    @Test
    void eachBieThatHasTheTermIsListedOnce() {
        assignInThreeBies();
        // Two more nodes of a BIE that is listed already: it does not make a second row for the BIE.
        assignBbie(businessTerm, insertBbie(oldest, "ASCCP-1>ACC-1>BCC-9"), false, null);
        assignAsbie(businessTerm, insertAsbie(oldest, "ASCCP-1>ACC-1>ASCC-9"), false, null);

        var usages = usages(businessTerm);

        assertEquals(3, usages.size(), "the number of BIEs the dialog counts in its title");
        assertEquals(3, usages.stream().map(u -> u.topLevelAsbiepId().value()).distinct().count());
    }

    @Test
    void aBieThatOnlyHasAnotherTermIsNotListed() {
        assignInThreeBies();
        long other = insertBusinessTerm("other");
        assignBbie(other, insertBbie(oldest, "ASCCP-1>ACC-1>BCC-7"), false, null);
        assignBbie(other, insertBbie(newest, "ASCCP-1>ACC-1>BCC-8"), false, null);

        var usages = usages(other);

        assertEquals(2, usages.size());
        assertEquals(List.of(1), usageOf(usages, oldest).nodes().stream().map(n -> 1).toList());
        assertFalse(usages.stream().anyMatch(u -> u.topLevelAsbiepId().value().longValue() == middle));
    }

    // ----- the order -----

    @Test
    void theMostRecentlyUpdatedBieComesFirst() {
        assignInThreeBies();

        var order = usages(businessTerm).stream().map(u -> u.topLevelAsbiepId().value().longValue()).toList();

        // 2026-03-05 (middle), 2026-02-01 (newest), 2026-01-10 (oldest)
        assertEquals(List.of(middle, newest, oldest), order);
    }

    @Test
    void theOrderFollowsTheTimeOfTheUpdateAndNotTheNodesOrTheIds() {
        assignInThreeBies();
        updateTopLevel(oldest, ownerA, "WIP", "2026-04-01 00:00:00", ownerA);

        var order = usages(businessTerm).stream().map(u -> u.topLevelAsbiepId().value().longValue()).toList();

        assertEquals(List.of(oldest, middle, newest), order);
    }

    // ----- what a BIE shows -----

    @Test
    void theOwnerTheStateAndTheTimeOfTheLastUpdateAreThoseOfTheBie() {
        assignInThreeBies();
        var usages = usages(businessTerm);

        var first = usageOf(usages, middle);
        assertEquals(ownerB, first.owner().userId().value().longValue());
        assertEquals(BieState.Production, first.state());
        assertEquals(ownerA, first.lastUpdated().who().userId().value().longValue(), "who updated it last");
        assertEquals(LocalDateTime.of(2026, 3, 5, 13, 45), first.lastUpdated().when().toInstant().atZone(ZONE).toLocalDateTime());

        var second = usageOf(usages, newest);
        assertEquals(ownerA, second.owner().userId().value().longValue());
        assertEquals(BieState.QA, second.state());
        assertEquals(ownerB, second.lastUpdated().who().userId().value().longValue());
        assertEquals(LocalDateTime.of(2026, 2, 1, 8, 30), second.lastUpdated().when().toInstant().atZone(ZONE).toLocalDateTime());

        var third = usageOf(usages, oldest);
        assertEquals(BieState.WIP, third.state());
        assertEquals(LocalDateTime.of(2026, 1, 10, 9, 0), third.lastUpdated().when().toInstant().atZone(ZONE).toLocalDateTime());
    }

    @Test
    void theOwnerAndTheUpdaterCarryTheirLoginIds() {
        assignInThreeBies();

        var usage = usageOf(usages(businessTerm), middle);

        assertEquals(dsl.fetchOne("select login_id from app_user where app_user_id = ?", ownerB).get(0, String.class), usage.owner().loginId());
        assertEquals(dsl.fetchOne("select login_id from app_user where app_user_id = ?", ownerA).get(0, String.class),
                usage.lastUpdated().who().loginId());
    }

    // ----- the nodes and the assignments -----

    @Test
    void theNodesAreTheAsbieAndBbieThatHaveTheTerm() {
        assignInThreeBies();
        long asbie = insertAsbie(oldest, "ASCCP-1>ACC-1>ASCC-5");
        long bbie = insertBbie(oldest, "ASCCP-1>ACC-1>BCC-6");
        assignAsbie(businessTerm, asbie, false, null);
        assignBbie(businessTerm, bbie, false, null);
        // A node that does not have the term is not a node of the list.
        insertBbie(oldest, "ASCCP-1>ACC-1>BCC-unassigned");

        var nodes = usageOf(usages(businessTerm), oldest).nodes();

        assertEquals(3, nodes.size(), "the nodes of the BIE that have the term, the number of the badges of the tree");
        assertEquals(2, nodes.stream().filter(n -> n.bieType().equals("BBIE")).count());
        assertEquals(1, nodes.stream().filter(n -> n.bieType().equals("ASBIE")).count());
        assertTrue(nodes.stream().anyMatch(n -> n.bieType().equals("ASBIE") && n.bieId().longValue() == asbie));
        assertTrue(nodes.stream().anyMatch(n -> n.bieType().equals("BBIE") && n.bieId().longValue() == bbie));
    }

    @Test
    void aNodeCarriesThePathAndTheHashPathThatTheTreeUsesToFindIt() {
        long asbie = insertAsbie(middle, "ASCCP-1>ACC-1>ASCC-2");
        assignAsbie(businessTerm, asbie, false, null);

        var node = usageOf(usages(businessTerm), middle).nodes().get(0);

        assertEquals("ASCCP-1>ACC-1>ASCC-2", node.path());
        assertEquals(dsl.fetchOne("select hash_path from asbie where asbie_id = ?", asbie).get(0, String.class), node.hashPath());
    }

    @Test
    void thePreferredFlagAndTheTypeCodeOfAnAssignmentAreKept() {
        assignInThreeBies();

        var oldestNode = usageOf(usages(businessTerm), oldest).nodes().get(0);
        var middleNode = usageOf(usages(businessTerm), middle).nodes().get(0);

        assertEquals(1, oldestNode.assignments().size());
        assertTrue(oldestNode.assignments().get(0).primaryIndicator(), "Preferred");
        assertEquals("Synonym", oldestNode.assignments().get(0).typeCode());
        assertFalse(middleNode.assignments().get(0).primaryIndicator(), "not Preferred");
        assertEquals(null, middleNode.assignments().get(0).typeCode(), "no Type Code");
    }

    @Test
    void theSameTermAssignedTwiceToANodeWithDifferentTypeCodesIsOneNodeWithTwoAssignments() {
        long bbie = insertBbie(newest, "ASCCP-1>ACC-1>BCC-3");
        assignBbie(businessTerm, bbie, true, "Synonym");
        assignBbie(businessTerm, bbie, false, "Legal");

        var nodes = usageOf(usages(businessTerm), newest).nodes();

        assertEquals(1, nodes.size());
        List<BusinessTermUsageNodeRecord.Assignment> assignments = nodes.get(0).assignments();
        assertEquals(2, assignments.size());
        assertEquals(1, assignments.stream().filter(BusinessTermUsageNodeRecord.Assignment::primaryIndicator).count(),
                "one preferred assignment");
        assertEquals(List.of("Legal", "Synonym"), assignments.stream().map(BusinessTermUsageNodeRecord.Assignment::typeCode).sorted().toList());
    }
}
