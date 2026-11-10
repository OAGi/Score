package org.oagi.score.gateway.http.api.release_management.service;

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
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpType;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseDependencyValidatorMatrixTest.Location;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseDependencyValidatorMatrixTest.Scenario;
import org.oagi.score.gateway.http.common.TestDataSource;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The 8.0 release of a library is published; what the scenario tests build in its 'Working' release. */
abstract class ReleaseScenarioFixture {


    static final ScoreUser REQUESTER = new ScoreUser(new UserId(BigInteger.ZERO), "sysadm", "sysadm",
            "sysadm@example.com", true, List.of(ScoreRole.ADMINISTRATOR));


    /** What a component of 'Working' is, since 8.0. */
    enum Variant {
        P(CcState.Published, true), // published and untouched
        R_WIP(CcState.WIP, true), R_DRAFT(CcState.Draft, true), R_CAND(CcState.Candidate, true), // revised
        N_WIP(CcState.WIP, false), N_DRAFT(CcState.Draft, false), N_CAND(CcState.Candidate, false); // new

        final CcState state;
        final boolean released;

        Variant(CcState state, boolean released) {
            this.state = state;
            this.released = released;
        }

        boolean revised() {
            return name().startsWith("R_");
        }

        boolean isNew() {
            return name().startsWith("N_");
        }
    }

    /** A variant and whether it is assigned to the release; only a Published or a Candidate one can be assigned. */
    record Cls(Variant variant, boolean assigned) {
        @Override
        public String toString() {
            return variant + (assigned ? "/assigned" : "/unassigned");
        }
    }

    static List<Cls> classes() {
        List<Cls> classes = new ArrayList<>();
        for (Variant v : Variant.values()) {
            classes.add(new Cls(v, false));
            if (v == Variant.P || v == Variant.R_CAND || v == Variant.N_CAND) {
                classes.add(new Cls(v, true));
            }
        }
        return classes;
    }

    enum Kind {
        ACC("acc"), ASCCP("asccp"), BCCP("bccp"), DT("dt"), CODE_LIST("code_list");

        final String table;

        Kind(String table) {
            this.table = table;
        }

        String manifest() {
            return table + "_manifest";
        }

        String id() {
            return table + "_manifest_id";
        }

        String record() {
            return table + "_id";
        }
    }

    record Inst(Kind kind, long id, Cls cls) {
    }

    record Edge(String name, Kind dependent, Kind target, BiConsumer<Inst, Inst> wire) {
    }

    static TestDataSource dataSource;
    Connection connection;
    DSLContext dsl;
    RepositoryFactory factory;

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
    void finish() throws SQLException {
        if (Boolean.getBoolean("score.keep")) {
            connection.commit();
        } else {
            connection.rollback();
        }
        connection.close();
    }

    long one(String sql, Object... bindings) {
        return dsl.fetchOne(sql, bindings).get(0, Long.class);
    }

    List<Long> ids(String sql, Object... bindings) {
        return dsl.fetch(sql, bindings).map(r -> r.get(0, Long.class));
    }

    static BigInteger n(long v) {
        return BigInteger.valueOf(v);
    }

    // ---- the library ----

    long library;
    long working;
    long published;
    long namespace;
    final Set<Long> used = new HashSet<>();
    long anchorAcc;
    long anchorDt;
    int serial;

    void findLibrary(String name) {
        library = one("select library_id from library where name = ?", name);
        working = one("select release_id from `release` where library_id = ? and release_num = 'Working'", library);
        published = one("select max(release_id) from `release` where library_id = ? and release_num <> 'Working' "
                + "and state = 'Published'", library);
        namespace = one("select namespace_id from `release` where release_id = ?", working);
        anchorAcc = pool(Kind.ACC);
        anchorDt = pool(Kind.DT);
    }

    /** The next component of 'Working' that has been released and not touched since, and is not used yet. */
    long pool(Kind kind) {
        String extra = switch (kind) {
            case DT -> " and m.based_dt_manifest_id in (select dt_manifest_id from dt_manifest where release_id = "
                    + working + ") and exists (select 1 from dt_sc_manifest s where s.owner_dt_manifest_id = m.dt_manifest_id)";
            default -> "";
        };
        String notUsed = used.isEmpty() ? "" : " and m.`" + kind.id() + "` not in ("
                + String.join(",", used.stream().map(String::valueOf).toList()) + ")";
        List<Long> found = ids("select m.`" + kind.id() + "` from `" + kind.manifest() + "` m join `" + kind.table
                + "` x using (`" + kind.record() + "`) where m.release_id = ? and x.state = 'Published' "
                + "and m.`prev_" + kind.id() + "` is not null" + extra + notUsed + " order by m.`" + kind.id()
                + "` limit 1", working);
        assertTrue(!found.isEmpty(), "the pool of the published " + kind + " in 'Working' is exhausted");
        used.add(found.get(0));
        return found.get(0);
    }

    void setState(Kind kind, long id, CcState state) {
        dsl.execute("update `" + kind.table + "` set state = ? where `" + kind.record() + "` = "
                + "(select `" + kind.record() + "` from `" + kind.manifest() + "` where `" + kind.id() + "` = ?)",
                state.name(), id);
    }

    void setNamespace(Kind kind, long id) {
        dsl.execute("update `" + kind.table + "` set namespace_id = ? where `" + kind.record() + "` = "
                + "(select `" + kind.record() + "` from `" + kind.manifest() + "` where `" + kind.id() + "` = ?)",
                namespace, id);
    }

    long recordOf(Kind kind, long id) {
        return one("select `" + kind.record() + "` from `" + kind.manifest() + "` where `" + kind.id() + "` = ?", id);
    }

    Inst make(Kind kind, Cls cls) {
        ReleaseId releaseId = new ReleaseId(n(working));
        NamespaceId namespaceId = new NamespaceId(n(namespace));
        Variant v = cls.variant();
        long id;
        if (v == Variant.P) {
            id = pool(kind);
        } else if (v.revised()) {
            id = pool(kind);
            switch (kind) {
                case ACC -> factory.accCommandRepository(REQUESTER).revise(new AccManifestId(n(id)));
                case ASCCP -> factory.asccpCommandRepository(REQUESTER).revise(new AsccpManifestId(n(id)));
                case BCCP -> factory.bccpCommandRepository(REQUESTER).revise(new BccpManifestId(n(id)));
                case DT -> factory.dtCommandRepository(REQUESTER).revise(new DtManifestId(n(id)));
                case CODE_LIST -> factory.codeListCommandRepository(REQUESTER).revise(new CodeListManifestId(n(id)));
            }
        } else {
            String term = "Scenario " + (++serial);
            id = switch (kind) {
                case ACC -> factory.accCommandRepository(REQUESTER)
                        .create(releaseId, null, term, null, null, null, namespaceId).value().longValue();
                case ASCCP -> factory.asccpCommandRepository(REQUESTER)
                        .create(releaseId, new AccManifestId(n(anchorAcc)), term, AsccpType.Default, true, null,
                                namespaceId, null).value().longValue();
                case BCCP -> factory.bccpCommandRepository(REQUESTER)
                        .create(releaseId, new DtManifestId(n(anchorDt)), term).value().longValue();
                case DT -> factory.dtCommandRepository(REQUESTER)
                        .create(releaseId, new DtManifestId(n(anchorDt))).value().longValue();
                case CODE_LIST -> factory.codeListCommandRepository(REQUESTER)
                        .create(releaseId, null).value().longValue();
            };
            used.add(id);
            setNamespace(kind, id);
        }
        if (v != Variant.P) {
            setState(kind, id, v.state);
        }
        return new Inst(kind, id, cls);
    }


    long newRelease(String num) {
        long user = one("select min(app_user_id) from app_user");
        dsl.execute("insert into `release` (library_id, guid, release_num, namespace_id, created_by, last_updated_by, "
                + "creation_timestamp, last_update_timestamp, state) values (?, replace(uuid(), '-', ''), ?, ?, ?, ?, "
                + "now(6), now(6), 'Initialized')", library, num, namespace, user, user);
        return one("select last_insert_id()");
    }


    static Scenario scenario(Cls dependent, Cls target) {
        return new Scenario(dependent.variant().state, dependent.assigned(), Location.WORKING,
                target.variant().state, target.assigned(), target.variant().released);
    }

    /** Whether the dependent is wired to the target in the release that is to be published. */
    static boolean valid(Cls dependent, Cls target) {
        return ReleaseDependencyValidatorMatrixTest.expected(scenario(dependent, target)).stream()
                .noneMatch(m -> m.startsWith("Error"));
    }

}
