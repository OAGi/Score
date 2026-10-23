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
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.bie_management.service.BieDocument;
import org.oagi.score.gateway.http.api.bie_management.service.BieGenerateService;
import org.oagi.score.gateway.http.api.bie_management.service.BieReadService;
import org.oagi.score.gateway.http.api.bie_management.service.BieVisitor;
import org.oagi.score.gateway.http.cache.RecordCache;
import org.oagi.score.gateway.http.cache.RecordCacheStatus;
import org.oagi.score.gateway.http.cache.RedisRecordCache;
import org.oagi.score.gateway.http.cache.CaffeineRecordCache;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.graph.model.FindUsagesRequest;
import org.oagi.score.gateway.http.api.graph.service.GraphService;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseQueryService;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * What it costs a consumer to get its core components from the cache instead of from the loaded releases, measured through the
 * real services of the application (a Spring context against the development database) in three settings:
 * <ul>
 *   <li><b>A</b>, no cache: every consumer loads the releases it asks for, as it did before the cache;</li>
 *   <li><b>B</b>, the cache in the JVM (Caffeine);</li>
 *   <li><b>C</b>, the cache in Redis.</li>
 * </ul>
 * For each scenario: the first call with an empty cache (cold), then the median and the 95th percentile of the next ones (warm),
 * what a call allocates in all threads, how many SQL statements it runs and how long the database takes, and how long the
 * garbage collector worked. A separate pass with a recording proxy tells which methods the scenario calls and whether the document
 * had to go over to the loaded releases (and by which call): a scenario that does gains nothing from the cache.
 * <pre>
 *   mvn test -Dtest=CcDocumentImpactBenchmark -DargLine="-Xmx4g -Dscore.impact=true"
 * </pre>
 * Options: {@code -Dscore.impact.runs=7}, {@code -Dscore.impact.only=graph} (scenario names that contain it),
 * {@code -Dscore.impact.out=path.md}. The numbers are for the machine they ran on: compare the settings, not the machines.
 */
@EnabledIfSystemProperty(named = "score.impact", matches = "true")
@SpringBootTest(properties = {"logging.level.org.jooq=WARN", "logging.level.org.jooq.tools.LoggerListener=WARN"})
class CcDocumentImpactBenchmark {

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
    private final AtomicLong databaseNanos = new AtomicLong();

    private ScoreUser requester;
    private CcDocumentFactory originalFactory;
    private RecordCache originalCache;
    private String redisPrefix;

    // ---- the scenarios -------------------------------------------------------------------------------------------

    private record Scenario(String name, Callable<?> call) {
    }

    private long one(String sql) {
        return dsl.fetchOne(sql).get(0, ULong.class).toBigInteger().longValueExact();
    }

    private List<Long> many(String sql) {
        return dsl.fetch(sql).getValues(0, ULong.class).stream().map(e -> e.toBigInteger().longValueExact()).toList();
    }

    private List<Scenario> scenarios() {
        List<Scenario> list = new ArrayList<>();
        // the roots of the BIEs there are: their ASCCPs, the ACCs those are of, and the BIEs themselves
        List<Long> bies = many("select top_level_asbiep_id from top_level_asbiep order by 1 limit 3");
        List<Long> asccps = many("select distinct a.based_asccp_manifest_id from top_level_asbiep t join asbiep a on a.asbiep_id = t.asbiep_id"
                + " order by 1 limit 3");
        {
            // and three ASCCPs of the latest published release, small to large by their position
            long release = one("select max(release_id) from `release` where state = 'Published' and release_num <> 'Working'");
            asccps = new ArrayList<>(asccps);
            for (int offset : new int[]{0, 1000, 3000}) {
                for (long id : many("select asccp_manifest_id from asccp_manifest where release_id = " + release + " order by 1 limit 1 offset " + offset)) {
                    if (!asccps.contains(id)) {
                        asccps.add(id);
                    }
                }
            }
        }
        for (long asccp : asccps) {
            long acc = one("select role_of_acc_manifest_id from asccp_manifest where asccp_manifest_id = " + asccp);
            list.add(new Scenario("graph.asccp " + asccp, () -> graphService.getAsccpGraph(requester, new AsccpManifestId(BigInteger.valueOf(asccp)), false)));
            list.add(new Scenario("graph.acc " + acc, () -> graphService.getAccGraph(requester, new AccManifestId(BigInteger.valueOf(acc)))));
        }
        long bccp = one("select min(bccp_manifest_id) from bccp_manifest where release_id = (select release_id from asccp_manifest where asccp_manifest_id = " + asccps.get(0) + ")");
        long dt = one("select min(dt_manifest_id) from dt_manifest where release_id = (select release_id from asccp_manifest where asccp_manifest_id = " + asccps.get(0) + ")");
        long acc0 = one("select role_of_acc_manifest_id from asccp_manifest where asccp_manifest_id = " + asccps.get(0));
        list.add(new Scenario("graph.bccp " + bccp, () -> graphService.getBccpGraph(requester, new BccpManifestId(BigInteger.valueOf(bccp)))));
        list.add(new Scenario("graph.dt " + dt, () -> graphService.getDtGraph(requester, new DtManifestId(BigInteger.valueOf(dt)))));
        list.add(new Scenario("usages.acc " + acc0, () -> graphService.findUsages(requester,
                new FindUsagesRequest(CcType.ACC, new AccManifestId(BigInteger.valueOf(acc0))))));
        for (long bie : bies) {
            // a BIE that is being worked on can only be read by its owner
            var owner = dsl.fetchOne("select u.app_user_id, u.login_id, u.is_developer from top_level_asbiep t join app_user u on u.app_user_id = t.owner_user_id"
                    + " where t.top_level_asbiep_id = " + bie);
            ScoreUser ownerUser = new ScoreUser(new UserId(owner.get(0, ULong.class).toBigInteger()), owner.get(1, String.class), owner.get(1, String.class),
                    null, false, owner.get(2, Integer.class) == 1 ? List.of(ScoreRole.DEVELOPER) : List.of(ScoreRole.END_USER));
            list.add(new Scenario("bie.read+visit " + bie, () -> {
                BieDocument document = bieReadService.getBieDocument(ownerUser, new TopLevelAsbiepId(BigInteger.valueOf(bie)));
                document.accept(new BieVisitor() {
                });
                return document;
            }));
            list.add(new Scenario("bie.generate.xml " + bie, () -> {
                GenerateExpressionOption option = new GenerateExpressionOption();
                option.setExpressionOption("XML");
                option.setPackageOption("EACH");
                return bieGenerateService.generate(ownerUser, List.of(new TopLevelAsbiepId(BigInteger.valueOf(bie))), option);
            }));
        }
        for (long asccp : asccps) {
            list.add(new Scenario("export.standalone.xml " + asccp, () ->
                    releaseQueryService.exportStandaloneSchema(requester, List.of(new AsccpManifestId(BigInteger.valueOf(asccp))))));
        }
        String only = System.getProperty("score.impact.only");
        return (only == null) ? list : list.stream().filter(e -> e.name().contains(only)).toList();
    }

    // ---- the settings ----------------------------------------------------------------------------------------------

    private void useNoCache() {
        ReflectionTestUtils.setField(repositoryFactory, "recordCache", RecordCache.DISABLED);
    }

    private void useCaffeine() {
                ReflectionTestUtils.setField(repositoryFactory, "recordCache", new CaffeineRecordCache("maximumSize=200000,expireAfterWrite=600s", RecordCacheStatus.alwaysReady()));
    }

    private void useRedis() {
        ReflectionTestUtils.setField(repositoryFactory, "recordCache",
                new RedisRecordCache(redisConnectionFactory, redisPrefix, RecordCacheStatus.alwaysReady(), Duration.ofMinutes(30)));
    }

    private void removeRedisKeys() {
        try (var connection = redisConnectionFactory.getConnection()) {
            Set<byte[]> keys = connection.keyCommands().keys((redisPrefix + "*").getBytes());
            if (keys != null && !keys.isEmpty()) {
                connection.keyCommands().del(keys.toArray(new byte[0][]));
            }
        }
    }

    // ---- measuring -------------------------------------------------------------------------------------------------

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

    private record Sample(double millis, double allocatedMb, long statements, double databaseMillis, long gcMillis) {
    }

    private Sample sample(Callable<?> call) throws Exception {
        long statementsBefore = statements.get(), databaseBefore = databaseNanos.get(), gcBefore = gcMillis();
        long allocatedBefore = allocatedBytes();
        long start = System.nanoTime();
        call.call();
        long nanos = System.nanoTime() - start;
        return new Sample(nanos / 1e6, (allocatedBytes() - allocatedBefore) / 1048576.0, statements.get() - statementsBefore,
                (databaseNanos.get() - databaseBefore) / 1e6, gcMillis() - gcBefore);
    }

    private static double median(List<Double> values) {
        List<Double> sorted = values.stream().sorted().toList();
        return sorted.get(sorted.size() / 2);
    }

    private static double percentile95(List<Double> values) {
        List<Double> sorted = values.stream().sorted().toList();
        return sorted.get(Math.min(sorted.size() - 1, (int) Math.ceil(sorted.size() * 0.95) - 1));
    }

    private record Result(Sample cold, double warmMedian, double warmP95, double allocatedMb, double statements, double databaseMillis, double gcMillisPerCall) {
    }

    private Result measure(Callable<?> call, int runs, boolean emptyCacheFirst, Runnable freshCache) throws Exception {
        freshCache.run();
        Sample cold = sample(call);
        List<Double> millis = new ArrayList<>(), allocated = new ArrayList<>(), database = new ArrayList<>(), statementCounts = new ArrayList<>(), gc = new ArrayList<>();
        for (int i = 0; i < runs; i++) {
            Sample s = sample(call);
            millis.add(s.millis());
            allocated.add(s.allocatedMb());
            database.add(s.databaseMillis());
            statementCounts.add((double) s.statements());
            gc.add((double) s.gcMillis());
        }
        return new Result(cold, median(millis), percentile95(millis), median(allocated), median(statementCounts), median(database), median(gc));
    }

    // ---- the test ----------------------------------------------------------------------------------------------------

    @Test
    void measureTheScenarios() throws Exception {
        requester = new ScoreUser(new UserId(BigInteger.ONE), "oagis", "oagis", null, false, List.of(ScoreRole.ADMINISTRATOR, ScoreRole.DEVELOPER));
        originalFactory = (CcDocumentFactory) ReflectionTestUtils.getField(repositoryFactory, "ccDocumentFactory");
        originalCache = (RecordCache) ReflectionTestUtils.getField(repositoryFactory, "recordCache");
        redisPrefix = "{zz-cc-impact:" + UUID.randomUUID() + "}:";
        int runs = Integer.getInteger("score.impact.runs", 7);

        // count the statements and the time the database takes
        ExecuteListenerProvider[] old = dsl.configuration().executeListenerProviders();
        ExecuteListenerProvider[] all = Arrays.copyOf(old, old.length + 1);
        all[old.length] = new DefaultExecuteListenerProvider(new DefaultExecuteListener() {
            @Override
            public void executeStart(ExecuteContext ctx) {
                statements.incrementAndGet();
                ctx.data("impact.start", System.nanoTime());
            }

            @Override
            public void executeEnd(ExecuteContext ctx) {
                Object start = ctx.data("impact.start");
                if (start != null) {
                    databaseNanos.addAndGet(System.nanoTime() - (Long) start);
                }
            }
        });
        dsl.configuration().set(all);

        StringBuilder report = new StringBuilder();
        report.append("# CcDocument impact (").append(new Date()).append(")\n\n");
        report.append("Heap: ").append(Runtime.getRuntime().maxMemory() / 1048576).append(" MB max. Warm runs per scenario: ").append(runs)
                .append(". Milliseconds are wall time of one call; MB are allocated by all threads during one call.\n\n");
        report.append("| scenario | setting | cold ms | warm p50 ms | warm p95 ms | alloc MB | SQL statements | DB ms | GC ms/call |\n");
        report.append("|---|---|---:|---:|---:|---:|---:|---:|---:|\n");

        StringBuilder classification = new StringBuilder();
        classification.append("\n## What each scenario asks of its document (setting B, a recording proxy, one call)\n\n");
        classification.append("| scenario | documents | went over to the loaded releases | by (first call that did) | methods called (count) |\n|---|---:|---:|---|---|\n");

        try {
            for (Scenario scenario : scenarios()) {
                System.out.println("IMPACT " + scenario.name());
                // A: no cache
                useNoCache();
                for (int i = 0; i < 2; i++) {
                    scenario.call().call();   // warms the JVM and the database
                }
                Result a = measure(scenario.call(), runs, false, this::useNoCache);
                row(report, scenario.name(), "A no cache", a);

                // B: the cache in the JVM
                Result b = measure(scenario.call(), runs, true, this::useCaffeine);
                row(report, scenario.name(), "B Caffeine", b);

                // C: the cache in Redis
                Result c = measure(scenario.call(), runs, true, () -> {
                    removeRedisKeys();
                    useRedis();
                });
                row(report, scenario.name(), "C Redis", c);
                removeRedisKeys();

                // what the scenario asks of its document
                useCaffeine();
                RecordingCcDocumentFactory recorder = new RecordingCcDocumentFactory(repositoryFactory);
                ReflectionTestUtils.setField(repositoryFactory, "ccDocumentFactory", recorder);
                try {
                    scenario.call().call();
                } finally {
                    ReflectionTestUtils.setField(repositoryFactory, "ccDocumentFactory", originalFactory);
                }
                classify(classification, scenario.name(), recorder.docs());
            }
        } finally {
            ReflectionTestUtils.setField(repositoryFactory, "recordCache", originalCache);
            ReflectionTestUtils.setField(repositoryFactory, "ccDocumentFactory", originalFactory);
            dsl.configuration().set(old);
            removeRedisKeys();
        }

        report.append(classification);
        Path out = Path.of(System.getProperty("score.impact.out", "target/cc-document-impact.md"));
        Files.createDirectories(out.toAbsolutePath().getParent());
        Files.writeString(out, report.toString());
        System.out.println(report);
        System.out.println("IMPACT report written to " + out.toAbsolutePath());
    }

    private static void row(StringBuilder report, String scenario, String setting, Result r) {
        report.append("| %s | %s | %.1f | %.1f | %.1f | %.1f | %.0f | %.1f | %.1f |%n".formatted(
                scenario, setting, r.cold().millis(), r.warmMedian(), r.warmP95(), r.allocatedMb(), r.statements(), r.databaseMillis(), r.gcMillisPerCall()));
    }

    private static void classify(StringBuilder out, String scenario, List<RecordingCcDocumentFactory.Doc> docs) {
        long switched = docs.stream().filter(d -> d.switchedBy != null).count();
        Set<String> by = new TreeSet<>();
        Map<String, Long> calls = new TreeMap<>();
        for (RecordingCcDocumentFactory.Doc doc : docs) {
            if (doc.switchedBy != null) {
                by.add(doc.switchedBy);
            }
            doc.calls.forEach((method, count) -> calls.merge(method, count.sum(), Long::sum));
        }
        StringBuilder methods = new StringBuilder();
        calls.entrySet().stream().sorted((x, y) -> Long.compare(y.getValue(), x.getValue())).forEach(e ->
                methods.append(e.getKey()).append(" (").append(e.getValue()).append("), "));
        if (methods.length() > 2) {
            methods.setLength(methods.length() - 2);
        }
        out.append("| %s | %d | %d | %s | %s |%n".formatted(scenario, docs.size(), switched, by.isEmpty() ? "-" : String.join(", ", by), methods));
    }
}
