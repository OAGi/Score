package org.oagi.score.gateway.http.cache;

import org.oagi.score.gateway.http.change.ChangeFeed;
import org.oagi.score.gateway.http.change.RecordChangeWatchdog;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.oagi.score.gateway.http.common.TestDataSource;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.propagation.AsccPropagationHandler;
import org.oagi.score.gateway.http.api.cc_management.propagation.AsccpPropagationHandler;
import org.oagi.score.gateway.http.api.cc_management.propagation.BccPropagationHandler;
import org.oagi.score.gateway.http.api.cc_management.propagation.BccpPropagationHandler;
import org.oagi.score.gateway.http.api.cc_management.repository.jooq.JooqCcDependencyLookup;
import org.oagi.score.gateway.http.change.propagation.ChangePropagator;
import org.oagi.score.gateway.http.change.propagation.PropagationConsumer;
import org.oagi.score.gateway.http.change.propagation.PropagationEngine;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.cc_management.model.CachedCcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocumentImpl;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Changes the data of the development database for real, the way the application does, and checks that a cached document
 * answers what an eager one does afterwards: that the triggers write the change log, the watchdog reads it, and the cache forgets
 * what changed and no more. Everything it changes it changes back.
 * <pre>
 *   mvn test -Dtest=RecordCacheFreshnessTest -Dscore.freshness=true
 * </pre>
 * Needs the migration that creates record_change_log and its triggers applied to the database.
 */
@EnabledIfSystemProperty(named = "score.freshness", matches = "true")
class RecordCacheFreshnessTest {

    private static final ScoreUser REQUESTER = new ScoreUser(null, "freshness", "freshness", null, false, null);
    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

    private static TestDataSource dataSource;
    private static DSLContext dsl;

    private RecordCacheStatus status;
    private RecordCache cache;
    private CaffeineRecordCache caffeine;
    private org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory redis;
    private String redisPrefix;
    private RecordChangeWatchdog watchdog;
    private RepositoryFactory repositoryFactory;

    private ReleaseId release;
    private AccManifestId accManifest;
    private long accId, asccId, bccId, asccpId, bccpId, dtId, ownerId, libraryId, releaseId, namespaceId, dtManifestId, dtAwdPriId, xbtId, codeListManifestId, agencyIdListManifestId;

    @BeforeAll
    static void connect() {
        dataSource = TestDataSource.open(6);
        dsl = DSL.using(dataSource, SQLDialect.MARIADB);
    }

    @AfterAll
    static void disconnect() {
        dataSource.close();
    }

    @BeforeEach
    void setUp() {
        status = new RecordCacheStatus();
        if ("redis".equals(System.getProperty("score.freshness.cache"))) {
            redis = new org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory(
                    new org.springframework.data.redis.connection.RedisStandaloneConfiguration(
                            System.getProperty("score.redis.host", "localhost"), Integer.getInteger("score.redis.port", 6379)));
            redis.afterPropertiesSet();
            redisPrefix = "{zz-cc-freshness:" + UUID.randomUUID() + "}:";
            cache = new RedisRecordCache(redis, redisPrefix, status, java.time.Duration.ofMinutes(5));
        } else {
            caffeine = new CaffeineRecordCache("", status);
            cache = caffeine;
        }
        watchdog = ChangeFeed.watchdog(new RepositoryFactory(dsl), List.of(TestRecordKeys.resolver(new RepositoryFactory(dsl).affectedRecordsRepository())), cache, status);
        watchdog.catchUp();
        assertTrue(status.isReady(), "the change log must be readable: is the migration applied?");

        repositoryFactory = new RepositoryFactory(dsl);
        ReflectionTestUtils.setField(repositoryFactory, "recordCache", cache);

        // an ACC with several associations of both kinds
        Record pick = dsl.fetchOne("""
                select m.acc_manifest_id, m.release_id, m.acc_id, r.library_id
                from acc_manifest m join `release` r on r.release_id = m.release_id
                where (select count(*) from ascc_manifest a where a.from_acc_manifest_id = m.acc_manifest_id) > 1
                  and (select count(*) from bcc_manifest b where b.from_acc_manifest_id = m.acc_manifest_id) > 1
                order by m.release_id desc, m.acc_manifest_id limit 1""");
        accManifest = new AccManifestId(pick.get(0, ULong.class).toBigInteger());
        release = new ReleaseId(pick.get(1, ULong.class).toBigInteger());
        releaseId = pick.get(1, Long.class);
        accId = pick.get(2, Long.class);
        libraryId = pick.get(3, Long.class);
        long acc = accManifest.value().longValue();
        asccId = dsl.fetchOne("select ascc_id from ascc_manifest where from_acc_manifest_id = ? order by 1 limit 1", acc).get(0, Long.class);
        bccId = dsl.fetchOne("select bcc_id from bcc_manifest where from_acc_manifest_id = ? order by 1 limit 1", acc).get(0, Long.class);
        asccpId = dsl.fetchOne("""
                select p.asccp_id from ascc_manifest a join asccp_manifest p on p.asccp_manifest_id = a.to_asccp_manifest_id
                where a.from_acc_manifest_id = ? order by 1 limit 1""", acc).get(0, Long.class);
        bccpId = dsl.fetchOne("""
                select p.bccp_id from bcc_manifest b join bccp_manifest p on p.bccp_manifest_id = b.to_bccp_manifest_id
                where b.from_acc_manifest_id = ? order by 1 limit 1""", acc).get(0, Long.class);
        dtId = dsl.fetchOne("""
                select d.dt_id from bcc_manifest b join bccp_manifest p on p.bccp_manifest_id = b.to_bccp_manifest_id
                join dt_manifest d on d.dt_manifest_id = p.bdt_manifest_id
                where b.from_acc_manifest_id = ? order by 1 limit 1""", acc).get(0, Long.class);
        ownerId = dsl.fetchOne("select owner_user_id from acc where acc_id = ?", accId).get(0, Long.class);
        namespaceId = dsl.fetchOne("select namespace_id from acc where acc_id = ?", accId).get(0, Long.class);
        dtManifestId = dsl.fetchOne("""
                select d.dt_manifest_id from bcc_manifest b join bccp_manifest p on p.bccp_manifest_id = b.to_bccp_manifest_id
                join dt_manifest d on d.dt_manifest_id = p.bdt_manifest_id
                where b.from_acc_manifest_id = ? order by 1 limit 1""", acc).get(0, Long.class);
        Record awd = dsl.fetchOne("""
                select p.dt_awd_pri_id, x.xbt_id from dt_awd_pri p join dt_manifest d on d.dt_id = p.dt_id and d.release_id = p.release_id
                join xbt_manifest x on x.xbt_manifest_id = p.xbt_manifest_id
                where d.dt_manifest_id = ? order by 1 limit 1""", dtManifestId);
        codeListManifestId = dsl.fetchOne("""
                select min(m.code_list_manifest_id) from code_list_manifest m where m.release_id = ?
                and exists (select 1 from code_list_value_manifest v where v.code_list_manifest_id = m.code_list_manifest_id)""", releaseId).get(0, Long.class);
        agencyIdListManifestId = dsl.fetchOne("""
                select min(m.agency_id_list_manifest_id) from agency_id_list_manifest m where m.release_id = ?
                and exists (select 1 from agency_id_list_value_manifest v where v.agency_id_list_manifest_id = m.agency_id_list_manifest_id)""", releaseId).get(0, Long.class);
        dtAwdPriId = awd.get(0, Long.class);
        xbtId = awd.get(1, Long.class);
    }

    @org.junit.jupiter.api.AfterEach
    void removeWhatWasLeftInRedis() {
        if (redis != null) {
            try (var connection = redis.getConnection()) {
                Set<byte[]> keys = connection.keyCommands().keys((redisPrefix + "*").getBytes());
                if (keys != null && !keys.isEmpty()) {
                    connection.keyCommands().del(keys.toArray(new byte[0][]));
                }
            }
            redis.destroy();
        }
    }

    /** The keys that are in the cache now. */
    private Set<String> cachedKeys() {
        if (caffeine != null) {
            return caffeine.keys();
        }
        try (var connection = redis.getConnection()) {
            Set<byte[]> keys = connection.keyCommands().keys((redisPrefix + "*").getBytes());
            Set<String> out = new TreeSet<>();
            if (keys != null) {
                for (byte[] key : keys) {
                    String name = new String(key).substring(redisPrefix.length());
                    if (!name.equals("epoch")) {
                        out.add(name);
                    }
                }
            }
            return out;
        }
    }

    // ---- the keys a change must forget, written out ----------------------------------------------------------------

    private static Set<String> parentLists(Long acc) {
        return (acc == null) ? Set.of() : Set.of("ascc-from-acc:" + acc, "bcc-from-acc:" + acc, "seq-key-from-acc:" + acc);
    }

    /** The keys under which a record and what is listed under it are cached. */
    private Set<String> keysOf(String table, long id, Long parent) {
        Set<String> keys = new TreeSet<>();
        switch (table) {
            case "acc" -> keys.addAll(Set.of("acc:" + id, "tags:acc:" + id, "ascc-from-acc:" + id, "bcc-from-acc:" + id, "seq-key-from-acc:" + id));
            case "ascc" -> {
                keys.add("ascc:" + id);
                keys.addAll(parentLists(parent));
            }
            case "bcc" -> {
                keys.add("bcc:" + id);
                keys.addAll(parentLists(parent));
            }
            case "asccp" -> keys.addAll(Set.of("asccp:" + id, "tags:asccp:" + id));
            case "bccp" -> keys.addAll(Set.of("bccp:" + id, "tags:bccp:" + id));
            case "dt" -> {
                keys.addAll(Set.of("dt:" + id, "tags:dt:" + id, "dt_sc-of-dt:" + id, "dt-awd-pri:" + id));
                // the data types of the release are cached as a list
                Record release = dsl.fetchOne("select release_id from dt_manifest where dt_manifest_id = ?", id);
                if (release != null) {
                    keys.add("dts:" + release.get(0, Long.class));
                }
            }
            case "dt_sc" -> {
                keys.add("dt_sc:" + id);
                keys.add("dt_sc-awd-pri:" + id);
                if (parent != null) {
                    keys.add("dt_sc-of-dt:" + parent);
                }
            }
            default -> throw new IllegalArgumentException(table);
        }
        return keys;
    }

    private static final Map<String, String> PARENT_COLUMN = Map.of("ascc", "from_acc_manifest_id", "bcc", "from_acc_manifest_id",
            "dt_sc", "owner_dt_manifest_id");

    /** The keys of every record that the SQL fragment (after "from X_manifest m") selects, of all seven kinds. */
    private Set<String> keysWhere(java.util.function.Function<String, String> fragment, Object... bindings) {
        Set<String> keys = new TreeSet<>();
        for (String table : List.of("acc", "ascc", "bcc", "asccp", "bccp", "dt", "dt_sc")) {
            String parent = PARENT_COLUMN.containsKey(table) ? "m." + PARENT_COLUMN.get(table) : "null";
            for (Record r : dsl.fetch("select m." + table + "_manifest_id, " + parent + " from " + table + "_manifest m " + fragment.apply(table), bindings)) {
                keys.addAll(keysOf(table, r.get(0, Long.class), r.get(1, Long.class)));
            }
        }
        // the XBTs are cached as a list per release
        for (Record r : dsl.fetch("select distinct m.release_id from xbt_manifest m " + fragment.apply("xbt"), bindings)) {
            keys.add("xbts:" + r.get(0, Long.class));
        }
        // the code lists and the agency identification lists are cached one by one
        for (Record r : dsl.fetch("select m.code_list_manifest_id from code_list_manifest m " + fragment.apply("code_list"), bindings)) {
            keys.add("code-list:" + r.get(0, Long.class));
        }
        for (Record r : dsl.fetch("select m.agency_id_list_manifest_id from agency_id_list_manifest m " + fragment.apply("agency_id_list"), bindings)) {
            keys.add("agency-id-list:" + r.get(0, Long.class));
        }
        return keys;
    }

    // ---- what a document answers ---------------------------------------------------------------------------------

    private String answers(CcDocument document) throws Exception {
        Map<String, Object> out = new LinkedHashMap<>();
        AccSummaryRecord acc = document.getAcc(accManifest);
        out.put("acc", acc);
        out.put("namespace", document.getNamespace(acc.namespaceId()));
        out.put("code list", document.getCodeList(new org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId(BigInteger.valueOf(codeListManifestId))));
        out.put("agency id list", document.getAgencyIdList(new org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId(BigInteger.valueOf(agencyIdListManifestId))));
        out.put("tags", document.getTagListByAccManifestId(accManifest));
        List<AsccSummaryRecord> asccs = document.getAsccListByFromAccManifestId(accManifest);
        List<BccSummaryRecord> bccs = document.getBccListByFromAccManifestId(accManifest);
        out.put("asccs", asccs);
        out.put("bccs", bccs);
        out.put("associations", document.getAssociations(acc));
        for (AsccSummaryRecord ascc : asccs) {
            var asccp = document.getAsccp(ascc.toAsccpManifestId());
            out.put("asccp " + ascc.toAsccpManifestId().value(), asccp);
            out.put("asccp tags " + ascc.toAsccpManifestId().value(), document.getTagListByAsccpManifestId(ascc.toAsccpManifestId()));
        }
        for (BccSummaryRecord bcc : bccs) {
            var bccp = document.getBccp(bcc.toBccpManifestId());
            out.put("bccp " + bcc.toBccpManifestId().value(), bccp);
            if (bccp != null) {
                var dt = document.getDt(bccp.dtManifestId());
                out.put("dt " + bccp.dtManifestId().value(), dt);
                out.put("dt tags " + bccp.dtManifestId().value(), document.getTagListByDtManifestId(bccp.dtManifestId()));
                out.put("dt_sc " + bccp.dtManifestId().value(), document.getDtScList(bccp.dtManifestId()));
                var awdPri = document.getDtAwdPriList(bccp.dtManifestId());
                out.put("dt awd pri " + bccp.dtManifestId().value(), awdPri);
                for (var a : awdPri) {
                    if (a.xbtManifestId() != null) {
                        out.put("xbt " + a.xbtManifestId().value(), document.getXbt(a.xbtManifestId()));
                    }
                }
                for (var sc : document.getDtScList(bccp.dtManifestId())) {
                    out.put("dt_sc awd pri " + sc.dtScManifestId().value(), document.getDtScAwdPriList(sc.dtScManifestId()));
                }
            }
        }
        return JSON.writeValueAsString(out);
    }

    private String cachedAnswers() throws Exception {
        return answers(new CachedCcDocument(REQUESTER, repositoryFactory, release));
    }

    private String eagerAnswers() throws Exception {
        return answers(new CcDocumentImpl(REQUESTER, repositoryFactory, release));
    }

    // ---- a change, and back ----------------------------------------------------------------------------------------

    /**
     * Fills the cache, makes the change, lets the watchdog read, and checks the cached document against an eager one; then the same
     * for the change undone.
     *
     * @param visible whether the answers must differ from before (the change is in a field the records carry)
     */
    private void check(String what, boolean visible, Set<String> mustForget, String change, String undo, Object... bindings) throws Exception {
        String before = cachedAnswers();
        assertEquals(eagerAnswers(), before, what + ": before the change");
        assertEquals(before, cachedAnswers(), what + ": a second read from the cache");
        Set<String> cachedBefore = cachedKeys();
        Set<String> expectedForgotten = new TreeSet<>(mustForget);
        expectedForgotten.retainAll(cachedBefore);
        assertFalse(expectedForgotten.isEmpty(), what + ": none of the keys the change must forget is in the cache, so the check proves nothing: " + mustForget);

        dsl.execute(change, bindings);
        try {
            watchdog.catchUp();
            Set<String> cachedAfter = cachedKeys();
            Set<String> forgotten = new TreeSet<>(cachedBefore);
            forgotten.removeAll(cachedAfter);
            assertEquals(expectedForgotten, forgotten, what + ": the keys that were forgotten");
            assertTrue(cachedBefore.containsAll(cachedAfter), what + ": nothing was added by the watchdog");

            String eager = eagerAnswers();
            assertEquals(eager, cachedAnswers(), what + ": after the change");
            if (visible) {
                assertNotEquals(before, eager, what + ": the change must be visible in the answers, or the check proves nothing");
            }
        } finally {
            dsl.execute(undo, bindings);
        }
        watchdog.catchUp();
        assertEquals(before, cachedAnswers(), what + ": after the change was undone");
        assertEquals(eagerAnswers(), cachedAnswers(), what + ": after the change was undone, as eager");
    }

    private Set<String> manifestKeys(String table, String baseId, long id) {
        String parent = PARENT_COLUMN.containsKey(table) ? PARENT_COLUMN.get(table) : "null";
        Set<String> keys = new TreeSet<>();
        for (Record r : dsl.fetch("select " + table + "_manifest_id, " + parent + " from " + table + "_manifest where " + baseId + " = ?", id)) {
            keys.addAll(keysOf(table, r.get(0, Long.class), r.get(1, Long.class)));
        }
        return keys;
    }

    @Test
    void aChangeOfTheComponentTablesIsSeenAtOnce() throws Exception {
        check("acc", true, manifestKeys("acc", "acc_id", accId), "update acc set object_class_term = concat(object_class_term, 'X') where acc_id = ?",
                "update acc set object_class_term = left(object_class_term, char_length(object_class_term) - 1) where acc_id = ?", accId);
        check("acc_manifest", true, keysOf("acc", accManifest.value().longValue(), null), "update acc_manifest set den = concat(den, 'X') where acc_manifest_id = ?",
                "update acc_manifest set den = left(den, char_length(den) - 1) where acc_manifest_id = ?", accManifest.value().longValue());
        check("ascc", true, manifestKeys("ascc", "ascc_id", asccId), "update ascc set cardinality_max = cardinality_max + 1 where ascc_id = ?",
                "update ascc set cardinality_max = cardinality_max - 1 where ascc_id = ?", asccId);
        check("bcc", true, manifestKeys("bcc", "bcc_id", bccId), "update bcc set cardinality_max = cardinality_max + 1 where bcc_id = ?",
                "update bcc set cardinality_max = cardinality_max - 1 where bcc_id = ?", bccId);
        check("asccp", true, manifestKeys("asccp", "asccp_id", asccpId), "update asccp set property_term = concat(property_term, 'X') where asccp_id = ?",
                "update asccp set property_term = left(property_term, char_length(property_term) - 1) where asccp_id = ?", asccpId);
        check("bccp", true, manifestKeys("bccp", "bccp_id", bccpId), "update bccp set property_term = concat(property_term, 'X') where bccp_id = ?",
                "update bccp set property_term = left(property_term, char_length(property_term) - 1) where bccp_id = ?", bccpId);
        check("dt", true, manifestKeys("dt", "dt_id", dtId), "update dt set qualifier = concat(ifnull(qualifier, ''), 'X') where dt_id = ?",
                "update dt set qualifier = nullif(left(qualifier, char_length(qualifier) - 1), '') where dt_id = ?", dtId);
    }

    @Test
    void aChangeOfANamespaceOfAnXbtOrOfTheAllowedPrimitivesIsSeenAtOnce() throws Exception {
        check("namespace", true, Set.of("namespace:" + namespaceId), "update namespace set uri = concat(uri, 'x') where namespace_id = ?",
                "update namespace set uri = left(uri, char_length(uri) - 1) where namespace_id = ?", namespaceId);

        Set<String> dtKeys = new TreeSet<>(manifestKeys("dt", "dt_id", dsl.fetchOne("select dt_id from dt_manifest where dt_manifest_id = ?", dtManifestId).get(0, Long.class)));
        dtKeys.removeIf(k -> !k.startsWith("dt-awd-pri:") && !k.startsWith("dts:") && !k.startsWith("dt:") && !k.startsWith("tags:dt:") && !k.startsWith("dt_sc-of-dt:"));
        check("dt_awd_pri", true, dtKeys, "update dt_awd_pri set is_default = 1 - is_default where dt_awd_pri_id = ?",
                "update dt_awd_pri set is_default = 1 - is_default where dt_awd_pri_id = ?", dtAwdPriId);

        // an XBT: the list of its release and the data types (and their supplementary components) whose primitives show its name
        Set<String> xbtKeys = new TreeSet<>();
        for (Record r : dsl.fetch("select release_id from xbt_manifest where xbt_id = ?", xbtId)) {
            xbtKeys.add("xbts:" + r.get(0, Long.class));
        }
        for (Record r : dsl.fetch("""
                select distinct d.dt_manifest_id from dt_awd_pri p join xbt_manifest x on x.xbt_manifest_id = p.xbt_manifest_id
                join dt_manifest d on d.dt_id = p.dt_id and d.release_id = p.release_id where x.xbt_id = ?""", xbtId)) {
            xbtKeys.addAll(keysOf("dt", r.get(0, Long.class), null));
            xbtKeys.add("dt-awd-pri:" + r.get(0, Long.class));
        }
        xbtKeys.removeIf(k -> k.startsWith("dts:"));
        for (Record r : dsl.fetch("""
                select distinct m.dt_sc_manifest_id, m.owner_dt_manifest_id from dt_sc_awd_pri p join xbt_manifest x on x.xbt_manifest_id = p.xbt_manifest_id
                join dt_sc_manifest m on m.dt_sc_id = p.dt_sc_id and m.release_id = p.release_id where x.xbt_id = ?""", xbtId)) {
            xbtKeys.addAll(keysOf("dt_sc", r.get(0, Long.class), r.get(1, Long.class)));
            xbtKeys.add("dt_sc-awd-pri:" + r.get(0, Long.class));
        }
        check("xbt", true, xbtKeys, "update xbt set name = concat(name, 'x') where xbt_id = ?",
                "update xbt set name = left(name, char_length(name) - 1) where xbt_id = ?", xbtId);
    }

    /** The data types (and supplementary components) whose allowed primitives refer to these manifests, and their primitives' keys. */
    private Set<String> primitivesShowing(String column, Collection<Long> manifestIds) {
        Set<String> keys = new TreeSet<>();
        String in = manifestIds.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
        for (Record r : dsl.fetch("select distinct d.dt_manifest_id from dt_awd_pri p join dt_manifest d on d.dt_id = p.dt_id and d.release_id = p.release_id"
                + " where p." + column + " in (" + in + ")")) {
            keys.addAll(keysOf("dt", r.get(0, Long.class), null));
        }
        keys.removeIf(k -> k.startsWith("dts:"));   // what a change of a name reaches through the primitives is not the list of the release
        for (Record r : dsl.fetch("select distinct m.dt_sc_manifest_id, m.owner_dt_manifest_id from dt_sc_awd_pri p join dt_sc_manifest m on m.dt_sc_id = p.dt_sc_id"
                + " and m.release_id = p.release_id where p." + column + " in (" + in + ")")) {
            keys.addAll(keysOf("dt_sc", r.get(0, Long.class), r.get(1, Long.class)));
        }
        return keys;
    }

    @Test
    void aChangeOfACodeListOrAnAgencyIdListAndOfTheirValuesIsSeenAtOnce() throws Exception {
        // the code list: its name
        long codeListId = dsl.fetchOne("select code_list_id from code_list_manifest where code_list_manifest_id = ?", codeListManifestId).get(0, Long.class);
        List<Long> listManifests = dsl.fetch("select code_list_manifest_id from code_list_manifest where code_list_id = ?", codeListId).getValues(0, Long.class);
        Set<String> keys = new TreeSet<>(primitivesShowing("code_list_manifest_id", listManifests));
        for (Long m : listManifests) {
            keys.add("code-list:" + m);
        }
        check("code list", true, keys, "update code_list set name = concat(name, 'x') where code_list_id = ?",
                "update code_list set name = left(name, char_length(name) - 1) where code_list_id = ?", codeListId);

        // one of its values
        long valueId = dsl.fetchOne("select code_list_value_id from code_list_value_manifest where code_list_manifest_id = ? order by 1 limit 1", codeListManifestId).get(0, Long.class);
        listManifests = dsl.fetch("select m.code_list_manifest_id from code_list_value_manifest vm join code_list_manifest m on m.code_list_manifest_id = vm.code_list_manifest_id"
                + " where vm.code_list_value_id = ?", valueId).getValues(0, Long.class);
        keys = new TreeSet<>(primitivesShowing("code_list_manifest_id", listManifests));
        for (Long m : listManifests) {
            keys.add("code-list:" + m);
        }
        check("code list value", true, keys, "update code_list_value set value = concat(value, 'x') where code_list_value_id = ?",
                "update code_list_value set value = left(value, char_length(value) - 1) where code_list_value_id = ?", valueId);

        // the agency identification list: its name
        long agencyId = dsl.fetchOne("select agency_id_list_id from agency_id_list_manifest where agency_id_list_manifest_id = ?", agencyIdListManifestId).get(0, Long.class);
        List<Long> agencyManifests = dsl.fetch("select agency_id_list_manifest_id from agency_id_list_manifest where agency_id_list_id = ?", agencyId).getValues(0, Long.class);
        keys = new TreeSet<>(primitivesShowing("agency_id_list_manifest_id", agencyManifests));
        for (Long m : agencyManifests) {
            keys.add("agency-id-list:" + m);
        }
        check("agency id list", true, keys, "update agency_id_list set name = concat(name, 'x') where agency_id_list_id = ?",
                "update agency_id_list set name = left(name, char_length(name) - 1) where agency_id_list_id = ?", agencyId);

        // one of its values
        long agencyValueId = dsl.fetchOne("select agency_id_list_value_id from agency_id_list_value_manifest where agency_id_list_manifest_id = ? order by 1 limit 1", agencyIdListManifestId).get(0, Long.class);
        agencyManifests = dsl.fetch("select m.agency_id_list_manifest_id from agency_id_list_manifest m where m.agency_id_list_manifest_id in"
                + " (select vm.agency_id_list_manifest_id from agency_id_list_value_manifest vm where vm.agency_id_list_value_id = ?)"
                + " or m.agency_id_list_value_manifest_id in (select vm.agency_id_list_value_manifest_id from agency_id_list_value_manifest vm where vm.agency_id_list_value_id = ?)",
                agencyValueId, agencyValueId).getValues(0, Long.class);
        keys = new TreeSet<>(primitivesShowing("agency_id_list_manifest_id", agencyManifests));
        for (Long m : agencyManifests) {
            keys.add("agency-id-list:" + m);
        }
        check("agency id list value", true, keys, "update agency_id_list_value set name = concat(name, 'x') where agency_id_list_value_id = ?",
                "update agency_id_list_value set name = left(name, char_length(name) - 1) where agency_id_list_value_id = ?", agencyValueId);
    }

    @Test
    void aChangeOfWhatTheRecordsAreJoinedWithIsSeenAtOnce() throws Exception {
        check("owner", true, keysWhere(t -> "join `" + t + "` b on b." + t + "_id = m." + t + "_id where b.owner_user_id = " + ownerId
                + " or b.created_by = " + ownerId + " or b.last_updated_by = " + ownerId), "update app_user set is_developer = 1 - is_developer where app_user_id = ?",
                "update app_user set is_developer = 1 - is_developer where app_user_id = ?", ownerId);
        check("release", true, keysWhere(t -> "where m.release_id = " + releaseId), "update `release` set release_num = concat(release_num, 'X') where release_id = ?",
                "update `release` set release_num = left(release_num, char_length(release_num) - 1) where release_id = ?", releaseId);
        check("library", false, keysWhere(t -> "join `release` r on r.release_id = m.release_id where r.library_id = " + libraryId), "update library set name = concat(name, 'X') where library_id = ?",
                "update library set name = left(name, char_length(name) - 1) where library_id = ?", libraryId);
    }

    @Test
    void aTagAddedAndRemovedIsSeenAtOnce() throws Exception {
        long tagId = dsl.fetchOne("select min(tag_id) from tag").get(0, Long.class);
        long acc = accManifest.value().longValue();
        dsl.execute("delete from acc_manifest_tag where acc_manifest_id = ? and tag_id = ?", acc, tagId);
        check("tag on acc", true, keysOf("acc", acc, null),
                "insert into acc_manifest_tag (acc_manifest_id, tag_id, created_by, creation_timestamp) values (?, ?, " + ownerId + ", now())",
                "delete from acc_manifest_tag where acc_manifest_id = ? and tag_id = ?", acc, tagId);
    }

    @Test
    void aRecordThatIsNotInTheComponentsGraphIsLeftInTheCache() throws Exception {
        String before = cachedAnswers();
        long other = dsl.fetchOne("select min(acc_manifest_id) from acc_manifest where acc_manifest_id <> ? and release_id = ?",
                accManifest.value().longValue(), releaseId).get(0, Long.class);
        assertNotNull(cache.get("acc:" + accManifest.value()), "the ACC is cached");

        dsl.execute("update acc_manifest set den = den where acc_manifest_id = ?", other);
        watchdog.catchUp();

        assertNotNull(cache.get("acc:" + accManifest.value()), "a change of another record does not empty the cache");
        assertEquals(before, cachedAnswers());
    }

    @Test
    void aChangeOfATransactionThatCommitsLaterIsNotMissed() throws Exception {
        String before = cachedAnswers();
        long acc = accManifest.value().longValue();
        long other = dsl.fetchOne("select min(acc_manifest_id) from acc_manifest where acc_manifest_id <> ? and release_id = ?", acc, releaseId).get(0, Long.class);
        try (Connection slow = dataSource.getConnection()) {
            slow.setAutoCommit(false);
            try (Statement statement = slow.createStatement()) {
                statement.executeUpdate("update acc_manifest set den = concat(den, 'X') where acc_manifest_id = " + acc);
            }
            // a later transaction commits first
            dsl.execute("update acc_manifest set den = den where acc_manifest_id = ?", other);
            watchdog.catchUp();
            assertNotNull(cache.get("acc:" + acc), "the change of the open transaction is not visible yet");

            slow.commit();
            try {
                long deadline = System.nanoTime() + 10_000_000_000L;
                while (cache.get("acc:" + acc) != null || !cachedAnswers().equals(eagerAnswers())) {
                    assertTrue(System.nanoTime() < deadline, "the change that became visible late was missed");
                    Thread.sleep(200);
                    watchdog.catchUp();
                }
            } finally {
                dsl.execute("update acc_manifest set den = left(den, char_length(den) - 1) where acc_manifest_id = ?", acc);
            }
        }
        watchdog.catchUp();
        assertEquals(before, cachedAnswers());
    }

    // ---- what a change reaches by the dependencies between records -------------------------------------------------

    /** Forgets the records that depend on the ones that changed, too: the watchdog hands every batch to the propagation. */
    private void usePropagation() {
        DefaultListableBeanFactory beans = new DefaultListableBeanFactory();
        beans.registerSingleton("cache", cache);
        beans.registerSingleton("consumer", new CacheInvalidationConsumer(beans.getBeanProvider(RecordCache.class), TestRecordKeys.resolver(new RepositoryFactory(dsl).affectedRecordsRepository()), new RecordCacheProperties()));
        JooqCcDependencyLookup lookup = new JooqCcDependencyLookup(dsl, 500);
        PropagationEngine engine = new PropagationEngine(List.of(new AsccpPropagationHandler(lookup), new AsccPropagationHandler(lookup),
                new BccpPropagationHandler(lookup), new BccPropagationHandler(lookup)));
        ChangePropagator propagator = new ChangePropagator(engine, beans.getBeanProvider(PropagationConsumer.class));
        watchdog = ChangeFeed.watchdog(new RepositoryFactory(dsl), List.of(TestRecordKeys.resolver(new RepositoryFactory(dsl).affectedRecordsRepository())),
                propagator, cache, status);
        watchdog.catchUp();
        assertTrue(status.isReady());
    }

    /**
     * The keys of the records that derive a value from the records of {@code table} with these manifest ids, found with SQL of
     * its own (not with the lookups the propagation uses): the dependents, and theirs in turn.
     */
    private Set<String> derivedKeys(String table, Collection<Long> sources) {
        Set<String> seen = new HashSet<>();
        sources.forEach(id -> seen.add(table + ":" + id));
        Deque<String> queue = new ArrayDeque<>(seen);
        Set<String> keys = new TreeSet<>();
        while (!queue.isEmpty()) {
            String[] node = queue.poll().split(":");
            String type = node[0];
            long id = Long.parseLong(node[1]);
            switch (type) {
                case "acc" -> {
                    reach(queue, seen, keys, "asccp", "select asccp_manifest_id, null from asccp_manifest where role_of_acc_manifest_id = ?", id);
                    reach(queue, seen, keys, "ascc", "select ascc_manifest_id, from_acc_manifest_id from ascc_manifest where from_acc_manifest_id = ?", id);
                    reach(queue, seen, keys, "bcc", "select bcc_manifest_id, from_acc_manifest_id from bcc_manifest where from_acc_manifest_id = ?", id);
                }
                case "asccp" -> reach(queue, seen, keys, "ascc", "select ascc_manifest_id, from_acc_manifest_id from ascc_manifest where to_asccp_manifest_id = ?", id);
                case "dt" -> {
                    reach(queue, seen, keys, "bccp", "select bccp_manifest_id, null from bccp_manifest where bdt_manifest_id = ?", id);
                }
                case "bccp" -> reach(queue, seen, keys, "bcc", "select bcc_manifest_id, from_acc_manifest_id from bcc_manifest where to_bccp_manifest_id = ?", id);
                default -> {
                }
            }
        }
        return keys;
    }

    private void reach(Deque<String> queue, Set<String> seen, Set<String> keys, String table, String sql, long source) {
        for (Record r : dsl.fetch(sql, source)) {
            long id = r.get(0, Long.class);
            if (seen.add(table + ":" + id)) {
                queue.add(table + ":" + id);
                keys.addAll(keysOf(table, id, r.get(1, Long.class)));
            }
        }
    }

    private List<Long> manifestsOf(String table, String baseId, long id) {
        return dsl.fetch("select " + table + "_manifest_id from " + table + "_manifest where " + baseId + " = ?", id).getValues(0, Long.class);
    }

    private static Set<String> union(Set<String> a, Set<String> b) {
        Set<String> all = new TreeSet<>(a);
        all.addAll(b);
        return all;
    }

    @Test
    void aChangeOfAnAccTermForgetsWhatIsDerivedFromItToo() throws Exception {
        usePropagation();
        Set<String> derived = derivedKeys("acc", manifestsOf("acc", "acc_id", accId));
        assertTrue(derived.stream().anyMatch(k -> k.startsWith("ascc:")), "the ACC has ASCCs: " + derived);

        check("acc term, with the propagation", true, union(manifestKeys("acc", "acc_id", accId), derived),
                "update acc set object_class_term = concat(object_class_term, 'X') where acc_id = ?",
                "update acc set object_class_term = left(object_class_term, char_length(object_class_term) - 1) where acc_id = ?", accId);
    }

    @Test
    void aChangeOfAnAsccpTermForgetsTheAsccsThatPointToItToo() throws Exception {
        usePropagation();
        Set<String> derived = derivedKeys("asccp", manifestsOf("asccp", "asccp_id", asccpId));
        assertTrue(derived.stream().anyMatch(k -> k.startsWith("ascc:")), "an ASCC points to the ASCCP: " + derived);

        check("asccp term, with the propagation", true, union(manifestKeys("asccp", "asccp_id", asccpId), derived),
                "update asccp set property_term = concat(property_term, 'X') where asccp_id = ?",
                "update asccp set property_term = left(property_term, char_length(property_term) - 1) where asccp_id = ?", asccpId);
    }

    @Test
    void aChangeOfADataTypeForgetsTheBccpsAndBccsThatShowItToo() throws Exception {
        usePropagation();
        Set<String> derived = derivedKeys("dt", manifestsOf("dt", "dt_id", dtId));
        assertTrue(derived.stream().anyMatch(k -> k.startsWith("bccp:")), "a BCCP has the data type: " + derived);

        check("dt qualifier, with the propagation", true, union(manifestKeys("dt", "dt_id", dtId), derived),
                "update dt set qualifier = concat(ifnull(qualifier, ''), 'X') where dt_id = ?",
                "update dt set qualifier = nullif(left(qualifier, char_length(qualifier) - 1), '') where dt_id = ?", dtId);
    }

    @Test
    void withoutThePropagationOnlyWhatChangedIsForgotten() throws Exception {
        // the watchdog of setUp has no propagation: the same change forgets the ACC and its parent lists, and nothing derived
        Set<String> derived = derivedKeys("acc", manifestsOf("acc", "acc_id", accId));
        derived.removeAll(manifestKeys("acc", "acc_id", accId));
        check("acc term, without the propagation", true, manifestKeys("acc", "acc_id", accId),
                "update acc set object_class_term = concat(object_class_term, 'X') where acc_id = ?",
                "update acc set object_class_term = left(object_class_term, char_length(object_class_term) - 1) where acc_id = ?", accId);
        assertFalse(derived.isEmpty());
    }

    @Test
    void aChangeThatIsNotInAColumnADenIsMadeOfDoesNotReachTheDependents() throws Exception {
        usePropagation();
        Set<String> derived = derivedKeys("acc", manifestsOf("acc", "acc_id", accId));
        derived.removeAll(manifestKeys("acc", "acc_id", accId));
        assertTrue(derived.stream().anyMatch(k -> k.startsWith("ascc:")), "the ACC has dependents that must be left alone: " + derived);

        // the definition of the ACC is not part of the DEN of anything: only the ACC (and the lists of its parents) is forgotten
        check("acc definition, with the propagation", true, manifestKeys("acc", "acc_id", accId),
                "update acc set definition = concat(ifnull(definition, ''), 'X') where acc_id = ?",
                "update acc set definition = nullif(left(definition, char_length(definition) - 1), '') where acc_id = ?", accId);
        check("asccp definition, with the propagation", true, manifestKeys("asccp", "asccp_id", asccpId),
                "update asccp set definition = concat(ifnull(definition, ''), 'X') where asccp_id = ?",
                "update asccp set definition = nullif(left(definition, char_length(definition) - 1), '') where asccp_id = ?", asccpId);
        check("dt definition, with the propagation", true, manifestKeys("dt", "dt_id", dtId),
                "update dt set definition = concat(ifnull(definition, ''), 'X') where dt_id = ?",
                "update dt set definition = nullif(left(definition, char_length(definition) - 1), '') where dt_id = ?", dtId);
    }
}
