package org.oagi.score.gateway.http.api.cc_management.model;

import org.oagi.score.gateway.http.common.TestDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.cc_management.cache.CcCache;
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

import java.math.BigInteger;
import java.time.Duration;
import java.util.*;

/**
 * How long a graph takes over the cache of the JVM (Caffeine) and over Redis, with the real implementations, split into
 * what a request does: making the document, reading the graph from the cache, and walking it. Opt-in:
 * {@code mvn test -Dtest=CcCacheProviderBenchmark -Dscore.benchmark=true}.
 */
@EnabledIfSystemProperty(named = "score.benchmark", matches = "true")
class CcCacheProviderBenchmark {

    private static final ScoreUser REQUESTER = new ScoreUser(null, "benchmark", "benchmark", null, false, null);

    private static double median(List<Double> values) {
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        return sorted.get(sorted.size() / 2);
    }

    @Test
    void caffeineAgainstRedis() throws Exception {
        long root = Long.getLong("score.benchmark.root", 786126);
        int runs = Integer.getInteger("score.benchmark.runs", 30);
        LettuceConnectionFactory redis = new LettuceConnectionFactory(new RedisStandaloneConfiguration(
                System.getProperty("score.redis.host", "localhost"), Integer.getInteger("score.redis.port", 6379)));
        redis.afterPropertiesSet();
        String prefix = "{zz-cc-benchmark:" + UUID.randomUUID() + "}:";
        try (TestDataSource dataSource = TestDataSource.open(4)) {
            DSLContext dsl = DSL.using(dataSource, SQLDialect.MARIADB);
            ReleaseId release = new ReleaseId(dsl.fetchOne("select release_id from acc_manifest where acc_manifest_id = ?", root)
                    .get(0, org.jooq.types.ULong.class).toBigInteger());

            CaffeineCacheManager caffeine = new CaffeineCacheManager();
            caffeine.setCacheSpecification("maximumSize=200000,expireAfterWrite=600s");
            Map<String, CcCache> caches = new LinkedHashMap<>();
            caches.put("caffeine", new SpringCcCache(caffeine, org.oagi.score.gateway.http.api.cc_management.cache.CcCacheStatus.alwaysReady()));
            caches.put("redis", new RedisCcCache(redis, prefix, org.oagi.score.gateway.http.api.cc_management.cache.CcCacheStatus.alwaysReady(), Duration.ofMinutes(10)));

            StringBuilder report = new StringBuilder("\ngraph rooted at ACC %d, %d warm runs; milliseconds, median%n".formatted(root, runs));
            report.append("%-9s %9s %9s %9s %9s %9s%n".formatted("cache", "cold", "document", "read", "walk", "total"));
            try {
                for (Map.Entry<String, CcCache> entry : caches.entrySet()) {
                    RepositoryFactory repositoryFactory = new RepositoryFactory(dsl);
                    ReflectionTestUtils.setField(repositoryFactory, "ccCache", entry.getValue());
                    GraphService graphService = new GraphService();
                    ReflectionTestUtils.setField(graphService, "repositoryFactory", repositoryFactory);
                    entry.getValue().invalidate(java.util.List.of());

                    double[] cold = one(graphService, repositoryFactory, release, root);
                    one(graphService, repositoryFactory, release, root);
                    List<Double> document = new ArrayList<>(), read = new ArrayList<>(), walk = new ArrayList<>(), total = new ArrayList<>();
                    for (int i = 0; i < runs; i++) {
                        double[] t = one(graphService, repositoryFactory, release, root);
                        document.add(t[1]);
                        read.add(t[2]);
                        walk.add(t[3]);
                        total.add(t[0]);
                    }
                    report.append("%-9s %9.0f %9.1f %9.1f %9.1f %9.1f%n".formatted(entry.getKey(), cold[0],
                            median(document), median(read), median(walk), median(total)));
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
            System.out.println(report);
        }
    }

    /** One request: {total, making the document, reading the graph from the cache, walking it}, in milliseconds. */
    private double[] one(GraphService graphService, RepositoryFactory repositoryFactory, ReleaseId release, long root) {
        long t0 = System.nanoTime();
        CcDocument document = new CachedCcDocument(REQUESTER, repositoryFactory, release);
        long t1 = System.nanoTime();
        document.prefetchDependencies(CcType.ACC, new AccManifestId(BigInteger.valueOf(root)));
        long t2 = System.nanoTime();
        CoreComponentGraphContext context = new CoreComponentGraphContext(document);
        Node rootNode = context.toNode(document.getAcc(new AccManifestId(BigInteger.valueOf(root))));
        Graph graph = ReflectionTestUtils.invokeMethod(graphService, "buildGraph", context, rootNode, false);
        long t3 = System.nanoTime();
        if (graph == null || graph.getNodes().isEmpty()) {
            throw new IllegalStateException();
        }
        return new double[]{(t3 - t0) / 1e6, (t1 - t0) / 1e6, (t2 - t1) / 1e6, (t3 - t2) / 1e6};
    }

}
