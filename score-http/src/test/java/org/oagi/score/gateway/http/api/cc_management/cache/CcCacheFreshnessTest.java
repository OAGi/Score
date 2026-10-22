package org.oagi.score.gateway.http.api.cc_management.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
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
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Changes the data of the development database for real, the way the application does, and checks that a cached document
 * answers what an eager one does afterwards: that the triggers write the change log, the feed reads it, and the cache forgets
 * what changed and no more. Everything it changes it changes back.
 * <pre>
 *   mvn test -Dtest=CcCacheFreshnessTest -Dscore.freshness=true
 * </pre>
 * Needs the migration that creates cc_change_log and its triggers applied to the database.
 */
@EnabledIfSystemProperty(named = "score.freshness", matches = "true")
class CcCacheFreshnessTest {

    private static final ScoreUser REQUESTER = new ScoreUser(null, "freshness", "freshness", null, false, null);
    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

    private static HikariDataSource dataSource;
    private static DSLContext dsl;

    private CcCacheStatus status;
    private CcCache cache;
    private ConcurrentMapCacheManager springCaches;
    private org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory redis;
    private String redisPrefix;
    private CcChangeFeed feed;
    private RepositoryFactory repositoryFactory;

    private ReleaseId release;
    private AccManifestId accManifest;
    private long accId, asccId, bccId, asccpId, bccpId, dtId, ownerId, libraryId, releaseId;

    @BeforeAll
    static void connect() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(System.getProperty("score.db.url", "jdbc:mariadb://localhost:3306/oagi?useSSL=false"));
        config.setUsername(System.getProperty("score.db.username", "oagi"));
        config.setPassword(System.getProperty("score.db.password", "oagi"));
        config.setMaximumPoolSize(6);
        dataSource = new HikariDataSource(config);
        dsl = DSL.using(dataSource, SQLDialect.MARIADB);
    }

    @AfterAll
    static void disconnect() {
        dataSource.close();
    }

    @BeforeEach
    void setUp() {
        status = new CcCacheStatus();
        if ("redis".equals(System.getProperty("score.freshness.cache"))) {
            redis = new org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory(
                    new org.springframework.data.redis.connection.RedisStandaloneConfiguration(
                            System.getProperty("score.redis.host", "localhost"), Integer.getInteger("score.redis.port", 6379)));
            redis.afterPropertiesSet();
            redisPrefix = "{zz-cc-freshness:" + UUID.randomUUID() + "}:";
            cache = new RedisCcCache(redis, redisPrefix, status, java.time.Duration.ofMinutes(5));
        } else {
            springCaches = new ConcurrentMapCacheManager();
            cache = new SpringCcCache(springCaches, status);
        }
        feed = new CcChangeFeed(new RepositoryFactory(dsl), cache, status);
        feed.poll();
        assertTrue(status.isReady(), "the change log must be readable: is the migration applied?");

        repositoryFactory = new RepositoryFactory(dsl);
        ReflectionTestUtils.setField(repositoryFactory, "ccCache", cache);

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
        if (springCaches != null) {
            org.springframework.cache.concurrent.ConcurrentMapCache c =
                    (org.springframework.cache.concurrent.ConcurrentMapCache) springCaches.getCache(SpringCcCache.CACHE_NAME);
            return c.getNativeCache().keySet().stream().map(Object::toString).collect(java.util.stream.Collectors.toCollection(TreeSet::new));
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
    private static Set<String> keysOf(String table, long id, Long parent) {
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
            case "dt" -> keys.addAll(Set.of("dt:" + id, "tags:dt:" + id, "dt_sc-of-dt:" + id));
            case "dt_sc" -> {
                keys.add("dt_sc:" + id);
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
        return keys;
    }

    // ---- what a document answers ---------------------------------------------------------------------------------

    private String answers(CcDocument document) throws Exception {
        Map<String, Object> out = new LinkedHashMap<>();
        AccSummaryRecord acc = document.getAcc(accManifest);
        out.put("acc", acc);
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
     * Fills the cache, makes the change, lets the feed read, and checks the cached document against an eager one; then the same
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
            feed.catchUp();
            Set<String> cachedAfter = cachedKeys();
            Set<String> forgotten = new TreeSet<>(cachedBefore);
            forgotten.removeAll(cachedAfter);
            assertEquals(expectedForgotten, forgotten, what + ": the keys that were forgotten");
            assertTrue(cachedBefore.containsAll(cachedAfter), what + ": nothing was added by the feed");

            String eager = eagerAnswers();
            assertEquals(eager, cachedAnswers(), what + ": after the change");
            if (visible) {
                assertNotEquals(before, eager, what + ": the change must be visible in the answers, or the check proves nothing");
            }
        } finally {
            dsl.execute(undo, bindings);
        }
        feed.catchUp();
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
        feed.catchUp();

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
            feed.catchUp();
            assertNotNull(cache.get("acc:" + acc), "the change of the open transaction is not visible yet");

            slow.commit();
            try {
                long deadline = System.nanoTime() + 10_000_000_000L;
                while (cache.get("acc:" + acc) != null || !cachedAnswers().equals(eagerAnswers())) {
                    assertTrue(System.nanoTime() < deadline, "the change that became visible late was missed");
                    Thread.sleep(200);
                    feed.catchUp();
                }
            } finally {
                dsl.execute("update acc_manifest set den = left(den, char_length(den) - 1) where acc_manifest_id = ?", acc);
            }
        }
        feed.catchUp();
        assertEquals(before, cachedAnswers());
    }
}
