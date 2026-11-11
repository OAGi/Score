package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What the allowed primitives of a Data Type become in a new release, when the Code List they use is not part of the release
 * assignment but has been revised (so the release keeps the Code List of the earlier release). Against the development
 * database, in a transaction that is rolled back:
 * <pre>
 *   mvn test -Dtest=ReleaseCopyDtAwdPriDependencyTest -DargLine=-Dscore.dtAwdPri=true
 * </pre>
 * The data it needs: a library whose 'Working' release has a Data Type with a Code List (connectSpec).
 */
@EnabledIfSystemProperty(named = "score.dtAwdPri", matches = "true")
class ReleaseCopyDtAwdPriDependencyTest {

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

    private long newRelease(long libraryId, String releaseNum, String state) {
        long user = dsl.fetchOne("select min(app_user_id) from app_user").get(0, Long.class);
        dsl.execute("insert into `release` (library_id, guid, release_num, created_by, last_updated_by, "
                + "creation_timestamp, last_update_timestamp, state) values (?, replace(uuid(), '-', ''), ?, ?, ?, "
                + "now(6), now(6), ?)", libraryId, releaseNum, user, user, state);
        return dsl.fetchOne("select last_insert_id()").get(0, Long.class);
    }

    /**
     * Makes the Code List of the 'Working' release a revision of a Code List of an earlier, published release, as a
     * Code List that has been released once and revised since.
     */
    private void reviseAsOfEarlierRelease(long earlierRelease, long workingCodeListManifest) {
        dsl.execute("insert into code_list (guid, enum_type_guid, name, list_id, version_id, definition, remark, "
                + "definition_source, namespace_id, extensible_indicator, is_deprecated, created_by, owner_user_id, "
                + "last_updated_by, creation_timestamp, last_update_timestamp, state) "
                + "select replace(uuid(), '-', ''), enum_type_guid, name, list_id, version_id, definition, remark, "
                + "definition_source, namespace_id, extensible_indicator, is_deprecated, created_by, owner_user_id, "
                + "last_updated_by, creation_timestamp, last_update_timestamp, 'Published' "
                + "from code_list where code_list_id = (select code_list_id from code_list_manifest "
                + "where code_list_manifest_id = ?)", workingCodeListManifest);
        long earlierCodeList = dsl.fetchOne("select last_insert_id()").get(0, Long.class);
        dsl.execute("insert into code_list_manifest (release_id, code_list_id) values (?, ?)",
                earlierRelease, earlierCodeList);
        long earlierManifest = dsl.fetchOne("select last_insert_id()").get(0, Long.class);
        dsl.execute("update code_list_manifest set prev_code_list_manifest_id = ? where code_list_manifest_id = ?",
                earlierManifest, workingCodeListManifest);
        dsl.execute("update code_list_manifest set next_code_list_manifest_id = ? where code_list_manifest_id = ?",
                workingCodeListManifest, earlierManifest);
    }

    @Test
    void aDataTypeKeepsItsDefaultWhenItsCodeListIsAssigned() {
        assertEquals(1, defaultsOfTheNewRelease(false));
    }

    @Test
    void aDataTypeKeepsItsDefaultWhenItsCodeListIsRevisedAndNotAssigned() {
        assertEquals(1, defaultsOfTheNewRelease(true), "the Data Type of the new release has one default");
    }

    /** An allowed primitive that uses a Code List or an Agency ID List, of a Data Type or of a supplementary component. */
    private enum Use {
        DT_CODE_LIST("connectSpec", "dt_awd_pri", "code_list_manifest_id", "code_list_manifest", false),
        SC_CODE_LIST("connectSpec", "dt_sc_awd_pri", "code_list_manifest_id", "code_list_manifest", true),
        DT_AGENCY_ID_LIST("connectSpec", "dt_awd_pri", "agency_id_list_manifest_id", "agency_id_list_manifest", false),
        SC_AGENCY_ID_LIST("connectSpec", "dt_sc_awd_pri", "agency_id_list_manifest_id", "agency_id_list_manifest", true);

        final String library;
        final String table;
        final String column;
        final String manifestTable;
        final boolean supplementaryComponent;

        Use(String library, String table, String column, String manifestTable, boolean supplementaryComponent) {
            this.library = library;
            this.table = table;
            this.column = column;
            this.manifestTable = manifestTable;
            this.supplementaryComponent = supplementaryComponent;
        }
    }

    /**
     * What the allowed primitive becomes when the Code List or the Agency ID List it uses is in a release the library
     * depends on, not in 'Working': that release is referenced as it is, so the new release keeps the primitive, pointing
     * to the same manifest.
     */
    @ParameterizedTest
    @EnumSource(Use.class)
    void anAllowedPrimitiveKeepsWhatItUsesInAReleaseTheLibraryDependsOn(Use use) {
        long working = dsl.fetchOne("select r.release_id from `release` r join library l using (library_id) "
                + "where l.name = ? and r.release_num = 'Working'", use.library).get(0, Long.class);
        long libraryId = dsl.fetchOne("select library_id from `release` where release_id = ?", working)
                .get(0, Long.class);

        String owner = use.supplementaryComponent
                ? "join dt_sc_manifest m on m.dt_sc_id = p.dt_sc_id and m.release_id = p.release_id"
                : "join dt_manifest m on m.dt_id = p.dt_id and m.release_id = p.release_id";
        var row = dsl.fetchOne("select " + (use.supplementaryComponent ? "p.dt_sc_awd_pri_id" : "p.dt_awd_pri_id")
                + ", m." + (use.supplementaryComponent ? "dt_sc_manifest_id, m.owner_dt_manifest_id" : "dt_manifest_id, m.dt_manifest_id")
                + " from " + use.table + " p " + owner + " where p.release_id = ? and p." + use.column
                + " is not null limit 1", working);
        assertNotNull(row, "the library has no such allowed primitive");
        long awdPri = row.get(0, Long.class);
        long ownerManifest = row.get(1, Long.class);
        long dtManifest = row.get(2, Long.class);

        var dependency = dsl.fetchOne("select min(" + use.column + ") from " + use.manifestTable
                + " where release_id <> ?", working);
        assertNotNull(dependency.get(0), "no manifest in another release");
        long dependencyManifest = dependency.get(0, Long.class);
        dsl.execute("update " + use.table + " set " + use.column + " = ? where "
                + (use.supplementaryComponent ? "dt_sc_awd_pri_id" : "dt_awd_pri_id") + " = ?", dependencyManifest, awdPri);

        long release = newRelease(libraryId, "test-new", "Draft");
        factory.ccCommandRepository(REQUESTER).copyWorkingManifests(new ReleaseId(BigInteger.valueOf(release)),
                new ReleaseId(BigInteger.valueOf(working)),
                List.<AccManifestId>of(), List.<AsccpManifestId>of(), List.<BccpManifestId>of(),
                List.of(new DtManifestId(BigInteger.valueOf(dtManifest))),
                List.<CodeListManifestId>of(), List.<AgencyIdListManifestId>of());

        String next = use.supplementaryComponent ? "next_dt_sc_manifest_id" : "next_dt_manifest_id";
        long kept = dsl.fetchOne("select count(*) from " + use.table + " p " + owner
                + " where p.release_id = ? and m." + next + " = ? and p." + use.column + " = ?",
                release, ownerManifest, dependencyManifest).get(0, Long.class);
        assertEquals(1, kept, "the allowed primitive is kept, pointing to the manifest of the release it depends on");
    }

    /**
     * An XBT that has a previous release keeps its allowed primitives: they are found through the link to the manifest of
     * 'Working', not through the record the manifest holds.
     */
    @Test
    void aDataTypeKeepsItsXbtAllowedPrimitivesWhenTheXbtIsRevised() {
        long working = dsl.fetchOne("select r.release_id from `release` r join library l using (library_id) "
                + "where l.name = 'connectSpec' and r.release_num = 'Working'").get(0, Long.class);
        long libraryId = dsl.fetchOne("select library_id from `release` where release_id = ?", working).get(0, Long.class);
        var row = dsl.fetchOne("select dm.dt_manifest_id, p.xbt_manifest_id, x.xbt_id "
                + "from dt_manifest dm join dt_awd_pri p on p.dt_id = dm.dt_id and p.release_id = dm.release_id "
                + "join xbt_manifest x on x.xbt_manifest_id = p.xbt_manifest_id "
                + "where dm.release_id = ? and p.xbt_manifest_id is not null and x.release_id = dm.release_id limit 1", working);
        assertNotNull(row, "no Data Type of the library has an XBT");
        long dtManifest = row.get(0, Long.class);
        long xbtManifest = row.get(1, Long.class);
        long xbtId = row.get(2, Long.class);
        long otherXbt = dsl.fetchOne("select min(xbt_id) from xbt where xbt_id <> ?", xbtId).get(0, Long.class);

        long earlier = newRelease(libraryId, "test-earlier", "Published");
        long release = newRelease(libraryId, "test-new", "Draft");
        dsl.execute("insert into xbt_manifest (release_id, xbt_id) values (?, ?)", earlier, otherXbt);
        long earlierManifest = dsl.fetchOne("select last_insert_id()").get(0, Long.class);
        dsl.execute("update xbt_manifest set prev_xbt_manifest_id = ? where xbt_manifest_id = ?", earlierManifest, xbtManifest);
        dsl.execute("update xbt_manifest set next_xbt_manifest_id = ? where xbt_manifest_id = ?", xbtManifest, earlierManifest);

        factory.ccCommandRepository(REQUESTER).copyWorkingManifests(new ReleaseId(BigInteger.valueOf(release)),
                new ReleaseId(BigInteger.valueOf(working)),
                List.<AccManifestId>of(), List.<AsccpManifestId>of(), List.<BccpManifestId>of(),
                List.of(new DtManifestId(BigInteger.valueOf(dtManifest))),
                List.<CodeListManifestId>of(), List.<AgencyIdListManifestId>of());

        long inWorking = dsl.fetchOne("select count(*) from dt_awd_pri p join dt_manifest m on m.dt_id = p.dt_id "
                + "and m.release_id = p.release_id where p.release_id = ? and m.dt_manifest_id = ? "
                + "and p.xbt_manifest_id = ?", working, dtManifest, xbtManifest).get(0, Long.class);
        long inRelease = dsl.fetchOne("select count(*) from dt_awd_pri p join dt_manifest m on m.dt_id = p.dt_id "
                + "and m.release_id = p.release_id join xbt_manifest x on x.xbt_manifest_id = p.xbt_manifest_id "
                + "where p.release_id = ? and m.next_dt_manifest_id = ? and x.next_xbt_manifest_id = ?",
                release, dtManifest, xbtManifest).get(0, Long.class);
        assertEquals(inWorking, inRelease, "the allowed primitives of the Data Type that use the revised XBT are kept");
    }

    /**
     * Releases a Data Type of the library whose default is a Code List. When {@code revisedNotAssigned}, the Code List is
     * a revision that is not assigned to the release; otherwise it is assigned.
     */
    private long defaultsOfTheNewRelease(boolean revisedNotAssigned) {
        long working = dsl.fetchOne("select r.release_id from `release` r join library l using (library_id) "
                + "where l.name = 'connectSpec' and r.release_num = 'Working'").get(0, Long.class);
        long libraryId = dsl.fetchOne("select library_id from `release` where release_id = ?", working)
                .get(0, Long.class);
        var row = dsl.fetchOne("select dm.dt_manifest_id, dm.dt_id, p.code_list_manifest_id, p.dt_awd_pri_id "
                + "from dt_manifest dm join dt_awd_pri p on p.dt_id = dm.dt_id and p.release_id = dm.release_id "
                + "where dm.release_id = ? and p.code_list_manifest_id is not null order by p.is_default desc limit 1", working);
        assertNotNull(row, "no Data Type of the library has a Code List");
        // the initial data of the library has no Data Type with a Code List as its default: it is made one here
        dsl.execute("update dt_awd_pri set is_default = (dt_awd_pri_id = ?) where release_id = ? and dt_id = ?",
                row.get(3, Long.class), working, row.get(1, Long.class));
        long dtManifest = row.get(0, Long.class);
        long codeListManifest = row.get(2, Long.class);

        long earlier = newRelease(libraryId, "test-earlier", "Published");
        long release = newRelease(libraryId, "test-new", "Draft");
        if (revisedNotAssigned) {
            reviseAsOfEarlierRelease(earlier, codeListManifest);
        }

        factory.ccCommandRepository(REQUESTER).copyWorkingManifests(new ReleaseId(BigInteger.valueOf(release)),
                new ReleaseId(BigInteger.valueOf(working)),
                List.<AccManifestId>of(), List.<AsccpManifestId>of(), List.<BccpManifestId>of(),
                List.of(new DtManifestId(BigInteger.valueOf(dtManifest))),
                revisedNotAssigned ? List.<CodeListManifestId>of()
                        : List.of(new CodeListManifestId(BigInteger.valueOf(codeListManifest))),
                List.<AgencyIdListManifestId>of());

        return dsl.fetchOne("select count(*) from dt_awd_pri p join dt_manifest m "
                + "on m.dt_id = p.dt_id and m.release_id = p.release_id "
                + "where p.release_id = ? and m.next_dt_manifest_id = ? and p.is_default = 1",
                release, dtManifest).get(0, Long.class);
    }
}
