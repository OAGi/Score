package org.oagi.score.gateway.http.api.cc_management.propagation;

import org.oagi.score.gateway.http.api.cc_management.model.CcRecordTypes;

import org.oagi.score.gateway.http.cache.RecordChangeKeyResolver;
import org.oagi.score.gateway.http.cache.TestRecordKeys;


import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.oagi.score.gateway.http.change.TestIds.event;
import static org.oagi.score.gateway.http.change.TestIds.id;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.repository.jooq.JooqCcDependencyLookup;
import org.oagi.score.gateway.http.change.ChangeEvent;
import org.oagi.score.gateway.http.change.RecordType;
import org.oagi.score.gateway.http.change.propagation.PropagationEngine;
import org.oagi.score.gateway.http.change.propagation.PropagationResult;
import org.oagi.score.gateway.http.common.TestDataSource;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The reverse lookups against the development database, and the worked example end to end with the real data: the keys the
 * rules name for a change of an ASCCP are those of the ASCCs that point to it. Reads only.
 * <pre>
 *   mvn test -Dtest=JooqCcDependencyLookupTest -DargLine=-Dscore.freshness=true
 * </pre>
 */
@EnabledIfSystemProperty(named = "score.freshness", matches = "true")
class JooqCcDependencyLookupTest {

    private static final RecordChangeKeyResolver KEYS = TestRecordKeys.resolver(null);


    private static TestDataSource dataSource;
    private static Connection connection;
    private static DSLContext dsl;
    private static JooqCcDependencyLookup lookup;

    @BeforeAll
    static void connect() throws SQLException {
        dataSource = TestDataSource.open(2);
        connection = dataSource.getConnection();
        dsl = DSL.using(connection, SQLDialect.MARIADB);
        lookup = new JooqCcDependencyLookup(dsl, 3);   // a small batch: every lookup below asks more than once
    }

    @AfterAll
    static void disconnect() throws SQLException {
        connection.close();
        dataSource.close();
    }

    private static long first(String sql) {
        return dsl.fetchOne(sql).get(0, Long.class);
    }

    private static Set<Long> ids(List<? extends Dependent<?, ?>> dependents) {
        return dependents.stream().map(d -> d.id().value().longValue()).collect(Collectors.toSet());
    }

    private static BigInteger big(long id) {
        return BigInteger.valueOf(id);
    }

    @Test
    void asccsPointingToAnAsccpAreFoundWithTheAccTheyAreIn() {
        long asccp = first("select to_asccp_manifest_id from ascc_manifest group by 1 having count(*) > 1 limit 1");
        Set<Long> expected = dsl.fetch("select ascc_manifest_id from ascc_manifest where to_asccp_manifest_id = ?", asccp)
                .intoSet(0, Long.class);

        List<Dependent<AsccpManifestId, AsccManifestId>> found = lookup.asccsToAsccp(List.of(new AsccpManifestId(big(asccp))));

        assertEquals(expected, ids(found));
        for (Dependent<AsccpManifestId, AsccManifestId> d : found) {
            assertEquals(asccp, d.source().value().longValue());
            assertEquals(dsl.fetchOne("select from_acc_manifest_id from ascc_manifest where ascc_manifest_id = ?", d.id().value())
                    .get(0, Long.class), d.parent().value().longValue());
        }
    }

    @Test
    void aBatchIsSplitAndEverySourceIsAnswered() {
        List<Long> accs = dsl.fetch("select from_acc_manifest_id from ascc_manifest group by 1 order by count(*) desc limit 7")
                .getValues(0, Long.class);
        Set<Long> expected = dsl.fetch("select ascc_manifest_id from ascc_manifest where from_acc_manifest_id in ("
                + accs.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")").intoSet(0, Long.class);

        List<Dependent<AccManifestId, AsccManifestId>> found = lookup.asccsFromAcc(accs.stream().map(a -> new AccManifestId(big(a))).toList());   // 7 ids, batch of 3

        assertEquals(expected, ids(found));
        assertTrue(found.stream().allMatch(d -> accs.contains(d.source().value().longValue()) && d.parent().equals(d.source())));
    }

    @Test
    void otherLookupsMatchTheirColumns() {
        long acc = first("select role_of_acc_manifest_id from asccp_manifest group by 1 order by count(*) desc limit 1");
        assertEquals(dsl.fetch("select asccp_manifest_id from asccp_manifest where role_of_acc_manifest_id = ?", acc)
                .intoSet(0, Long.class), ids(lookup.asccpsRoleOf(List.of(new AccManifestId(big(acc))))));

        long bccp = first("select to_bccp_manifest_id from bcc_manifest limit 1");
        assertEquals(dsl.fetch("select bcc_manifest_id from bcc_manifest where to_bccp_manifest_id = ?", bccp)
                .intoSet(0, Long.class), ids(lookup.bccsToBccp(List.of(new BccpManifestId(big(bccp))))));

        long dt = first("select bdt_manifest_id from bccp_manifest group by 1 order by count(*) desc limit 1");
        assertEquals(dsl.fetch("select bccp_manifest_id from bccp_manifest where bdt_manifest_id = ?", dt)
                .intoSet(0, Long.class), ids(lookup.bccpsOfDt(List.of(new DtManifestId(big(dt))))));

    }

    @Test
    void noSourceMeansNoQuery() {
        assertTrue(lookup.asccsToAsccp(List.of()).isEmpty());
    }

    @Test
    void theWorkedExampleOnRealData() {
        long asccp = first("select to_asccp_manifest_id from ascc_manifest group by 1 having count(*) > 1 limit 1");
        Set<Long> asccs = dsl.fetch("select ascc_manifest_id from ascc_manifest where to_asccp_manifest_id = ?", asccp)
                .intoSet(0, Long.class);

        PropagationResult result = new PropagationEngine(CcHandlers.of(lookup))
                .propagate(List.of(event(CcRecordTypes.ASCCP, asccp)));
        System.out.println(result.describe(event -> KEYS.keysOfRecords(List.of(event))));

        Set<String> keys = KEYS.keysOfRecords(result.affected());
        asccs.forEach(ascc -> assertTrue(keys.contains("ascc:" + ascc)));
        Set<Long> parents = dsl.fetch("select from_acc_manifest_id from ascc_manifest where to_asccp_manifest_id = ?", asccp)
                .intoSet(0, Long.class);
        parents.forEach(acc -> assertTrue(keys.contains("ascc-from-acc:" + acc) && !keys.contains("acc:" + acc)));
    }

    @Test
    void aWidelyUsedAccReachesManyRecordsAndEnds() {
        long acc = first("select role_of_acc_manifest_id from asccp_manifest group by 1 order by count(*) desc limit 1");
        PropagationEngine engine = new PropagationEngine(CcHandlers.of(lookup));

        long start = System.nanoTime();
        PropagationResult result = engine.propagate(List.of(event(CcRecordTypes.ACC, acc)));
        long ms = (System.nanoTime() - start) / 1_000_000;

        System.out.println("ACC " + acc + ": " + result.affected().size() + " records in " + result.waves().size()
                + " waves, " + KEYS.keysOfRecords(result.affected()).size() + " keys, " + ms + " ms");
        assertFalse(result.affected().isEmpty());
        assertTrue(result.waves().size() <= 3, "ACC -> ASCCP -> ASCC");
    }
}
