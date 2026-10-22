package org.oagi.score.gateway.http.api.cc_management.model;

import org.jooq.DSLContext;
import org.jooq.ExecuteContext;
import org.jooq.ExecuteListenerProvider;
import org.jooq.impl.DefaultExecuteListener;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.jooq.types.ULong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.cc_management.cache.CcCache;
import org.oagi.score.gateway.http.api.cc_management.cache.CcCacheProperties;
import org.oagi.score.gateway.http.api.cc_management.cache.CcCacheStatus;
import org.oagi.score.gateway.http.api.cc_management.cache.RedisCcCache;
import org.oagi.score.gateway.http.api.cc_management.cache.SpringCcCache;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.graph.service.GraphService;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseQueryService;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Which values of the two settings of {@code score.cc-cache} are enough. Opt-in:
 * <pre>
 *   mvn test -Dtest=CcCacheTuningBenchmark -DargLine="-Xmx4g -Dscore.tuning=true"
 * </pre>
 * <b>batch-size</b>: a large graph and a large schema export with the cache empty, which read thousands of ids in
 * {@code IN} lists, with the batch sizes in {@code -Dscore.tuning.batchSizes} (time, statements, the longest statement).
 * <b>max-prefetched-dependencies</b>: one document that is asked for k components of no relation to each other (a schema export
 * of k ASCCPs), with the limits in {@code -Dscore.tuning.limits}: the cache empty, full in the JVM and full in Redis.
 * Other options: {@code -Dscore.tuning.reps=5}, {@code -Dscore.tuning.roots=1,2,4,8,16,32},
 * {@code -Dscore.tuning.out=path.md}.
 */
@EnabledIfSystemProperty(named = "score.tuning", matches = "true")
@SpringBootTest(properties = {"logging.level.org.jooq=WARN", "logging.level.org.jooq.tools.LoggerListener=WARN"})
class CcCacheTuningBenchmark {

    @Autowired
    private RepositoryFactory repositoryFactory;
    @Autowired
    private DSLContext dsl;
    @Autowired
    private GraphService graphService;
    @Autowired
    private ReleaseQueryService releaseQueryService;
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    @Autowired
    private CcCacheProperties properties;

    private final AtomicLong statements = new AtomicLong();
    private final AtomicInteger longestStatement = new AtomicInteger();

    private ScoreUser requester;
    private String redisPrefix;

    private record Sample(double millis, long statements, int longestStatement) {
    }

    private Sample sample(Callable<?> call) throws Exception {
        long before = statements.get();
        longestStatement.set(0);
        long start = System.nanoTime();
        call.call();
        return new Sample((System.nanoTime() - start) / 1e6, statements.get() - before, longestStatement.get());
    }

    private static double median(List<Double> values) {
        List<Double> sorted = values.stream().sorted().toList();
        return sorted.get(sorted.size() / 2);
    }

    private void freshCaffeine() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCacheSpecification("maximumSize=200000,expireAfterWrite=600s");
        ReflectionTestUtils.setField(repositoryFactory, "ccCache", new SpringCcCache(manager, CcCacheStatus.alwaysReady()));
    }

    private void freshRedis() {
        removeRedisKeys();
        ReflectionTestUtils.setField(repositoryFactory, "ccCache",
                new RedisCcCache(redisConnectionFactory, redisPrefix, CcCacheStatus.alwaysReady(), Duration.ofMinutes(30)));
    }

    private void removeRedisKeys() {
        try (var connection = redisConnectionFactory.getConnection()) {
            Set<byte[]> keys = connection.keyCommands().keys((redisPrefix + "*").getBytes());
            if (keys != null && !keys.isEmpty()) {
                connection.keyCommands().del(keys.toArray(new byte[0][]));
            }
        }
    }

    private long one(String sql) {
        return dsl.fetchOne(sql).get(0, ULong.class).toBigInteger().longValueExact();
    }

    private static List<Integer> ints(String property, String defaults) {
        return Arrays.stream(System.getProperty(property, defaults).split(",")).map(String::trim).map(Integer::parseInt).toList();
    }

    @Test
    void tune() throws Exception {
        requester = new ScoreUser(new UserId(BigInteger.ONE), "oagis", "oagis", null, false, List.of(ScoreRole.ADMINISTRATOR, ScoreRole.DEVELOPER));
        redisPrefix = "{zz-cc-tuning:" + UUID.randomUUID() + "}:";
        int reps = Integer.getInteger("score.tuning.reps", 5);
        List<Integer> batchSizes = ints("score.tuning.batchSizes", "25,50,100,200,500,1000,2000");
        List<Integer> limits = ints("score.tuning.limits", "0,1,2,4,8,16");
        List<Integer> roots = ints("score.tuning.roots", "1,2,4,8,16,32");

        ExecuteListenerProvider[] old = dsl.configuration().executeListenerProviders();
        ExecuteListenerProvider[] all = Arrays.copyOf(old, old.length + 1);
        all[old.length] = new DefaultExecuteListenerProvider(new DefaultExecuteListener() {
            @Override
            public void executeStart(ExecuteContext ctx) {
                statements.incrementAndGet();
                String sql = ctx.sql();
                if (sql != null) {
                    longestStatement.accumulateAndGet(sql.length(), Math::max);
                }
            }
        });
        dsl.configuration().set(all);

        long release = one("select max(release_id) from `release` where state = 'Published' and release_num <> 'Working'");
        long count = one("select count(*) from asccp_manifest where release_id = " + release);
        long large = one("select asccp_manifest_id from asccp_manifest where release_id = " + release + " order by 1 limit 1 offset 3000");

        CcCache originalCache = (CcCache) ReflectionTestUtils.getField(repositoryFactory, "ccCache");
        int originalBatch = properties.getBatchSize(), originalLimit = properties.getMaxPrefetchedDependencies();
        StringBuilder report = new StringBuilder("# Tuning of score.cc-cache (" + new Date() + ")\n\n");
        report.append("Heap ").append(Runtime.getRuntime().maxMemory() / 1048576).append(" MB. Median of ").append(reps)
                .append(" runs, milliseconds. Release ").append(release).append(" has ").append(count).append(" ASCCPs.\n\n");
        try {
            // ---- batch-size ------------------------------------------------------------------------------------------------
            report.append("## batch-size: a large graph and a large schema export, the cache empty (Caffeine)\n\n");
            report.append("| batch-size | graph ms | export ms | statements (graph + export) | longest statement, characters |\n|---|---:|---:|---:|---:|\n");
            Callable<?> graph = () -> graphService.getAsccpGraph(requester, new AsccpManifestId(BigInteger.valueOf(large)), false);
            Callable<?> export = () -> releaseQueryService.exportStandaloneSchema(requester, List.of(new AsccpManifestId(BigInteger.valueOf(large))));
            properties.setMaxPrefetchedDependencies(originalLimit);
            for (int i = 0; i < 2; i++) {   // warms the JVM and the database
                freshCaffeine();
                graph.call();
                export.call();
            }
            for (int batchSize : batchSizes) {
                properties.setBatchSize(batchSize);
                List<Double> graphMillis = new ArrayList<>(), exportMillis = new ArrayList<>();
                long statementCount = 0;
                int longest = 0;
                for (int i = 0; i < reps; i++) {
                    freshCaffeine();
                    Sample g = sample(graph);
                    freshCaffeine();
                    Sample e = sample(export);
                    graphMillis.add(g.millis());
                    exportMillis.add(e.millis());
                    statementCount = g.statements() + e.statements();
                    longest = Math.max(g.longestStatement(), e.longestStatement());
                }
                System.out.println("TUNING batch-size " + batchSize);
                report.append(String.format("| %d | %.0f | %.0f | %d | %d |%n", batchSize, median(graphMillis), median(exportMillis), statementCount, longest));
            }
            properties.setBatchSize(originalBatch);

            // ---- max-prefetched-dependencies ---------------------------------------------------------------------------
            report.append("\n## max-prefetched-dependencies: one schema export of k unrelated ASCCPs\n\n");
            report.append("Each cell: milliseconds (statements). `cold` is the cache empty, `warm` the cache full in the JVM, `redis` the cache full in Redis.\n\n");
            long step = Math.max(1, count / (roots.stream().mapToInt(Integer::intValue).max().orElse(1) + 1));
            List<Long> candidates = new ArrayList<>();
            for (int i = 0; i < roots.stream().mapToInt(Integer::intValue).max().orElse(1); i++) {
                candidates.add(one("select asccp_manifest_id from asccp_manifest where release_id = " + release
                        + " order by 1 limit 1 offset " + (i * step)));
            }
            for (String setting : new String[]{"cold", "warm", "redis"}) {
                report.append("### ").append(setting).append("\n\n| k roots |");
                for (int limit : limits) {
                    report.append(" limit ").append(limit).append(" |");
                }
                report.append("\n|---|").append("---:|".repeat(limits.size())).append("\n");
                for (int k : roots) {
                    List<AsccpManifestId> ids = candidates.subList(0, k).stream().map(e -> new AsccpManifestId(BigInteger.valueOf(e))).toList();
                    Callable<?> call = () -> releaseQueryService.exportStandaloneSchema(requester, ids);
                    report.append("| ").append(k).append(" |");
                    for (int limit : limits) {
                        properties.setMaxPrefetchedDependencies(limit);
                        List<Double> millis = new ArrayList<>();
                        long statementCount = 0;
                        for (int i = 0; i < reps; i++) {
                            switch (setting) {
                                case "cold" -> freshCaffeine();
                                case "warm" -> {
                                    freshCaffeine();
                                    call.call();
                                }
                                default -> {
                                    freshRedis();
                                    call.call();
                                }
                            }
                            Sample s = sample(call);
                            millis.add(s.millis());
                            statementCount = s.statements();
                        }
                        System.out.println("TUNING " + setting + " k=" + k + " limit=" + limit);
                        report.append(String.format(" %.0f (%d) |", median(millis), statementCount));
                    }
                    report.append("\n");
                }
                report.append("\n");
            }
        } finally {
            properties.setBatchSize(originalBatch);
            properties.setMaxPrefetchedDependencies(originalLimit);
            ReflectionTestUtils.setField(repositoryFactory, "ccCache", originalCache);
            removeRedisKeys();
        }
        System.out.println(report);
        String out = System.getProperty("score.tuning.out");
        if (out != null) {
            try {
                Files.writeString(Path.of(out), report.toString());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
