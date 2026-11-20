package org.oagi.score.gateway.http.api.cc_management.service;

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
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.agency_id_management.service.AgencyIdListQueryService;
import org.oagi.score.gateway.http.api.bie_management.service.BieUpliftingService;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.ValueListCandidate;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.service.CcMatchingService;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListQueryRepository;
import org.oagi.score.gateway.http.api.code_list_management.service.CodeListQueryService;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.TestDataSource;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Uplifting a BIE chooses its Code Lists and Agency ID Lists from what the BIE editor offers for the node: the editor's
 * lists less what a developer cannot assign (Production, or owned by an end user; only for Code Lists).
 * Against the development database:
 * <pre>
 *   mvn test -Dtest=ValueListCandidateParityTest -DargLine=-Dscore.valueListCandidates=true
 * </pre>
 * Every data type and supplementary component of a release is compared, for a developer and for any other user. The data
 * has few derived lists and states other than Published, so the cases that it lacks are made inside a transaction that is
 * rolled back: lists derived from each other, from a list that no data type names, and lists in other states.
 * <p>
 * When the editor moved to {@link ValueListCandidateRule}, its answers were compared with those of the queries used before
 * (every data type and supplementary component of a release, a developer and any other user, on the data as it is and on
 * the cases made as here): they were the same.
 */
@EnabledIfSystemProperty(named = "score.valueListCandidates", matches = "true")
class ValueListCandidateParityTest {

    private static TestDataSource dataSource;
    private static Connection connection;
    private static DSLContext dsl;
    private static CodeListQueryRepository codeListQuery;
    private static AgencyIdListQueryRepository agencyIdListQuery;
    private static ReleaseId release;
    private static RepositoryFactory factory;
    private static CodeListQueryService codeListService;
    private static AgencyIdListQueryService agencyIdListService;
    private static final ScoreUser REQUESTER = new ScoreUser(new UserId(BigInteger.ZERO), "sysadm", "sysadm",
            "sysadm@example.com", true, List.of(ScoreRole.ADMINISTRATOR));
    private static final ScoreUser DEVELOPER = new ScoreUser(new UserId(BigInteger.ONE), "developer", "developer",
            "developer@example.com", true, List.of(ScoreRole.DEVELOPER));
    private static final ScoreUser END_USER = new ScoreUser(new UserId(BigInteger.TWO), "enduser", "enduser",
            "enduser@example.com", false, List.of(ScoreRole.END_USER));

    private static ScoreUser userOf(boolean developer) {
        return developer ? DEVELOPER : END_USER;
    }

    @BeforeAll
    static void connect() throws SQLException {
        dataSource = TestDataSource.open(2);
        connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        dsl = DSL.using(connection, SQLDialect.MARIADB);
        factory = new RepositoryFactory(dsl);
        codeListQuery = factory.codeListQueryRepository(REQUESTER);
        agencyIdListQuery = factory.agencyIdListQueryRepository(REQUESTER);
        codeListService = new CodeListQueryService();
        ReflectionTestUtils.setField(codeListService, "repositoryFactory", factory);
        agencyIdListService = new AgencyIdListQueryService();
        ReflectionTestUtils.setField(agencyIdListService, "repositoryFactory", factory);
        // the latest release that has both kinds of list and data types that name them
        release = new ReleaseId(dsl.fetchOne("select max(r.release_id) from `release` r"
                + " where exists (select 1 from code_list_manifest m where m.release_id = r.release_id)"
                + " and exists (select 1 from agency_id_list_manifest m where m.release_id = r.release_id)"
                + " and exists (select 1 from dt_awd_pri p where p.release_id = r.release_id and p.code_list_manifest_id is not null)"
                + " and exists (select 1 from dt_awd_pri p where p.release_id = r.release_id and p.agency_id_list_manifest_id is not null)")
                .get(0, BigInteger.class));
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

    // ----- the data -----

    private static List<DtManifestId> dts() {
        return dsl.fetch("select dt_manifest_id from dt_manifest where release_id = ? order by 1", release.value())
                .map(r -> new DtManifestId(r.get(0, BigInteger.class)));
    }

    private static List<DtScManifestId> dtScs() {
        return dsl.fetch("select dt_sc_manifest_id from dt_sc_manifest where release_id = ? order by 1", release.value())
                .map(r -> new DtScManifestId(r.get(0, BigInteger.class)));
    }

    /** The manifests that a data type of the release names, in order. */
    private static List<Long> namedCodeLists() {
        return dsl.fetch("select distinct code_list_manifest_id from dt_awd_pri where release_id = ? and code_list_manifest_id is not null order by 1",
                release.value()).map(r -> r.get(0, Long.class));
    }

    private static List<Long> unnamedCodeLists() {
        return dsl.fetch("select m.code_list_manifest_id from code_list_manifest m where m.release_id = ?"
                        + " and m.code_list_manifest_id not in (select code_list_manifest_id from dt_awd_pri where code_list_manifest_id is not null)"
                        + " and m.code_list_manifest_id not in (select code_list_manifest_id from dt_sc_awd_pri where code_list_manifest_id is not null)"
                        + " order by 1", release.value())
                .map(r -> r.get(0, Long.class));
    }

    private static void basedOn(long manifest, long base) {
        dsl.execute("update code_list_manifest set based_code_list_manifest_id = ? where code_list_manifest_id = ?", base, manifest);
    }

    private static void stateOf(long manifest, String state) {
        dsl.execute("update code_list c join code_list_manifest m on m.code_list_id = c.code_list_id set c.state = ?"
                + " where m.code_list_manifest_id = ?", state, manifest);
    }

    /** Lists derived from each other, from a list that no data type names, and lists in other states: made, not found. */
    private static void deriveCodeListsAndChangeTheirStates() {
        List<Long> named = namedCodeLists();
        List<Long> unnamed = unnamedCodeLists();
        assertTrue(named.size() >= 12, "the release names " + named.size() + " Code Lists");
        assertTrue(unnamed.size() >= 6, "the release leaves " + unnamed.size() + " Code Lists unnamed");

        // a family of named lists: 1 and 5 derive from 0; 2 and 3 from 1; 4 from 2
        basedOn(named.get(1), named.get(0));
        basedOn(named.get(5), named.get(0));
        basedOn(named.get(2), named.get(1));
        basedOn(named.get(3), named.get(1));
        basedOn(named.get(4), named.get(2));
        // a named list derived from a list that no data type names, and a second one from the same base
        basedOn(named.get(6), unnamed.get(0));
        basedOn(named.get(7), unnamed.get(0));
        // a list derived from a named list, that no data type names
        basedOn(unnamed.get(1), named.get(8));
        basedOn(unnamed.get(2), unnamed.get(1));
        // other states
        stateOf(named.get(2), "WIP");
        stateOf(named.get(5), "Deleted");
        stateOf(named.get(9), "Production");
        stateOf(named.get(10), "QA");
        stateOf(unnamed.get(0), "Candidate");
        stateOf(unnamed.get(3), "WIP");
        stateOf(unnamed.get(4), "Production");
        stateOf(unnamed.get(5), "Deleted");
    }

    /** The Agency ID Lists of the release, whatever their manifests, in a state: the release has few list records. */
    private static void agencyIdListsAre(String state) {
        dsl.execute("update agency_id_list l join agency_id_list_manifest m on m.agency_id_list_id = l.agency_id_list_id"
                + " set l.state = ? where m.release_id = ?", state, release.value());
    }

    private static void agencyIdListDerivedFrom(Long base) {
        dsl.execute("insert into agency_id_list_manifest (release_id, agency_id_list_id, agency_id_list_value_manifest_id,"
                + " based_agency_id_list_manifest_id)"
                + " select release_id, agency_id_list_id, agency_id_list_value_manifest_id, agency_id_list_manifest_id"
                + " from agency_id_list_manifest where agency_id_list_manifest_id = ?", base);
    }

    private static Long deriveAgencyIdLists() {
        List<Long> existing = dsl.fetch("select agency_id_list_manifest_id from agency_id_list_manifest where release_id = ? order by 1",
                release.value()).map(r -> r.get(0, Long.class));
        assertTrue(!existing.isEmpty(), "the release has no Agency ID List");
        Long named = dsl.fetchOne("select min(agency_id_list_manifest_id) from dt_awd_pri where release_id = ?"
                + " and agency_id_list_manifest_id is not null", release.value()).get(0, Long.class);
        assertTrue(named != null, "no data type names an Agency ID List");

        // three lists derived from the named one, two from the first of those, one from that
        agencyIdListDerivedFrom(named);
        agencyIdListDerivedFrom(named);
        agencyIdListDerivedFrom(named);
        List<Long> derived = dsl.fetch("select agency_id_list_manifest_id from agency_id_list_manifest where based_agency_id_list_manifest_id = ? order by 1",
                named).map(r -> r.get(0, Long.class));
        agencyIdListDerivedFrom(derived.get(0));
        agencyIdListDerivedFrom(derived.get(0));
        Long grandchild = dsl.fetchOne("select max(agency_id_list_manifest_id) from agency_id_list_manifest where based_agency_id_list_manifest_id = ?",
                derived.get(0)).get(0, Long.class);
        agencyIdListDerivedFrom(grandchild);
        return named;
    }

    // ----- the comparison -----

    private record Counts(int nodes, int reachedBeyondTheNamed) {
    }

    private Counts compareUpliftingWithTheEditor(List<String> problems) throws Exception {
        var constructor = BieUpliftingService.class.getDeclaredConstructor(CcMatchingService.class);
        constructor.setAccessible(true);
        BieUpliftingService uplifting = constructor.newInstance(new CcMatchingService());
        var codeListCandidates = codeListQuery.getCodeListCandidateList(release);
        var agencyIdListCandidates = agencyIdListQuery.getAgencyIdListCandidateList(release);
        Map<CodeListManifestId, ValueListCandidate<CodeListManifestId>> codeListById =
                codeListCandidates.stream().collect(Collectors.toMap(ValueListCandidate::manifestId, e -> e));
        int nodes = 0;
        int reached = 0;
        for (boolean developer : new boolean[]{true, false}) {
            ScoreUser user = userOf(developer);
            CcDocument document = factory.ccDocumentFactory().create(user, release);
            for (DtManifestId dt : dts()) {
                nodes++;
                Set<CodeListManifestId> named = codeListQuery.getNamedCodeListManifestIdsByDtManifestId(dt);
                Set<CodeListManifestId> editor = codeListService.availableCodeListListByDtManifestId(user, dt).stream()
                        .map(CodeListSummaryRecord::codeListManifestId).collect(Collectors.toSet());
                reached += !named.isEmpty() && !named.containsAll(editor) ? 1 : 0;
                compareCodeLists(problems, "DT " + dt + ", developer=" + developer, developer, codeListById, editor,
                        ReflectionTestUtils.invokeMethod(uplifting, "availableCodeLists", user, codeListCandidates,
                                document.getDtAwdPriList(dt),
                                (Function<DtAwdPriSummaryRecord, CodeListManifestId>) DtAwdPriSummaryRecord::codeListManifestId));

                Set<AgencyIdListManifestId> agencyEditor = agencyIdListService.availableAgencyIdListListByDtManifestId(user, dt).stream()
                        .map(AgencyIdListSummaryRecord::agencyIdListManifestId).collect(Collectors.toSet());
                List<ValueListCandidate<AgencyIdListManifestId>> agencyUplifted = ReflectionTestUtils.invokeMethod(
                        uplifting, "availableAgencyIdLists", user, agencyIdListCandidates, document.getDtAwdPriList(dt),
                        (Function<DtAwdPriSummaryRecord, AgencyIdListManifestId>) DtAwdPriSummaryRecord::agencyIdListManifestId);
                Set<AgencyIdListManifestId> agencyActual = agencyUplifted.stream().map(ValueListCandidate::manifestId).collect(Collectors.toSet());
                if (!agencyEditor.equals(agencyActual)) {
                    problems.add("Agency ID List, DT " + dt + ", developer=" + developer + ": editor " + agencyEditor + ", uplifting " + agencyActual);
                }
            }
            for (DtScManifestId dtSc : dtScs()) {
                nodes++;
                Set<CodeListManifestId> editor = codeListService.availableCodeListListByDtScManifestId(user, dtSc).stream()
                        .map(CodeListSummaryRecord::codeListManifestId).collect(Collectors.toSet());
                compareCodeLists(problems, "DT_SC " + dtSc + ", developer=" + developer, developer, codeListById, editor,
                        ReflectionTestUtils.invokeMethod(uplifting, "availableCodeLists", user, codeListCandidates,
                                document.getDtScAwdPriList(dtSc),
                                (Function<DtScAwdPriSummaryRecord, CodeListManifestId>) DtScAwdPriSummaryRecord::codeListManifestId));

                Set<AgencyIdListManifestId> agencyEditor = agencyIdListService.availableAgencyIdListListByDtScManifestId(user, dtSc).stream()
                        .map(AgencyIdListSummaryRecord::agencyIdListManifestId).collect(Collectors.toSet());
                List<ValueListCandidate<AgencyIdListManifestId>> agencyUplifted = ReflectionTestUtils.invokeMethod(
                        uplifting, "availableAgencyIdLists", user, agencyIdListCandidates, document.getDtScAwdPriList(dtSc),
                        (Function<DtScAwdPriSummaryRecord, AgencyIdListManifestId>) DtScAwdPriSummaryRecord::agencyIdListManifestId);
                Set<AgencyIdListManifestId> agencyActual = agencyUplifted.stream().map(ValueListCandidate::manifestId).collect(Collectors.toSet());
                if (!agencyEditor.equals(agencyActual)) {
                    problems.add("Agency ID List, DT_SC " + dtSc + ", developer=" + developer + ": editor " + agencyEditor + ", uplifting " + agencyActual);
                }
            }
        }
        return new Counts(nodes, reached);
    }

    private static void compareCodeLists(List<String> problems, String node, boolean developer,
                                         Map<CodeListManifestId, ValueListCandidate<CodeListManifestId>> codeListById,
                                         Set<CodeListManifestId> editor,
                                         List<ValueListCandidate<CodeListManifestId>> uplifted) {
        Set<CodeListManifestId> expected = editor.stream()
                .filter(id -> !developer || (codeListById.get(id).state() != CcState.Production && !codeListById.get(id).endUserOwned()))
                .collect(Collectors.toSet());
        Set<CodeListManifestId> actual = uplifted.stream().map(ValueListCandidate::manifestId).collect(Collectors.toSet());
        if (!expected.equals(actual)) {
            problems.add("Code List, " + node + ": editor less unassignable " + expected + ", uplifting " + actual);
        }
    }

    @Test
    void uplifting_offers_what_the_editor_offers_less_what_a_developer_cannot_assign() throws Exception {
        List<String> problems = new ArrayList<>();
        Counts counts = compareUpliftingWithTheEditor(problems);
        System.out.println("the data as it is: " + counts);
        assertEquals(List.of(), problems.stream().limit(10).toList());
    }

    @Test
    void uplifting_offers_what_the_editor_offers_for_lists_derived_from_each_other_and_in_other_states() throws Exception {
        deriveCodeListsAndChangeTheirStates();
        deriveAgencyIdLists();
        List<String> problems = new ArrayList<>();
        Counts counts = compareUpliftingWithTheEditor(problems);
        // What a developer is offered of the Agency ID Lists depends on their state, so the lists are compared in some others.
        for (String state : List.of("WIP", "Production")) {
            agencyIdListsAre(state);
            compareUpliftingWithTheEditor(problems);
        }
        System.out.println("derived and in other states: " + counts);
        assertEquals(List.of(), problems.stream().limit(10).toList());
        assertTrue(counts.reachedBeyondTheNamed() > 0, "no data type reached a list that it does not name");
    }
}
