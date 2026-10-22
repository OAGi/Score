package org.oagi.score.gateway.http.api.cc_management.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.cc_management.cache.CcCache;
import org.oagi.score.gateway.http.api.cc_management.cache.CcCacheWriteObserver;
import org.oagi.score.gateway.http.api.cc_management.cache.RedisCcCache;
import org.oagi.score.gateway.http.api.cc_management.cache.SpringCcCache;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.graph.model.CoreComponentGraphContext;
import org.oagi.score.gateway.http.api.graph.model.Graph;
import org.oagi.score.gateway.http.api.graph.model.Node;
import org.oagi.score.gateway.http.api.graph.service.GraphService;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Many graph requests at the same time, in one JVM, over the cache of the JVM and over Redis: the time of a request
 * (building the graph and writing it as JSON, as the endpoint does), and the memory and the garbage collector it takes.
 * Opt-in: {@code mvn test -Dtest=CcCacheConcurrencyBenchmark -Dscore.benchmark=true -DargLine=-Xmx4g}
 * ({@code -Dscore.benchmark.threads=100 -Dscore.benchmark.requests=20 -Dscore.benchmark.thinkMillis=2000}: each thread
 * is a user who waits about that long between requests; without it the threads send requests back to back).
 */
@EnabledIfSystemProperty(named = "score.benchmark", matches = "true")
class CcCacheConcurrencyBenchmark {

    private static final ScoreUser REQUESTER = new ScoreUser(null, "benchmark", "benchmark", null, false, null);
    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

    @Test
    void manyRequestsAtOnce() throws Exception {
        long root = Long.getLong("score.benchmark.root", 786126);
        int threads = Integer.getInteger("score.benchmark.threads", 100);
        int requestsPerThread = Integer.getInteger("score.benchmark.requests", 20);
        long thinkMillis = Long.getLong("score.benchmark.thinkMillis", 0);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(System.getProperty("score.db.url", "jdbc:mariadb://localhost:3306/oagi?useSSL=false"));
        config.setUsername(System.getProperty("score.db.username", "oagi"));
        config.setPassword(System.getProperty("score.db.password", "oagi"));
        config.setMaximumPoolSize(threads);
        LettuceConnectionFactory redis = new LettuceConnectionFactory(new RedisStandaloneConfiguration(
                System.getProperty("score.redis.host", "localhost"), Integer.getInteger("score.redis.port", 6379)));
        redis.afterPropertiesSet();
        String prefix = "{zz-cc-concurrency:" + UUID.randomUUID() + "}:";
        StringBuilder report = new StringBuilder("\n%d threads x %d requests (think time %d ms) of the graph of ACC %d, max heap %d MB%n"
                .formatted(threads, requestsPerThread, thinkMillis, root, Runtime.getRuntime().maxMemory() >> 20));
        report.append("%-9s %8s %8s %8s %8s %12s %10s %12s%n".formatted("cache", "p50 ms", "p95 ms", "p99 ms", "max ms",
                "requests/s", "GC ms", "peak heap MB"));
        try (HikariDataSource dataSource = new HikariDataSource(config)) {
            DSLContext dsl = DSL.using(dataSource, SQLDialect.MARIADB);
            ReleaseId release = new ReleaseId(dsl.fetchOne("select release_id from acc_manifest where acc_manifest_id = ?", root)
                    .get(0, org.jooq.types.ULong.class).toBigInteger());
            CaffeineCacheManager caffeine = new CaffeineCacheManager();
            caffeine.setCacheSpecification("maximumSize=200000,expireAfterWrite=600s");
            Map<String, CcCache> caches = new LinkedHashMap<>();
            caches.put("caffeine", new SpringCcCache(caffeine,
                    new StaticListableBeanFactory().getBeanProvider(org.redisson.api.RedissonClient.class)));
            caches.put("redis", new RedisCcCache(redis, prefix, Duration.ofMinutes(10)));
            try {
                for (Map.Entry<String, CcCache> entry : caches.entrySet()) {
                    RepositoryFactory repositoryFactory = new RepositoryFactory(dsl);
                    ReflectionTestUtils.setField(repositoryFactory, "ccCache", entry.getValue());
                    GraphService graphService = new GraphService();
                    ReflectionTestUtils.setField(graphService, "repositoryFactory", repositoryFactory);
                    ((CcCacheWriteObserver) entry.getValue()).written();
                    request(graphService, repositoryFactory, release, root);   // fills the cache
                    for (int i = 0; i < 20; i++) {
                        request(graphService, repositoryFactory, release, root);   // warms the JVM
                    }
                    System.gc();

                    long gcBefore = ManagementFactory.getGarbageCollectorMXBeans().stream()
                            .mapToLong(GarbageCollectorMXBean::getCollectionTime).sum();
                    AtomicLong peak = new AtomicLong();
                    Thread sampler = new Thread(() -> {
                        while (!Thread.currentThread().isInterrupted()) {
                            Runtime rt = Runtime.getRuntime();
                            peak.accumulateAndGet(rt.totalMemory() - rt.freeMemory(), Math::max);
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    });
                    sampler.setDaemon(true);
                    sampler.start();

                    ExecutorService pool = Executors.newFixedThreadPool(threads);
                    CountDownLatch go = new CountDownLatch(1);
                    List<Future<List<Double>>> futures = new ArrayList<>();
                    for (int t = 0; t < threads; t++) {
                        futures.add(pool.submit(() -> {
                            go.await();
                            if (thinkMillis > 0) {
                                // the users do not all start at the same moment
                                Thread.sleep((long) (thinkMillis * java.util.concurrent.ThreadLocalRandom.current().nextDouble()));
                            }
                            List<Double> times = new ArrayList<>();
                            for (int i = 0; i < requestsPerThread; i++) {
                                long start = System.nanoTime();
                                request(graphService, repositoryFactory, release, root);
                                times.add((System.nanoTime() - start) / 1e6);
                                if (thinkMillis > 0) {
                                    Thread.sleep((long) (thinkMillis * (0.5 + java.util.concurrent.ThreadLocalRandom.current().nextDouble())));
                                }
                            }
                            return times;
                        }));
                    }
                    long started = System.nanoTime();
                    go.countDown();
                    List<Double> all = new ArrayList<>();
                    for (Future<List<Double>> future : futures) {
                        all.addAll(future.get());
                    }
                    double seconds = (System.nanoTime() - started) / 1e9;
                    pool.shutdown();
                    sampler.interrupt();
                    long gcAfter = ManagementFactory.getGarbageCollectorMXBeans().stream()
                            .mapToLong(GarbageCollectorMXBean::getCollectionTime).sum();
                    Collections.sort(all);
                    report.append("%-9s %8.0f %8.0f %8.0f %8.0f %12.1f %10d %12d%n".formatted(entry.getKey(),
                            all.get(all.size() / 2), all.get((int) (all.size() * 0.95)), all.get((int) (all.size() * 0.99)),
                            all.get(all.size() - 1), all.size() / seconds, gcAfter - gcBefore, peak.get() >> 20));
                }
            } finally {
                try (var connection = redis.getConnection()) {
                    var left = connection.keyCommands().keys((prefix + "*").getBytes());
                    if (left != null && !left.isEmpty()) {
                        connection.keyCommands().del(left.toArray(new byte[0][]));
                    }
                }
                redis.destroy();
            }
        }
        System.out.println(report);
    }

    /** What the endpoint does: a document, the graph, and the graph as JSON. */
    private void request(GraphService graphService, RepositoryFactory repositoryFactory, ReleaseId release, long root) throws Exception {
        CcDocument document = repositoryFactory.ccDocumentFactory().create(REQUESTER, release);
        CoreComponentGraphContext context = new CoreComponentGraphContext(document);
        Node rootNode = context.toNode(document.getAcc(new AccManifestId(BigInteger.valueOf(root))));
        Graph graph = ReflectionTestUtils.invokeMethod(graphService, "buildGraph", context, rootNode, false);
        if (JSON.writeValueAsBytes(graph).length == 0) {
            throw new IllegalStateException();
        }
    }

}
