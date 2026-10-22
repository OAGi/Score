package org.oagi.score.gateway.http.api.cc_management.model;

import com.sun.management.ThreadMXBean;
import org.jooq.DSLContext;
import org.jooq.ExecuteContext;
import org.jooq.ExecuteListenerProvider;
import org.jooq.impl.DefaultExecuteListener;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.jooq.types.ULong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.bie_management.service.BieGenerateService;
import org.oagi.score.gateway.http.api.bie_management.service.BieReadService;
import org.oagi.score.gateway.http.api.bie_management.service.BieVisitor;
import org.oagi.score.gateway.http.api.cc_management.cache.CcCache;
import org.oagi.score.gateway.http.api.cc_management.cache.CcCacheStatus;
import org.oagi.score.gateway.http.api.cc_management.cache.RedisCcCache;
import org.oagi.score.gateway.http.api.cc_management.cache.SpringCcCache;
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
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * How many users the application carries with the core components loaded for each request (A), from the cache in the JVM (B) or from
 * Redis (C), in the heap it is given. Many users work against the real services (a Spring context against the development
 * database), each in a loop: pick some work of a mix (graphs, reading a BIE, where-used, generating a schema, a small export), do it,
 * wait for a think time, again.
 * <ul>
 *   <li><b>think</b>: every user waits a random time (mean {@code score.capacity.think}, default 1,000 ms) between requests, as people do;</li>
 *   <li><b>stress</b>: no waiting at all, which shows where each setting saturates (and whether it runs out of heap).</li>
 * </ul>
 * Per setting: requests done, requests per second, the latency of the requests (p50, p95, p99, max), errors (out of memory counted apart),
 * the share of the time the garbage collector worked, allocation per second, the most of the heap that was ever in use, the
 * most the heap held right after a collection (what was alive), and database statements per second.
 * <pre>
 *   mvn test -Dtest=CcDocumentCapacityBenchmark -DargLine="-Xmx4g -Dscore.capacity=true"
 * </pre>
 * Options: {@code -Dscore.capacity.exclude=usages.acc,bie.generate} (kinds of work to leave out), {@code -Dscore.capacity.users=100}, {@code .seconds=40}, {@code .think=1000}, {@code .regimes=think,stress},
 * {@code .modes=A,B,C}, {@code .out=path.md}.
 */
@EnabledIfSystemProperty(named = "score.capacity", matches = "true")
@SpringBootTest(properties = {"logging.level.org.jooq=WARN", "logging.level.org.jooq.tools.LoggerListener=WARN"})
class CcDocumentCapacityBenchmark {

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
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    private final ThreadMXBean threads = (ThreadMXBean) ManagementFactory.getThreadMXBean();
    private final AtomicLong statements = new AtomicLong();
    private String redisPrefix;
    private CcCache originalCache;

    private record Work(String name, int weight, Callable<?> call) {
    }

    // ---- the work -------------------------------------------------------------------------------------------------

    private long one(String sql) {
        return dsl.fetchOne(sql).get(0, ULong.class).toBigInteger().longValueExact();
    }

    private List<Long> many(String sql) {
        return dsl.fetch(sql).getValues(0, ULong.class).stream().map(e -> e.toBigInteger().longValueExact()).toList();
    }

    private List<Work> mix() {
        ScoreUser admin = new ScoreUser(new UserId(BigInteger.ONE), "oagis", "oagis", null, false, List.of(ScoreRole.ADMINISTRATOR, ScoreRole.DEVELOPER));
        long release = one("select max(release_id) from `release` where state = 'Published' and release_num <> 'Working'");
        List<Long> asccps = new ArrayList<>();
        for (int offset : new int[]{0, 1000, 3000, 6000}) {
            asccps.addAll(many("select asccp_manifest_id from asccp_manifest where release_id = " + release + " order by 1 limit 1 offset " + offset));
        }
        List<Work> work = new ArrayList<>();
        for (long asccp : asccps) {
            long acc = one("select role_of_acc_manifest_id from asccp_manifest where asccp_manifest_id = " + asccp);
            work.add(new Work("graph.asccp", 30, () -> graphService.getAsccpGraph(admin, new AsccpManifestId(BigInteger.valueOf(asccp)), false)));
            work.add(new Work("graph.acc", 30, () -> graphService.getAccGraph(admin, new AccManifestId(BigInteger.valueOf(acc)))));
        }
        long acc0 = one("select role_of_acc_manifest_id from asccp_manifest where asccp_manifest_id = " + asccps.get(0));
        work.add(new Work("usages.acc", 12, () -> graphService.findUsages(admin, new FindUsagesRequest(CcType.ACC, new AccManifestId(BigInteger.valueOf(acc0))))));
        for (long bie : many("select top_level_asbiep_id from top_level_asbiep order by 1 limit 3")) {
            var owner = dsl.fetchOne("select u.app_user_id, u.login_id, u.is_developer from top_level_asbiep t join app_user u on u.app_user_id = t.owner_user_id"
                    + " where t.top_level_asbiep_id = " + bie);
            ScoreUser ownerUser = new ScoreUser(new UserId(owner.get(0, ULong.class).toBigInteger()), owner.get(1, String.class), owner.get(1, String.class),
                    null, false, owner.get(2, Integer.class) == 1 ? List.of(ScoreRole.DEVELOPER) : List.of(ScoreRole.END_USER));
            work.add(new Work("bie.read", 50, () -> {
                var document = bieReadService.getBieDocument(ownerUser, new TopLevelAsbiepId(BigInteger.valueOf(bie)));
                document.accept(new BieVisitor() {
                });
                return document;
            }));
            work.add(new Work("bie.generate", 4, () -> {
                GenerateExpressionOption option = new GenerateExpressionOption();
                option.setExpressionOption("XML");
                option.setPackageOption("EACH");
                return bieGenerateService.generate(ownerUser, List.of(new TopLevelAsbiepId(BigInteger.valueOf(bie))), option);
            }));
        }
        work.add(new Work("export.small", 4, () -> releaseQueryService.exportStandaloneSchema(admin, List.of(new AsccpManifestId(BigInteger.valueOf(asccps.get(0)))))));
        // -Dscore.capacity.exclude=usages.acc,bie.generate leaves those kinds of work out
        Set<String> excluded = new HashSet<>(Arrays.asList(System.getProperty("score.capacity.exclude", "").split(",")));
        work.removeIf(e -> excluded.contains(e.name()));
        return work;
    }

    // ---- the settings -----------------------------------------------------------------------------------------------

    private void setCache(String mode) {
        switch (mode) {
            case "A" -> ReflectionTestUtils.setField(repositoryFactory, "ccCache", CcCache.DISABLED);
            case "B" -> {
                CaffeineCacheManager manager = new CaffeineCacheManager();
                manager.setCacheSpecification("maximumSize=200000,expireAfterWrite=600s");
                ReflectionTestUtils.setField(repositoryFactory, "ccCache", new SpringCcCache(manager, CcCacheStatus.alwaysReady()));
            }
            case "C" -> {
                removeRedisKeys();
                ReflectionTestUtils.setField(repositoryFactory, "ccCache",
                        new RedisCcCache(redisConnectionFactory, redisPrefix, CcCacheStatus.alwaysReady(), Duration.ofMinutes(30)));
            }
            default -> throw new IllegalArgumentException(mode);
        }
    }

    private void removeRedisKeys() {
        try (var connection = redisConnectionFactory.getConnection()) {
            Set<byte[]> keys = connection.keyCommands().keys((redisPrefix + "*").getBytes());
            if (keys != null && !keys.isEmpty()) {
                connection.keyCommands().del(keys.toArray(new byte[0][]));
            }
        }
    }

    // ---- one run ------------------------------------------------------------------------------------------------------

    private record Result(String regime, String mode, int users, double seconds, long done, long errors, long outOfMemory, double perSecond,
                          double p50, double p95, double p99, double max, double gcPercent, double allocMbPerSecond, double peakUsedMb,
                          double maxLiveMb, double statementsPerSecond, Map<String, double[]> perWork) {
    }

    private long allocatedBytes() {
        long total = 0;
        for (long bytes : threads.getThreadAllocatedBytes(threads.getAllThreadIds())) {
            if (bytes > 0) {
                total += bytes;
            }
        }
        return total;
    }

    private long gcMillis() {
        long total = 0;
        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            total += Math.max(0, bean.getCollectionTime());
        }
        return total;
    }

    private static double percentile(List<Long> sortedNanos, double p) {
        if (sortedNanos.isEmpty()) {
            return Double.NaN;
        }
        return sortedNanos.get(Math.min(sortedNanos.size() - 1, (int) Math.ceil(sortedNanos.size() * p) - 1)) / 1e6;
    }

    private Result run(String regime, String mode, int users, long thinkMillis, int seconds, List<Work> work) throws Exception {
        setCache(mode);
        int totalWeight = work.stream().mapToInt(Work::weight).sum();
        // fill the cache the way the first users would, so the run measures the steady state
        for (Work w : work) {
            try {
                w.call().call();
            } catch (Throwable ignored) {
                // it will show in the run
            }
        }
        System.gc();

        AtomicBoolean stop = new AtomicBoolean();
        List<List<long[]>> perThread = new ArrayList<>();   // {work index, nanos}
        AtomicLong errors = new AtomicLong(), oom = new AtomicLong();
        Thread[] pool = new Thread[users];
        for (int i = 0; i < users; i++) {
            List<long[]> mine = new ArrayList<>();
            perThread.add(mine);
            pool[i] = new Thread(() -> {
                ThreadLocalRandom random = ThreadLocalRandom.current();
                try {
                    Thread.sleep(random.nextLong(Math.max(1, thinkMillis + 200)));   // not all at once
                } catch (InterruptedException e) {
                    return;
                }
                while (!stop.get()) {
                    int pick = random.nextInt(totalWeight), index = 0;
                    while (pick >= work.get(index).weight()) {
                        pick -= work.get(index).weight();
                        index++;
                    }
                    long start = System.nanoTime();
                    try {
                        work.get(index).call().call();
                        mine.add(new long[]{index, System.nanoTime() - start});
                    } catch (OutOfMemoryError e) {
                        oom.incrementAndGet();
                    } catch (Throwable e) {
                        errors.incrementAndGet();
                    }
                    if (thinkMillis > 0) {
                        try {
                            Thread.sleep((long) (-Math.log(1 - random.nextDouble()) * thinkMillis));
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                }
            }, "capacity-user-" + i);
            pool[i].setDaemon(true);
        }

        long statementsBefore = statements.get(), gcBefore = gcMillis(), allocatedBefore = allocatedBytes();
        double[] peak = {0, 0};
        Thread sampler = new Thread(() -> {
            while (!stop.get()) {
                peak[0] = Math.max(peak[0], ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / 1048576.0);
                double live = 0;
                for (MemoryPoolMXBean pool2 : ManagementFactory.getMemoryPoolMXBeans()) {
                    if (pool2.getType() == MemoryType.HEAP && pool2.getCollectionUsage() != null) {
                        live += pool2.getCollectionUsage().getUsed() / 1048576.0;
                    }
                }
                peak[1] = Math.max(peak[1], live);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }, "capacity-sampler");
        sampler.setDaemon(true);

        long start = System.nanoTime();
        sampler.start();
        for (Thread t : pool) {
            t.start();
        }
        Thread.sleep(seconds * 1000L);
        stop.set(true);
        double elapsed = (System.nanoTime() - start) / 1e9;
        long gcAfter = gcMillis(), allocatedAfter = allocatedBytes(), statementsAfter = statements.get();
        for (Thread t : pool) {
            t.join(60_000);
        }
        sampler.interrupt();

        List<Long> all = new ArrayList<>();
        Map<String, List<Long>> byWork = new TreeMap<>();
        long done = 0;
        for (List<long[]> mine : perThread) {
            List<long[]> copy;
            synchronized (mine) {
                copy = new ArrayList<>(mine);
            }
            for (long[] entry : copy) {
                all.add(entry[1]);
                byWork.computeIfAbsent(work.get((int) entry[0]).name(), k -> new ArrayList<>()).add(entry[1]);
                done++;
            }
        }
        Collections.sort(all);
        Map<String, double[]> perWork = new TreeMap<>();
        byWork.forEach((name, list) -> {
            Collections.sort(list);
            perWork.put(name, new double[]{list.size(), percentile(list, 0.5), percentile(list, 0.95), percentile(list, 0.99)});
        });
        return new Result(regime, mode, users, elapsed, done, errors.get(), oom.get(), done / elapsed,
                percentile(all, 0.5), percentile(all, 0.95), percentile(all, 0.99), all.isEmpty() ? Double.NaN : all.get(all.size() - 1) / 1e6,
                (gcAfter - gcBefore) / (elapsed * 10.0), (allocatedAfter - allocatedBefore) / 1048576.0 / elapsed, peak[0], peak[1],
                (statementsAfter - statementsBefore) / elapsed, perWork);
    }

    // ---- the test -----------------------------------------------------------------------------------------------------

    @Test
    void measureTheCapacity() throws Exception {
        originalCache = (CcCache) ReflectionTestUtils.getField(repositoryFactory, "ccCache");
        redisPrefix = "{zz-cc-capacity:" + UUID.randomUUID() + "}:";
        int users = Integer.getInteger("score.capacity.users", 100);
        int seconds = Integer.getInteger("score.capacity.seconds", 40);
        long think = Long.getLong("score.capacity.think", 1000L);
        List<String> regimes = Arrays.asList(System.getProperty("score.capacity.regimes", "think,stress").split(","));
        List<String> modes = Arrays.asList(System.getProperty("score.capacity.modes", "A,B,C").split(","));

        ExecuteListenerProvider[] old = dsl.configuration().executeListenerProviders();
        ExecuteListenerProvider[] all = Arrays.copyOf(old, old.length + 1);
        all[old.length] = new DefaultExecuteListenerProvider(new DefaultExecuteListener() {
            @Override
            public void executeStart(ExecuteContext ctx) {
                statements.incrementAndGet();
            }
        });
        dsl.configuration().set(all);

        List<Result> results = new ArrayList<>();
        try {
            List<Work> work = mix();
            for (String regime : regimes) {
                for (String mode : modes) {
                    System.out.println("CAPACITY " + regime + " " + mode);
                    results.add(run(regime, mode, users, regime.equals("think") ? think : 0, seconds, work));
                    writeReport(results, users, seconds, think, work);
                    System.gc();
                    Thread.sleep(1000);
                }
            }
        } finally {
            ReflectionTestUtils.setField(repositoryFactory, "ccCache", originalCache);
            dsl.configuration().set(old);
            removeRedisKeys();
        }

        writeReport(results, users, seconds, think, mix());
    }

    /** Writes the report of the runs so far; called after every run, so that a run that is stopped leaves its results. */
    private void writeReport(List<Result> results, int users, int seconds, long think, List<Work> work) throws Exception {
        StringBuilder out = new StringBuilder();
        out.append("# CcDocument capacity (").append(new Date()).append(")\n\n");
        out.append("%d users, %d s per run, heap %d MB max. think = mean %d ms between requests; stress = none. A no cache, B Caffeine, C Redis.%n%n"
                .formatted(users, seconds, Runtime.getRuntime().maxMemory() / 1048576, think));
        out.append("| regime | setting | done | req/s | errors | OOM | p50 ms | p95 ms | p99 ms | max ms | GC % | alloc MB/s | peak used MB | max live MB | SQL/s |\n");
        out.append("|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|\n");
        for (Result r : results) {
            out.append("| %s | %s | %d | %.1f | %d | %d | %.0f | %.0f | %.0f | %.0f | %.1f | %.0f | %.0f | %.0f | %.0f |%n".formatted(
                    r.regime(), r.mode(), r.done(), r.perSecond(), r.errors(), r.outOfMemory(), r.p50(), r.p95(), r.p99(), r.max(),
                    r.gcPercent(), r.allocMbPerSecond(), r.peakUsedMb(), r.maxLiveMb(), r.statementsPerSecond()));
        }
        out.append("\n## p95 per kind of work (ms; requests in brackets)\n\n| regime | setting |");
        List<String> names = new ArrayList<>(new TreeSet<>(work.stream().map(Work::name).toList()));
        for (String name : names) {
            out.append(" ").append(name).append(" |");
        }
        out.append("\n|---|---|").append("---:|".repeat(names.size())).append("\n");
        for (Result r : results) {
            out.append("| ").append(r.regime()).append(" | ").append(r.mode()).append(" |");
            for (String name : names) {
                double[] v = r.perWork().get(name);
                out.append(v == null ? " - |" : " %.0f (%d) |".formatted(v[2], (long) v[0]));
            }
            out.append("\n");
        }
        Path file = Path.of(System.getProperty("score.capacity.out", "target/cc-document-capacity.md"));
        Files.createDirectories(file.toAbsolutePath().getParent());
        Files.writeString(file, out.toString());
        System.out.println(out);
        System.out.println("CAPACITY report written to " + file.toAbsolutePath());
    }
}
