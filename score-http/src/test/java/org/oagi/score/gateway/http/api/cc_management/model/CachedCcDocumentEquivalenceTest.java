package org.oagi.score.gateway.http.api.cc_management.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.oagi.score.gateway.http.common.TestDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.cache.CcCache;
import org.oagi.score.gateway.http.api.cc_management.cache.RedisCcCache;
import org.oagi.score.gateway.http.api.cc_management.cache.SpringCcCache;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.blob_content.BlobContentManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.graph.model.CoreComponentGraphContext;
import org.oagi.score.gateway.http.api.graph.model.Graph;
import org.oagi.score.gateway.http.api.graph.model.Node;
import org.oagi.score.gateway.http.api.graph.service.GraphService;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Calls {@link CcDocumentImpl} and {@link CachedCcDocument} with every input the database has and checks that every
 * output is the same: for each release, every manifest of every kind goes through every method that takes one, and so
 * do ids that are not in the release (the ones of other releases, ones that exist nowhere, null).
 * <p>
 * It reads the database the application uses, so it only runs when asked to:
 * <pre>
 *   mvn test -Dtest=CachedCcDocumentEquivalenceTest -Dscore.equivalence=true -DargLine=-Xmx8g
 * </pre>
 * Options: {@code -Dscore.equivalence.releases=76,54} (default: every release),
 * {@code -Dscore.equivalence.threads=8}. The database is the one Spring is configured for ({@code spring.datasource.*}).
 */
@EnabledIfSystemProperty(named = "score.equivalence", matches = "true")
class CachedCcDocumentEquivalenceTest {

    /** The cache of the application: the provider Spring Boot picks by default. */
    private static final String CACHE_SPEC = "maximumSize=200000,expireAfterWrite=600s";
    private static final int EXAMPLES_PER_METHOD = 5;
    private static final int GRAPH_ROOTS_PER_KIND = 3;

    private static TestDataSource dataSource;
    private static DSLContext dsl;
    private static final ScoreUser REQUESTER = new ScoreUser(null, "equivalence", "equivalence", null, false, null);
    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

    @BeforeAll
    static void connect() {
        dataSource = TestDataSource.open(Integer.getInteger("score.equivalence.threads", 8) * 2 + 2);
        dsl = DSL.using(dataSource, SQLDialect.MARIADB);
    }

    @AfterAll
    static void disconnect() {
        dataSource.close();
    }

    // ---- the report ----------------------------------------------------------------------------------------------

    private static final class Stats {
        final AtomicLong checked = new AtomicLong();
        final AtomicLong different = new AtomicLong();
        final AtomicLong orderOnly = new AtomicLong();
        final List<String> examples = Collections.synchronizedList(new ArrayList<>());
    }

    private final ConcurrentMap<String, Stats> report = new ConcurrentHashMap<>();

    private Stats stats(String method) {
        return report.computeIfAbsent(method, k -> new Stats());
    }

    /** What a call answered: its value (a collection as a list) or the type of what it threw. */
    private static Object outcome(Callable<Object> call) {
        try {
            Object value = call.call();
            return (value instanceof Collection<?> c) ? new ArrayList<>(c) : value;
        } catch (Throwable t) {
            return "THROWS " + t.getClass().getName();
        }
    }

    private void check(String method, Object input, CcDocument eager, CcDocument cached, Function<CcDocument, Object> call) {
        Object expected = outcome(() -> call.apply(eager));
        Object actual = outcome(() -> call.apply(cached));
        Stats stats = stats(method);
        stats.checked.incrementAndGet();
        if (Objects.equals(expected, actual) || deepSame(expected, actual)) {
            return;
        }
        boolean orderOnly = expected instanceof List<?> e && actual instanceof List<?> a
                && e.size() == a.size() && new HashSet<>(e).equals(new HashSet<>(a))
                && e.stream().collect(Collectors.groupingBy(x -> x, Collectors.counting()))
                .equals(a.stream().collect(Collectors.groupingBy(x -> x, Collectors.counting())));
        (orderOnly ? stats.orderOnly : stats.different).incrementAndGet();
        if (stats.examples.size() < EXAMPLES_PER_METHOD) {
            stats.examples.add((orderOnly ? "[order only] " : "") + method + "(" + input + ")\n    expected: "
                    + abbreviate(expected) + "\n    actual:   " + abbreviate(actual));
        }
    }

    /**
     * Whether two values are the same by what they hold. A record compares its arrays by identity (the content of a
     * blob is a byte[]), so the plain {@code equals} calls two blobs with the same bytes different.
     */
    private static boolean deepSame(Object a, Object b) {
        if (a == null || b == null) {
            return a == b;
        }
        if (a.getClass().isArray() && b.getClass().isArray()) {
            return Objects.deepEquals(a, b);
        }
        if (a instanceof List<?> x && b instanceof List<?> y) {
            if (x.size() != y.size()) {
                return false;
            }
            for (int i = 0; i < x.size(); i++) {
                if (!Objects.equals(x.get(i), y.get(i)) && !deepSame(x.get(i), y.get(i))) {
                    return false;
                }
            }
            return true;
        }
        if (a.getClass().isRecord() && a.getClass() == b.getClass()) {
            for (java.lang.reflect.RecordComponent component : a.getClass().getRecordComponents()) {
                try {
                    java.lang.reflect.Method accessor = component.getAccessor();
                    accessor.setAccessible(true);
                    Object x = accessor.invoke(a), y = accessor.invoke(b);
                    if (!Objects.equals(x, y) && !deepSame(x, y)) {
                        return false;
                    }
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException(e);
                }
            }
            return true;
        }
        return a.equals(b);
    }

    private static String abbreviate(Object o) {
        String s = String.valueOf(o);
        return s.length() > 600 ? s.substring(0, 600) + "..." : s;
    }

    // ---- the inputs ----------------------------------------------------------------------------------------------

    private record Inputs(List<AccManifestId> accs, List<AsccManifestId> asccs, List<BccManifestId> bccs,
                          List<AsccpManifestId> asccps, List<BccpManifestId> bccps, List<DtManifestId> dts,
                          List<DtScManifestId> dtScs, List<XbtManifestId> xbts, List<String> xbtNames,
                          List<CodeListManifestId> codeLists, List<AgencyIdListManifestId> agencyIdLists,
                          List<BlobContentManifestId> blobContents, List<NamespaceId> namespaces) {
    }

    private List<BigInteger> ids(String sql, Object... bind) {
        return dsl.fetch(sql, bind).map(r -> r.get(0, ULong.class).toBigInteger());
    }

    /** Every manifest of the release. */
    private Inputs inputsOf(ReleaseId release) {
        BigInteger r = release.value();
        return new Inputs(
                map(ids("select acc_manifest_id from acc_manifest where release_id = ?", r), AccManifestId::new),
                map(ids("select ascc_manifest_id from ascc_manifest where release_id = ?", r), AsccManifestId::new),
                map(ids("select bcc_manifest_id from bcc_manifest where release_id = ?", r), BccManifestId::new),
                map(ids("select asccp_manifest_id from asccp_manifest where release_id = ?", r), AsccpManifestId::new),
                map(ids("select bccp_manifest_id from bccp_manifest where release_id = ?", r), BccpManifestId::new),
                map(ids("select dt_manifest_id from dt_manifest where release_id = ?", r), DtManifestId::new),
                map(ids("select dt_sc_manifest_id from dt_sc_manifest where release_id = ?", r), DtScManifestId::new),
                map(ids("select xbt_manifest_id from xbt_manifest where release_id = ?", r), XbtManifestId::new),
                dsl.fetch("select distinct x.name from xbt_manifest m join xbt x on x.xbt_id = m.xbt_id where m.release_id = ?", r)
                        .map(rec -> rec.get(0, String.class)),
                map(ids("select code_list_manifest_id from code_list_manifest where release_id = ?", r), CodeListManifestId::new),
                map(ids("select agency_id_list_manifest_id from agency_id_list_manifest where release_id = ?", r), AgencyIdListManifestId::new),
                map(ids("select blob_content_manifest_id from blob_content_manifest where release_id = ?", r), BlobContentManifestId::new),
                map(ids("select namespace_id from namespace"), NamespaceId::new));
    }

    /**
     * Ids that are not in the release: a few of two other releases, ones that exist nowhere, and the ones the lookups
     * of a missing component start from.
     */
    private Inputs foreignInputs(ReleaseId release, List<ReleaseId> all) {
        List<ReleaseId> others = all.stream().filter(e -> !e.equals(release)).toList();
        List<ReleaseId> sample = others.isEmpty() ? List.of() : List.of(others.get(0), others.get(others.size() - 1));
        Inputs[] foreign = sample.stream().map(this::inputsOf).toArray(Inputs[]::new);
        Function<Function<Inputs, List<? extends ManifestId>>, List<BigInteger>> take = f -> Arrays.stream(foreign)
                .flatMap(i -> f.apply(i).stream().limit(200)).map(e -> e.value()).collect(Collectors.toList());
        BigInteger missing = BigInteger.valueOf(900_000_000L);
        List<BigInteger> accs = new ArrayList<>(take.apply(Inputs::accs));
        accs.add(missing);
        List<BigInteger> asccs = new ArrayList<>(take.apply(Inputs::asccs));
        asccs.add(missing);
        List<BigInteger> bccs = new ArrayList<>(take.apply(Inputs::bccs));
        bccs.add(missing);
        List<BigInteger> asccps = new ArrayList<>(take.apply(Inputs::asccps));
        asccps.add(missing);
        List<BigInteger> bccps = new ArrayList<>(take.apply(Inputs::bccps));
        bccps.add(missing);
        List<BigInteger> dts = new ArrayList<>(take.apply(Inputs::dts));
        dts.add(missing);
        List<BigInteger> dtScs = new ArrayList<>(take.apply(Inputs::dtScs));
        dtScs.add(missing);
        List<BigInteger> xbts = new ArrayList<>(take.apply(Inputs::xbts));
        xbts.add(missing);
        List<BigInteger> codeLists = new ArrayList<>(take.apply(Inputs::codeLists));
        codeLists.add(missing);
        List<BigInteger> agencies = new ArrayList<>(take.apply(Inputs::agencyIdLists));
        agencies.add(missing);
        List<BigInteger> blobs = new ArrayList<>(take.apply(Inputs::blobContents));
        blobs.add(missing);
        return new Inputs(map(accs, AccManifestId::new), map(asccs, AsccManifestId::new), map(bccs, BccManifestId::new),
                map(asccps, AsccpManifestId::new), map(bccps, BccpManifestId::new), map(dts, DtManifestId::new),
                map(dtScs, DtScManifestId::new), map(xbts, XbtManifestId::new), List.of("no such name", ""),
                map(codeLists, CodeListManifestId::new), map(agencies, AgencyIdListManifestId::new),
                map(blobs, BlobContentManifestId::new), List.of(new NamespaceId(missing)));
    }

    private static <T> List<T> map(List<BigInteger> ids, Function<BigInteger, T> f) {
        return ids.stream().map(f).collect(Collectors.toList());
    }

    // ---- every call on every input ------------------------------------------------------------------------------

    private void checkAll(CcDocument eager, CcDocument cached, Inputs in) {
        for (AccManifestId id : in.accs()) {
            AccSummaryRecord acc = eager.getAcc(id);
            check("getAcc", id, eager, cached, d -> d.getAcc(id));
            check("getAssociations", id, eager, cached, d -> d.getAssociations(acc));
            check("getAccListByBasedAccManifestId", id, eager, cached, d -> d.getAccListByBasedAccManifestId(id));
            check("getAsccpListByRoleOfAccManifestId", id, eager, cached, d -> d.getAsccpListByRoleOfAccManifestId(id));
            check("getAsccListByFromAccManifestId", id, eager, cached, d -> d.getAsccListByFromAccManifestId(id));
            check("getBccListByFromAccManifestId", id, eager, cached, d -> d.getBccListByFromAccManifestId(id));
            check("getAssociationListByFromAccManifestId", id, eager, cached, d -> d.getAssociationListByFromAccManifestId(id));
            check("getTagListByAccManifestId", id, eager, cached, d -> d.getTagListByAccManifestId(id));
        }
        for (AsccManifestId id : in.asccs()) {
            check("getAscc", id, eager, cached, d -> d.getAscc(id));
        }
        for (BccManifestId id : in.bccs()) {
            check("getBcc", id, eager, cached, d -> d.getBcc(id));
        }
        for (AsccpManifestId id : in.asccps()) {
            check("getAsccp", id, eager, cached, d -> d.getAsccp(id));
            check("getAccListByToAsccpManifestId", id, eager, cached, d -> d.getAccListByToAsccpManifestId(id));
            check("getTagListByAsccpManifestId", id, eager, cached, d -> d.getTagListByAsccpManifestId(id));
        }
        for (BccpManifestId id : in.bccps()) {
            check("getBccp", id, eager, cached, d -> d.getBccp(id));
            check("getAccListByToBccpManifestId", id, eager, cached, d -> d.getAccListByToBccpManifestId(id));
            check("getBccListByToBccpManifestId", id, eager, cached, d -> d.getBccListByToBccpManifestId(id));
            check("getTagListByBccpManifestId", id, eager, cached, d -> d.getTagListByBccpManifestId(id));
        }
        for (DtManifestId id : in.dts()) {
            check("getDt", id, eager, cached, d -> d.getDt(id));
            check("getDtScList", id, eager, cached, d -> d.getDtScList(id));
            check("getDtScListByDtManifestId", id, eager, cached, d -> d.getDtScListByDtManifestId(id));
            check("getDtAwdPriList", id, eager, cached, d -> d.getDtAwdPriList(id));
            check("getBccpListByDtManifestId", id, eager, cached, d -> d.getBccpListByDtManifestId(id));
            check("getTagListByDtManifestId", id, eager, cached, d -> d.getTagListByDtManifestId(id));
        }
        for (DtScManifestId id : in.dtScs()) {
            check("getDtSc", id, eager, cached, d -> d.getDtSc(id));
            check("getDtScAwdPriList", id, eager, cached, d -> d.getDtScAwdPriList(id));
        }
        for (XbtManifestId id : in.xbts()) {
            check("getXbt", id, eager, cached, d -> d.getXbt(id));
        }
        for (String name : in.xbtNames()) {
            check("getXbtListByName", name, eager, cached, d -> d.getXbtListByName(name));
        }
        for (CodeListManifestId id : in.codeLists()) {
            check("getCodeList", id, eager, cached, d -> d.getCodeList(id));
        }
        for (AgencyIdListManifestId id : in.agencyIdLists()) {
            check("getAgencyIdList", id, eager, cached, d -> d.getAgencyIdList(id));
        }
        for (NamespaceId id : in.namespaces()) {
            check("getNamespace", id, eager, cached, d -> d.getNamespace(id));
        }
    }

    /** The calls that take no id, and the ones that take null. */
    private void checkWithoutId(CcDocument eager, CcDocument cached) {
        // the loaded releases list the data types in the order of a hash map, which nothing relies on
        check("getDtList (as a set)", "", eager, cached, d -> new HashSet<>(d.getDtList()));
        check("getAcc", "null", eager, cached, d -> d.getAcc(null));
        check("getAscc", "null", eager, cached, d -> d.getAscc(null));
        check("getBcc", "null", eager, cached, d -> d.getBcc(null));
        check("getAsccp", "null", eager, cached, d -> d.getAsccp(null));
        check("getBccp", "null", eager, cached, d -> d.getBccp(null));
        check("getDt", "null", eager, cached, d -> d.getDt(null));
        check("getDtSc", "null", eager, cached, d -> d.getDtSc(null));
        check("getAsccListByFromAccManifestId", "null", eager, cached, d -> d.getAsccListByFromAccManifestId(null));
        check("getBccListByFromAccManifestId", "null", eager, cached, d -> d.getBccListByFromAccManifestId(null));
        check("getDtScListByDtManifestId", "null", eager, cached, d -> d.getDtScListByDtManifestId(null));
        check("getTagListByAccManifestId", "null", eager, cached, d -> d.getTagListByAccManifestId(null));
        check("getAssociations", "null", eager, cached, d -> d.getAssociations(null));
        check("getXbt", "null", eager, cached, d -> d.getXbt(null));
    }

    // ---- the graphs ----------------------------------------------------------------------------------------------

    private static final List<String> GRAPH_KINDS = List.of("acc", "asccp", "bccp", "dt");

    /** The graph {@code GraphService} builds for a root, made over the given document, as the JSON the API sends. */
    private String graphJson(GraphService graphService, CcDocument document, Node root) throws Exception {
        Graph graph = ReflectionTestUtils.invokeMethod(graphService, "buildGraph",
                new CoreComponentGraphContext(document), root, false);
        return JSON.writeValueAsString(graph);
    }

    private void checkGraphs(GraphService graphService, CcDocument eager, CcDocument cached, Inputs in) throws Exception {
        List<Node> roots = new ArrayList<>();
        pick(in.accs(), GRAPH_ROOTS_PER_KIND).forEach(id -> roots.add(
                new CoreComponentGraphContext(eager).toNode(eager.getAcc(id))));
        pick(in.asccps(), GRAPH_ROOTS_PER_KIND).forEach(id -> roots.add(
                new CoreComponentGraphContext(eager).toNode(eager.getAsccp(id))));
        pick(in.bccps(), GRAPH_ROOTS_PER_KIND).forEach(id -> roots.add(
                new CoreComponentGraphContext(eager).toNode(eager.getBccp(id))));
        pick(in.dts(), GRAPH_ROOTS_PER_KIND).forEach(id -> roots.add(
                new CoreComponentGraphContext(eager).toNode(eager.getDt(id))));
        for (Node root : roots) {
            Node cachedRoot = root; // a node is only a value: the same root starts both walks
            String expected = graphJson(graphService, eager, root);
            String actual = graphJson(graphService, cached, cachedRoot);
            Stats stats = stats("graph");
            stats.checked.incrementAndGet();
            if (!expected.equals(actual)) {
                stats.different.incrementAndGet();
                if (stats.examples.size() < EXAMPLES_PER_METHOD) {
                    stats.examples.add("graph(" + root.getKey() + ") differs: " + expected.length() + " vs " + actual.length() + " chars");
                }
            }
        }
    }

    private static <T> List<T> pick(List<T> list, int n) {
        if (list.size() <= n) {
            return list;
        }
        List<T> picked = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            picked.add(list.get(i * (list.size() - 1) / (n - 1)));
        }
        return picked;
    }

    // ---- the run -------------------------------------------------------------------------------------------------

    private static final String RUN_PREFIX = "zz-cc-equivalence:" + UUID.randomUUID() + ":";
    private static final AtomicLong CACHES = new AtomicLong();
    private static LettuceConnectionFactory redis;

    /** The cache the application would use: Caffeine, or Redis with {@code -Dscore.equivalence.cache=redis}. */
    private static CcCache freshCache() {
        if ("redis".equals(System.getProperty("score.equivalence.cache"))) {
            synchronized (CACHES) {
                if (redis == null) {
                    redis = new LettuceConnectionFactory(new RedisStandaloneConfiguration(
                            System.getProperty("score.redis.host", "localhost"), Integer.getInteger("score.redis.port", 6379)));
                    redis.afterPropertiesSet();
                }
            }
            return new RedisCcCache(redis, RUN_PREFIX + CACHES.incrementAndGet() + ":", org.oagi.score.gateway.http.api.cc_management.cache.CcCacheStatus.alwaysReady(), java.time.Duration.ofMinutes(30));
        }
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCacheSpecification(CACHE_SPEC);
        return new SpringCcCache(manager, org.oagi.score.gateway.http.api.cc_management.cache.CcCacheStatus.alwaysReady());
    }

    @AfterAll
    static void removeWhatTheRunLeftInRedis() {
        if (redis != null) {
            try (var connection = redis.getConnection()) {
                var keys = connection.keyCommands().keys((RUN_PREFIX + "*").getBytes());
                if (keys != null && !keys.isEmpty()) {
                    connection.keyCommands().del(keys.toArray(new byte[0][]));
                }
            }
            redis.destroy();
        }
    }

    private RepositoryFactory repositoryFactoryWithFreshCache() {
        RepositoryFactory repositoryFactory = new RepositoryFactory(dsl);
        ReflectionTestUtils.setField(repositoryFactory, "ccCache", freshCache());
        return repositoryFactory;
    }

    private GraphService graphServiceOver(RepositoryFactory repositoryFactory) {
        GraphService graphService = new GraphService();
        ReflectionTestUtils.setField(graphService, "repositoryFactory", repositoryFactory);
        return graphService;
    }

    private void verifyRelease(ReleaseId release, List<ReleaseId> all) throws Exception {
        Inputs own = inputsOf(release);
        Inputs foreign = foreignInputs(release, all);

        // 1. Point lookups against an empty cache, the way the first requests find it.
        RepositoryFactory cold = repositoryFactoryWithFreshCache();
        CcDocument eager = new CcDocumentImpl(REQUESTER, cold, release);
        CcDocument cached = new CachedCcDocument(REQUESTER, cold, release);
        checkWithoutId(eager, cached);
        checkAll(eager, cached, foreign);
        checkAll(eager, cached, own);
        // ... and again, now that the cache holds what the first pass read.
        checkAll(eager, cached, own);

        // 2. What a prefetched graph leaves in the cache, and the graphs themselves.
        RepositoryFactory prefetched = repositoryFactoryWithFreshCache();
        CcDocument cachedAfterPrefetch = new CachedCcDocument(REQUESTER, prefetched, release);
        for (AccManifestId id : pick(own.accs(), GRAPH_ROOTS_PER_KIND)) {
            cachedAfterPrefetch.prefetchDependencies(CcType.ACC, id);
        }
        for (AsccpManifestId id : pick(own.asccps(), GRAPH_ROOTS_PER_KIND)) {
            cachedAfterPrefetch.prefetchDependencies(CcType.ASCCP, id);
        }
        for (BccpManifestId id : pick(own.bccps(), GRAPH_ROOTS_PER_KIND)) {
            cachedAfterPrefetch.prefetchDependencies(CcType.BCCP, id);
        }
        for (DtManifestId id : pick(own.dts(), GRAPH_ROOTS_PER_KIND)) {
            cachedAfterPrefetch.prefetchDependencies(CcType.DT, id);
        }
        checkAll(eager, cachedAfterPrefetch, own);

        // 3. What the application gets from the factory (the cached document with the fallback), used by one thread
        //    and then by many, as the exports do.
        CcDocument fromFactory = repositoryFactoryWithFreshCache().ccDocumentFactory().create(REQUESTER, release);
        checkAll(eager, fromFactory, own);
        CcDocument sharedByThreads = repositoryFactoryWithFreshCache().ccDocumentFactory().create(REQUESTER, release);
        own.accs().parallelStream().forEach(id -> {
            check("shared getAcc", id, eager, sharedByThreads, d -> d.getAcc(id));
            check("shared getAsccListByFromAccManifestId", id, eager, sharedByThreads, d -> d.getAsccListByFromAccManifestId(id));
            check("shared getAssociations", id, eager, sharedByThreads, d -> d.getAssociations(d.getAcc(id)));
        });
        own.dts().parallelStream().forEach(id -> {
            check("shared getDtScList", id, eager, sharedByThreads, d -> d.getDtScList(id));
            check("shared getDtAwdPriList", id, eager, sharedByThreads, d -> d.getDtAwdPriList(id));
        });
        checkGraphs(graphServiceOver(prefetched), eager, new CachedCcDocument(REQUESTER, repositoryFactoryWithFreshCache(), release), own);
    }

    @Test
    void everyOutputIsTheSameForEveryInput() throws Exception {
        List<ReleaseId> all = map(ids("select release_id from `release` order by release_id"), ReleaseId::new);
        String only = System.getProperty("score.equivalence.releases");
        List<ReleaseId> releases = (only == null || only.isBlank()) ? all : Arrays.stream(only.split(","))
                .map(e -> new ReleaseId(new BigInteger(e.trim()))).collect(Collectors.toList());

        int threads = Integer.getInteger("score.equivalence.threads", 8);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        long started = System.nanoTime();
        List<Future<?>> futures = new ArrayList<>();
        for (ReleaseId release : releases) {
            futures.add(pool.submit(() -> {
                verifyRelease(release, all);
                System.out.printf("release %s done (%d s)%n", release.value(), (System.nanoTime() - started) / 1_000_000_000L);
                return null;
            }));
        }
        for (Future<?> future : futures) {
            future.get();
        }
        pool.shutdown();

        StringBuilder out = new StringBuilder("\n%-42s %12s %10s %10s%n".formatted("call", "compared", "different", "order only"));
        long checked = 0, different = 0, orderOnly = 0;
        for (Map.Entry<String, Stats> e : new TreeMap<>(report).entrySet()) {
            Stats s = e.getValue();
            out.append("%-42s %12d %10d %10d%n".formatted(e.getKey(), s.checked.get(), s.different.get(), s.orderOnly.get()));
            checked += s.checked.get();
            different += s.different.get();
            orderOnly += s.orderOnly.get();
        }
        out.append("%-42s %12d %10d %10d%n".formatted("TOTAL", checked, different, orderOnly));
        report.forEach((method, s) -> s.examples.forEach(example -> out.append("\n").append(example)));
        System.out.println(out);

        assertTrue(checked > 0);
        assertEquals(0, different + orderOnly, "outputs that differ (see the report above)");
    }

}
