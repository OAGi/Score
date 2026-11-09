package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.TestDataSource;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A Data Type created from a base Data Type points, through its allowed primitives, to the manifests of the release it is
 * created in, not to those of the release the base belongs to (a release the library depends on). Against the development
 * database, in a transaction that is rolled back:
 * <pre>
 *   mvn test -Dtest=DtCreateAllowedPrimitiveReleaseTest -DargLine=-Dscore.dtAwdPri=true
 * </pre>
 * The data it needs: a Data Type of one release whose allowed primitives use XBTs that another release has manifests for
 * (the Data Type Catalogue and an Ag eStandards / Chem eStandards library), and a Data Type that uses an XBT the other
 * release has no manifest for (a custom XBT, which only the connectSpec library has).
 */
@EnabledIfSystemProperty(named = "score.dtAwdPri", matches = "true")
class DtCreateAllowedPrimitiveReleaseTest {

    private static final ScoreUser REQUESTER = new ScoreUser(new UserId(BigInteger.ZERO), "sysadm", "sysadm",
            "sysadm@example.com", true, List.of(ScoreRole.ADMINISTRATOR));

    private static TestDataSource dataSource;
    private Connection connection;
    private DSLContext dsl;
    private RepositoryFactory factory;

    @BeforeAll
    static void open() {
        dataSource = TestDataSource.open(2);
    }

    @AfterAll
    static void close() {
        dataSource.close();
    }

    @BeforeEach
    void begin() throws SQLException {
        connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        dsl = DSL.using(connection, SQLDialect.MARIADB);
        factory = new RepositoryFactory(dsl);
    }

    @AfterEach
    void rollback() throws SQLException {
        connection.rollback();
        connection.close();
    }

    /** The release a library starts with: its only release that no other release follows. */
    private long workingReleaseOf(String libraryName) {
        return dsl.fetchOne("select r.release_id from `release` r join library l using (library_id) "
                + "where l.name = ? and r.release_num = 'Working'", libraryName).get(0, Long.class);
    }

    private long create(long releaseId, long basedDtManifestId) {
        return factory.dtCommandRepository(REQUESTER).create(
                new ReleaseId(BigInteger.valueOf(releaseId)),
                new DtManifestId(BigInteger.valueOf(basedDtManifestId))).value().longValue();
    }

    /**
     * The rule: the release of an allowed primitive (of the Data Type or of its supplementary components) is the release
     * of the XBT, Code List and Agency ID List manifest it points to. How many of them break it.
     */
    private long releaseMismatches(long releaseId, long dtManifestId) {
        String sql = "select count(*) from %s p join %s m on m.%s = p.%s and m.release_id = p.release_id %s "
                + "left join xbt_manifest x on x.xbt_manifest_id = p.xbt_manifest_id "
                + "left join code_list_manifest c on c.code_list_manifest_id = p.code_list_manifest_id "
                + "left join agency_id_list_manifest a on a.agency_id_list_manifest_id = p.agency_id_list_manifest_id "
                + "where p.release_id = ? and %s and (x.release_id <> p.release_id or c.release_id <> p.release_id "
                + "or a.release_id <> p.release_id)";
        long dt = dsl.fetchOne(String.format(sql, "dt_awd_pri", "dt_manifest", "dt_id", "dt_id", "",
                "m.dt_manifest_id = " + dtManifestId), releaseId).get(0, Long.class);
        long sc = dsl.fetchOne(String.format(sql, "dt_sc_awd_pri", "dt_sc_manifest", "dt_sc_id", "dt_sc_id", "",
                "m.owner_dt_manifest_id = " + dtManifestId), releaseId).get(0, Long.class);
        return dt + sc;
    }

    private List<Record> allowedPrimitives(long releaseId, long dtManifestId) {
        return dsl.fetch("select x.release_id xbt_release, x.xbt_id, p.cdt_pri_id, p.is_default "
                + "from dt_awd_pri p join dt_manifest m on m.dt_id = p.dt_id and m.release_id = p.release_id "
                + "left join xbt_manifest x on x.xbt_manifest_id = p.xbt_manifest_id "
                + "where p.release_id = ? and m.dt_manifest_id = ? order by 2, 3, 4", releaseId, dtManifestId);
    }

    @Test
    void theXbtOfTheBaseIsReplacedWithTheXbtOfTheReleaseOfTheNewDataType() {
        long working = workingReleaseOf("Ag eStandards / Chem eStandards");
        long baseManifest = dsl.fetchOne("select m.dt_manifest_id from dt_manifest m "
                + "join `release` r using (release_id) join library l using (library_id) "
                + "where l.name = 'CCTS Data Type Catalogue v3' and r.release_num = '3.1' and m.den = 'Code. Type'")
                .get(0, Long.class);
        long baseRelease = dsl.fetchOne("select release_id from dt_manifest where dt_manifest_id = ?", baseManifest)
                .get(0, Long.class);
        List<Record> base = allowedPrimitives(baseRelease, baseManifest);
        assertFalse(base.isEmpty(), "the base has no allowed primitive");

        long created = create(working, baseManifest);
        List<Record> actual = allowedPrimitives(working, created);

        assertEquals(base.size(), actual.size(), "no allowed primitive is dropped or added");
        for (int i = 0; i < base.size(); i++) {
            assertEquals(base.get(i).get("xbt_id", Long.class), actual.get(i).get("xbt_id", Long.class), "same XBT");
            assertEquals(base.get(i).get("cdt_pri_id", Long.class), actual.get(i).get("cdt_pri_id", Long.class));
            assertEquals(base.get(i).get("is_default", Integer.class), actual.get(i).get("is_default", Integer.class));
            assertEquals(working, actual.get(i).get("xbt_release", Long.class),
                    "the XBT manifest belongs to the release of the new Data Type");
        }
        assertEquals(0, releaseMismatches(working, created),
                "every XBT, Code List and Agency ID List manifest points to the release of the allowed primitive");
    }

    @Test
    void theCreationFailsWithTheReasonWhenTheReleaseOfTheNewDataTypeHasNoManifestOfAnXbtOfTheBase() {
        long working = workingReleaseOf("Ag eStandards / Chem eStandards");
        // a Data Type of another library that uses an XBT the Ag eStandards / Chem eStandards library has no manifest for
        Record base = dsl.fetchOne("select m.dt_manifest_id, x.name from dt_manifest m "
                + "join dt_awd_pri p on p.dt_id = m.dt_id and p.release_id = m.release_id "
                + "join xbt_manifest bx on bx.xbt_manifest_id = p.xbt_manifest_id join xbt x on x.xbt_id = bx.xbt_id "
                + "where not exists (select 1 from xbt_manifest t where t.release_id = ? and t.xbt_id = bx.xbt_id) "
                + "limit 1", working);
        assertTrue(base != null, "no Data Type uses an XBT that the library has no manifest for");
        long baseManifest = base.get(0, Long.class);
        String xbtName = base.get(1, String.class);
        long dtsBefore = dsl.fetchOne("select count(*) from dt").get(0, Long.class);
        long awdPrisBefore = dsl.fetchOne("select count(*) from dt_awd_pri").get(0, Long.class);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> create(working, baseManifest));

        assertTrue(e.getMessage().contains("'" + xbtName + "'"), "names the XBT: " + e.getMessage());
        assertTrue(e.getMessage().contains("Working"), "names the release: " + e.getMessage());
        assertTrue(e.getMessage().contains("no manifest"), "says what is missing: " + e.getMessage());
        assertEquals(dtsBefore, dsl.fetchOne("select count(*) from dt").get(0, Long.class), "no Data Type is created");
        assertEquals(awdPrisBefore, dsl.fetchOne("select count(*) from dt_awd_pri").get(0, Long.class),
                "no allowed primitive is created");
    }

    @Test
    void theCreationFailsWithTheReasonWhenTheReleaseOfTheNewDataTypeHasNoManifestOfACodeListOfTheBase() {
        long working = workingReleaseOf("Ag eStandards / Chem eStandards");
        Record base = dsl.fetchOne("select m.dt_manifest_id, l.name from dt_manifest m "
                + "join dt_awd_pri p on p.dt_id = m.dt_id and p.release_id = m.release_id "
                + "join code_list_manifest bc on bc.code_list_manifest_id = p.code_list_manifest_id "
                + "join code_list l on l.code_list_id = bc.code_list_id "
                + "where not exists (select 1 from code_list_manifest t where t.release_id = ? and t.code_list_id = bc.code_list_id) "
                + "and not exists (select 1 from dt_awd_pri q join xbt_manifest qx on qx.xbt_manifest_id = q.xbt_manifest_id "
                + "where q.dt_id = m.dt_id and q.release_id = m.release_id and not exists "
                + "(select 1 from xbt_manifest t where t.release_id = ? and t.xbt_id = qx.xbt_id)) "
                + "limit 1", working, working);
        assertTrue(base != null, "no Data Type uses a Code List that the library has no manifest for");
        long dtsBefore = dsl.fetchOne("select count(*) from dt").get(0, Long.class);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> create(working, base.get(0, Long.class)));

        assertTrue(e.getMessage().contains("Code List '" + base.get(1, String.class) + "'"), e.getMessage());
        assertEquals(dtsBefore, dsl.fetchOne("select count(*) from dt").get(0, Long.class), "no Data Type is created");
    }

    @Test
    void revisingADataTypeWhoseAllowedPrimitivesPointToAnotherReleaseMakesThemPointToItsOwnRelease() {
        long working = workingReleaseOf("Ag eStandards / Chem eStandards");
        // Data Types created before the allowed primitives were pointed to their own release have XBTs of another release:
        // make one, by pointing an allowed primitive to the manifest of the same XBT in the release the library depends on.
        Record dt = dsl.fetchOne("select m.dt_manifest_id, p.dt_awd_pri_id, other.xbt_manifest_id "
                + "from dt_manifest m join dt_awd_pri p on p.dt_id = m.dt_id and p.release_id = m.release_id "
                + "join xbt_manifest x on x.xbt_manifest_id = p.xbt_manifest_id "
                + "join xbt_manifest other on other.xbt_id = x.xbt_id and other.release_id <> m.release_id "
                + "where m.release_id = ? limit 1", working);
        assertTrue(dt != null, "no Data Type of the library uses an XBT that another release has a manifest of");
        dsl.execute("update dt_awd_pri set xbt_manifest_id = ? where dt_awd_pri_id = ?",
                dt.get(2, Long.class), dt.get(1, Long.class));
        long dtManifest = dt.get(0, Long.class);
        assertTrue(releaseMismatches(working, dtManifest) > 0, "the Data Type breaks the rule before it is revised");

        factory.dtCommandRepository(REQUESTER).revise(new DtManifestId(BigInteger.valueOf(dtManifest)));

        assertEquals(0, releaseMismatches(working, dtManifest));
    }

    /**
     * The records an allowed primitive of a Data Type (or of a supplementary component) stands for, in a form that does not
     * depend on the release: the same XBT, Code List and Agency ID List records, the same primitive, the same default.
     */
    private List<String> allowedPrimitiveRecords(long releaseId, long dtManifestId, boolean ofSupplementaryComponents) {
        String records = "concat_ws('|', coalesce(x.xbt_id, ''), coalesce(c.code_list_id, ''), "
                + "coalesce(a.agency_id_list_id, ''), coalesce(p.cdt_pri_id, ''), p.is_default)";
        String joins = "left join xbt_manifest x on x.xbt_manifest_id = p.xbt_manifest_id "
                + "left join code_list_manifest c on c.code_list_manifest_id = p.code_list_manifest_id "
                + "left join agency_id_list_manifest a on a.agency_id_list_manifest_id = p.agency_id_list_manifest_id ";
        String sql = ofSupplementaryComponents
                ? "select concat(sc.property_term, '|', sc.representation_term, '|', " + records + ") from dt_sc_awd_pri p "
                + "join dt_sc_manifest m on m.dt_sc_id = p.dt_sc_id and m.release_id = p.release_id "
                + "join dt_sc sc on sc.dt_sc_id = m.dt_sc_id " + joins
                + "where p.release_id = ? and m.owner_dt_manifest_id = ? order by 1"
                : "select " + records + " from dt_awd_pri p "
                + "join dt_manifest m on m.dt_id = p.dt_id and m.release_id = p.release_id " + joins
                + "where p.release_id = ? and m.dt_manifest_id = ? order by 1";
        return dsl.fetch(sql, releaseId, dtManifestId).getValues(0, String.class);
    }

    /** Allowed primitives of the supplementary components of the Data Type that point to a manifest of another release. */
    private long supplementaryComponentsOutsideRelease(long releaseId, long dtManifestId) {
        return dsl.fetchOne("select count(*) from dt_sc_manifest where owner_dt_manifest_id = ? and release_id <> ?",
                dtManifestId, releaseId).get(0, Long.class);
    }

    @Test
    void everyDataTypeOfTheDependencyReleaseCanBeTheBaseAndItsAllowedPrimitivesPointToTheCurrentRelease() {
        List<Record> bases = dsl.fetch("select m.dt_manifest_id, m.den, m.release_id from dt_manifest m "
                + "join `release` r using (release_id) join library l using (library_id) "
                + "where l.name = 'CCTS Data Type Catalogue v3' and r.release_num = '3.1' order by m.den");
        assertFalse(bases.isEmpty(), "the Data Type Catalogue has no Data Type");

        int created = 0;
        int failed = 0;
        int dtRows = 0;
        int scRows = 0;
        for (String library : List.of("Ag eStandards / Chem eStandards", "ADAPT Standard", "connectSpec")) {
            long working = workingReleaseOf(library);
            for (Record base : bases) {
                long baseManifest = base.get(0, Long.class);
                long baseRelease = base.get(2, Long.class);
                String den = base.get(1, String.class);
                String where = library + " <- " + den;
                assertTrue(baseRelease != working, "the base belongs to another release: " + where);

                long dtsBefore = dsl.fetchOne("select count(*) from dt").get(0, Long.class);
                long newDt;
                try {
                    newDt = create(working, baseManifest);
                } catch (IllegalArgumentException e) {
                    // a record the base uses has no manifest in the release: explicit failure, nothing created
                    assertTrue(e.getMessage().contains("has no manifest of"), where + ": " + e.getMessage());
                    assertEquals(dtsBefore, dsl.fetchOne("select count(*) from dt").get(0, Long.class), where);
                    failed++;
                    continue;
                }
                created++;

                // every record is the same record of the base, and its manifest belongs to the release of the new Data Type
                List<String> expectedDt = allowedPrimitiveRecords(baseRelease, baseManifest, false);
                List<String> actualDt = allowedPrimitiveRecords(working, newDt, false);
                assertEquals(expectedDt, actualDt, "dt_awd_pri of " + where);
                List<String> expectedSc = allowedPrimitiveRecords(baseRelease, baseManifest, true);
                List<String> actualSc = allowedPrimitiveRecords(working, newDt, true);
                assertEquals(expectedSc, actualSc, "dt_sc_awd_pri of " + where);
                dtRows += actualDt.size();
                scRows += actualSc.size();

                assertEquals(0, releaseMismatches(working, newDt),
                        where + ": every XBT, Code List and Agency ID List manifest is a manifest of the release");
                assertEquals(0, supplementaryComponentsOutsideRelease(working, newDt),
                        where + ": the supplementary components are created in the release of the Data Type");
                assertEquals(0, dsl.fetchOne("select count(*) from dt_awd_pri p join dt_manifest m on m.dt_id = p.dt_id "
                        + "and m.release_id = p.release_id where m.dt_manifest_id = ? and p.release_id <> ?",
                        newDt, working).get(0, Long.class), where);
            }
        }
        assertTrue(created > 0, "no Data Type could be created");
        assertTrue(dtRows > 0, "no allowed primitive of a Data Type was checked");
        assertTrue(scRows > 0, "no allowed primitive of a supplementary component was checked");
        System.out.println("created=" + created + ", failed with a missing manifest=" + failed
                + ", dt_awd_pri=" + dtRows + ", dt_sc_awd_pri=" + scRows);
    }
}
