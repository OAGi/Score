package org.oagi.score.gateway.http.api.cc_management.model;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.bie_management.service.BieGenerateService;
import org.oagi.score.gateway.http.api.bie_management.service.BieReadService;
import org.oagi.score.gateway.http.api.bie_management.service.BieVisitor;
import org.oagi.score.gateway.http.cache.RecordCache;
import org.oagi.score.gateway.http.cache.RecordCacheStatus;
import org.oagi.score.gateway.http.cache.CaffeineRecordCache;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.graph.model.FindUsagesRequest;
import org.oagi.score.gateway.http.api.graph.service.GraphService;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseQueryService;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Where a consumer asks the cache for one key at a time, which is what is slow when the cache is in Redis and what costs a database
 * query when the key is not there. A decorator of the cache writes down, for every single {@code get}, the kind of key
 * ({@code acc}, {@code namespace}, {@code dt-awd-pri}...), whether it was there, and the first frame of the call stack that is not
 * the cache or the document: the consumer's own method. Each scenario is run with an empty cache and then again with the cache filled.
 * <pre>
 *   mvn test -Dtest=CcSingleReadAnalysis -DargLine="-Xmx4g -Dscore.singlereads=true"
 * </pre>
 */
@EnabledIfSystemProperty(named = "score.singlereads", matches = "true")
@SpringBootTest(properties = {"logging.level.org.jooq=WARN", "logging.level.org.jooq.tools.LoggerListener=WARN"})
class CcSingleReadAnalysis {

    @Autowired
    private RepositoryFactory repositoryFactory;
    @Autowired
    private DSLContext dsl;
    @Autowired
    private GraphService graphService;
    @Autowired
    private BieReadService bieReadService;
    @Autowired
    private BieGenerateService bieGenerateService;
    @Autowired
    private ReleaseQueryService releaseQueryService;

    private static final StackWalker WALKER = StackWalker.getInstance();

    /** Counts what is asked of the cache. */
    private static final class Observing implements RecordCache {
        final RecordCache inner;
        final Map<String, LongAdder> byKind = new ConcurrentHashMap<>();
        final Map<String, LongAdder> byConsumer = new ConcurrentHashMap<>();
        final Set<String> distinct = ConcurrentHashMap.newKeySet();
        final LongAdder singles = new LongAdder(), hits = new LongAdder(), bulkCalls = new LongAdder(), bulkKeys = new LongAdder();

        Observing(RecordCache inner) {
            this.inner = inner;
        }

        void reset() {
            byKind.clear();
            byConsumer.clear();
            distinct.clear();
            singles.reset();
            hits.reset();
            bulkCalls.reset();
            bulkKeys.reset();
        }

        private static String consumer() {
            return WALKER.walk(frames -> frames
                    .filter(f -> f.getClassName().startsWith("org.oagi.score.gateway.http."))
                    .filter(f -> !f.getClassName().contains(".cc_management.cache.")
                            && !f.getClassName().endsWith("CachedCcDocument")
                            && !f.getClassName().endsWith("CacheFirstCcDocument")
                            && !f.getClassName().contains("CcSingleReadAnalysis")
                            && !f.getClassName().contains("$$"))
                    .findFirst()
                    .map(f -> f.getClassName().substring(f.getClassName().lastIndexOf('.') + 1) + "." + f.getMethodName())
                    .orElse("?"));
        }

        @Override
        public long epoch() {
            return inner.epoch();
        }

        @Override
        public Object get(String key) {
            Object value = inner.get(key);
            String kind = key.substring(0, key.indexOf(':')) + (value != null ? " (hit)" : " (miss)");
            byKind.computeIfAbsent(kind, k -> new LongAdder()).increment();
            byConsumer.computeIfAbsent(consumer() + " <- " + key.substring(0, key.indexOf(':')), k -> new LongAdder()).increment();
            distinct.add(key);
            singles.increment();
            if (value != null) {
                hits.increment();
            }
            return value;
        }

        @Override
        public Map<String, Object> getAll(Collection<String> keys) {
            bulkCalls.increment();
            bulkKeys.add(keys.size());
            return inner.getAll(keys);
        }

        @Override
        public void put(long epoch, String key, Object value) {
            inner.put(epoch, key, value);
        }

        @Override
        public void putAll(long epoch, Map<String, Object> values) {
            inner.putAll(epoch, values);
        }

        @Override
        public void invalidate(Collection<String> keys) {
            inner.invalidate(keys);
        }

        @Override
        public boolean isBypassed() {
            return inner.isBypassed();
        }

        @Override
        public void singleFlight(String key, Runnable task) {
            inner.singleFlight(key, task);
        }
    }

    private long one(String sql) {
        return dsl.fetchOne(sql).get(0, ULong.class).toBigInteger().longValueExact();
    }

    private List<Long> many(String sql) {
        return dsl.fetch(sql).getValues(0, ULong.class).stream().map(e -> e.toBigInteger().longValueExact()).toList();
    }

    @Test
    void whereTheSingleReadsAre() throws Exception {
        ScoreUser admin = new ScoreUser(new UserId(BigInteger.ONE), "oagis", "oagis", null, false, List.of(ScoreRole.ADMINISTRATOR, ScoreRole.DEVELOPER));
        long release = one("select max(release_id) from `release` where state = 'Published' and release_num <> 'Working'");
        long small = one("select asccp_manifest_id from asccp_manifest where release_id = " + release + " order by 1 limit 1");
        long middle = one("select asccp_manifest_id from asccp_manifest where release_id = " + release + " order by 1 limit 1 offset 1000");
        long large = one("select asccp_manifest_id from asccp_manifest where release_id = " + release + " order by 1 limit 1 offset 3000");
        long acc = one("select role_of_acc_manifest_id from asccp_manifest where asccp_manifest_id = " + large);
        long bie = many("select top_level_asbiep_id from top_level_asbiep order by 1 limit 1").get(0);
        var owner = dsl.fetchOne("select u.app_user_id, u.login_id, u.is_developer from top_level_asbiep t join app_user u on u.app_user_id = t.owner_user_id"
                + " where t.top_level_asbiep_id = " + bie);
        ScoreUser ownerUser = new ScoreUser(new UserId(owner.get(0, ULong.class).toBigInteger()), owner.get(1, String.class), owner.get(1, String.class),
                null, false, List.of(ScoreRole.END_USER));

        LinkedHashMap<String, Callable<?>> scenarios = new LinkedHashMap<>();
        scenarios.put("graph.asccp (large)", () -> graphService.getAsccpGraph(admin, new AsccpManifestId(BigInteger.valueOf(large)), false));
        scenarios.put("usages.acc (large)", () -> graphService.findUsages(admin, new FindUsagesRequest(CcType.ACC, new AccManifestId(BigInteger.valueOf(acc)))));
        scenarios.put("bie.read+visit", () -> {
            var document = bieReadService.getBieDocument(ownerUser, new TopLevelAsbiepId(BigInteger.valueOf(bie)));
            document.accept(new BieVisitor() {
            });
            return document;
        });
        scenarios.put("bie.generate.xml", () -> {
            GenerateExpressionOption option = new GenerateExpressionOption();
            option.setExpressionOption("XML");
            option.setPackageOption("EACH");
            return bieGenerateService.generate(ownerUser, List.of(new TopLevelAsbiepId(BigInteger.valueOf(bie))), option);
        });
        scenarios.put("export.standalone.xml (small)", () -> releaseQueryService.exportStandaloneSchema(admin, List.of(new AsccpManifestId(BigInteger.valueOf(small)))));
        scenarios.put("export.standalone.xml (middle)", () -> releaseQueryService.exportStandaloneSchema(admin, List.of(new AsccpManifestId(BigInteger.valueOf(middle)))));
        scenarios.put("export.standalone.xml (large)", () -> releaseQueryService.exportStandaloneSchema(admin, List.of(new AsccpManifestId(BigInteger.valueOf(large)))));

        RecordCache original = (RecordCache) ReflectionTestUtils.getField(repositoryFactory, "recordCache");
        StringBuilder out = new StringBuilder("# Single reads of the cache, by consumer (" + new Date() + ")\n\n");
        try {
            for (var scenario : scenarios.entrySet()) {
                                Observing observing = new Observing(new CaffeineRecordCache("maximumSize=200000,expireAfterWrite=600s", RecordCacheStatus.alwaysReady()));
                ReflectionTestUtils.setField(repositoryFactory, "recordCache", observing);
                System.out.println("SINGLEREADS " + scenario.getKey());
                for (String pass : List.of("cold (empty cache)", "warm (cache filled)")) {
                    observing.reset();
                    scenario.getValue().call();
                    out.append("## ").append(scenario.getKey()).append(" — ").append(pass).append("\n\n");
                    out.append("single gets %d (%d distinct keys, %d found), bulk reads %d (%d keys)%n%n".formatted(
                            observing.singles.sum(), observing.distinct.size(), observing.hits.sum(), observing.bulkCalls.sum(), observing.bulkKeys.sum()));
                    out.append("| kind of key | single gets |\n|---|---:|\n");
                    observing.byKind.entrySet().stream().sorted((a, b) -> Long.compare(b.getValue().sum(), a.getValue().sum())).limit(12)
                            .forEach(e -> out.append("| %s | %d |%n".formatted(e.getKey(), e.getValue().sum())));
                    out.append("\n| asked by (first frame outside the cache) <- kind | single gets |\n|---|---:|\n");
                    observing.byConsumer.entrySet().stream().sorted((a, b) -> Long.compare(b.getValue().sum(), a.getValue().sum())).limit(12)
                            .forEach(e -> out.append("| %s | %d |%n".formatted(e.getKey(), e.getValue().sum())));
                    out.append("\n");
                }
            }
        } finally {
            ReflectionTestUtils.setField(repositoryFactory, "recordCache", original);
        }
        Path file = Path.of(System.getProperty("score.singlereads.out", "target/cc-single-reads.md"));
        Files.createDirectories(file.toAbsolutePath().getParent());
        Files.writeString(file, out.toString());
        System.out.println("SINGLEREADS report written to " + file.toAbsolutePath());
    }
}
