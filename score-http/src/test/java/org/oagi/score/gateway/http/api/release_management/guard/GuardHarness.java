package org.oagi.score.gateway.http.api.release_management.guard;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.mockito.Mockito;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.service.AgencyIdListCommandService;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpType;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.service.CcCommandService;
import org.oagi.score.gateway.http.api.cc_management.service.ComponentStateChangeEventPublisher;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.service.CodeListCommandService;
import org.oagi.score.gateway.http.api.graph.repository.GraphContextRepository;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.controller.payload.CreateReleaseRequest;
import org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationRequest;
import org.oagi.score.gateway.http.api.release_management.controller.payload.TransitStateRequest;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseProgressRepository;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseCommandService;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseInProgressGuard;
import org.oagi.score.gateway.http.api.message_management.service.MessageCommandService;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseChangeNotifier;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseFailureReporter;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseProgressRecovery;
import org.oagi.score.gateway.http.common.TestDataSource;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.event.AfterCommitEventPublisher;
import org.oagi.score.gateway.http.common.model.event.EventListenerContainer;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The release services as the application runs them (the Spring proxies, and the transactions of Spring), on the database of
 * the development, with what is outside of the services (Redis, the session) replaced. The events the services publish are
 * collected, and handed to the handlers by the test, so that it decides when the handler runs.
 *
 * <p>The tests commit, as the application does, so they run on a database of their own, which they refuse to leave:
 * {@code -DDB_DATABASE=oagi_release_guard}.</p>
 */
public final class GuardHarness implements AutoCloseable {

    public static final String LIBRARY = "connectSpec";

    public static final ScoreUser REQUESTER = new ScoreUser(new UserId(BigInteger.ZERO), "sysadm", "sysadm",
            "sysadm@example.com", true, List.of(ScoreRole.DEVELOPER, ScoreRole.ADMINISTRATOR));

    /** The kinds of component a release holds, as the tests move them. */
    public enum K {
        ACC("acc"), ASCCP("asccp"), BCCP("bccp"), DT("dt"), CODE_LIST("code_list"), AGENCY_ID_LIST("agency_id_list");

        public final String table;

        K(String table) {
            this.table = table;
        }

        public String manifest() {
            return table + "_manifest";
        }

        public String manifestId() {
            return table + "_manifest_id";
        }

        public String record() {
            return table + "_id";
        }
    }

    /** Collects what is published, once the transaction that publishes it has committed. */
    public static class CollectingPublisher extends AfterCommitEventPublisher {
        public final List<Object> events = new CopyOnWriteArrayList<>();

        @Override
        public void publish(String channel, Object event) {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        events.add(event);
                    }
                });
            } else {
                events.add(event);
            }
        }
    }

    // not a @Configuration: the application would find it when it scans for them, and take its beans as its own
    @EnableTransactionManagement(order = ReleaseInProgressGuard.TRANSACTION_ORDER)
    @EnableAspectJAutoProxy
    static class Context {

        @Bean(destroyMethod = "close")
        TestDataSource dataSource() {
            return TestDataSource.open(30);
        }

        @Bean
        PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        FaultInjector faultInjector() {
            return new FaultInjector();
        }

        @Bean
        DSLContext dslContext(DataSource dataSource, FaultInjector injector) {
            return new DefaultDSLContext(new DefaultConfiguration()
                    .set(SQLDialect.MARIADB)
                    .set(new DataSourceConnectionProvider(new TransactionAwareDataSourceProxy(dataSource)))
                    .set(injector));
        }

        @Bean
        RepositoryFactory repositoryFactory(DSLContext dslContext) {
            return new RepositoryFactory(dslContext);
        }

        @Bean
        ReleaseInProgressGuard releaseInProgressGuard() {
            return new ReleaseInProgressGuard();
        }

        @Bean
        ReleaseChangeNotifier releaseChangeNotifier(RepositoryFactory repositoryFactory) {
            return new ReleaseChangeNotifier(Mockito.mock(org.springframework.messaging.simp.SimpMessagingTemplate.class),
                    repositoryFactory);
        }

        @Bean
        ReleaseFailureReporter releaseFailureReporter(RepositoryFactory repositoryFactory, SessionService sessionService,
                                                      MessageCommandService messageCommandService,
                                                      ReleaseChangeNotifier changeNotifier) {
            return new ReleaseFailureReporter(repositoryFactory, sessionService, messageCommandService, changeNotifier);
        }

        @Bean
        ReleaseProgressRecovery releaseProgressRecovery(RepositoryFactory repositoryFactory,
                                                        ReleaseFailureReporter failureReporter,
                                                        PlatformTransactionManager transactionManager) {
            return new ReleaseProgressRecovery(repositoryFactory, failureReporter, transactionManager,
                    java.time.Duration.ofMinutes(30));
        }

        @Bean
        CcCommandService ccCommandService() {
            return new CcCommandService();
        }

        @Bean
        CodeListCommandService codeListCommandService() {
            return new CodeListCommandService();
        }

        @Bean
        AgencyIdListCommandService agencyIdListCommandService() {
            return new AgencyIdListCommandService();
        }

        @Bean
        ReleaseCommandService releaseCommandService() {
            return new ReleaseCommandService();
        }
    }

    private final AnnotationConfigApplicationContext context;
    public final DSLContext dsl;
    public final RepositoryFactory factory;
    public final CcCommandService ccService;
    public final CodeListCommandService codeListService;
    public final AgencyIdListCommandService agencyIdListService;
    public final ReleaseCommandService releaseService;
    public final CollectingPublisher publisher;
    public final ReleaseProgressRepository progress;
    public final ReleaseProgressRecovery recovery;
    public final MessageCommandService messages;
    public final PlatformTransactionManager transactionManager;

    private final AtomicInteger serial = new AtomicInteger();
    private List<String> releaseTables;

    public static GuardHarness open() {
        String database = System.getProperty("DB_DATABASE", System.getenv("DB_DATABASE"));
        if (!"oagi_release_guard".equals(database)) {
            throw new IllegalStateException("The release guard tests commit to the database, so they only run on "
                    + "'oagi_release_guard' (-DDB_DATABASE=oagi_release_guard), not on '" + database + "'.");
        }
        // MariaDB 11.6.2 and later refuse by themselves to update a row another transaction has changed since the snapshot
        // (innodb_snapshot_isolation), which hides a lost update; MySQL and the older MariaDB do not. The tests must not
        // depend on that, so the sessions go without it, unless -Dguard.snapshotIsolation=on says to keep it.
        if (!"on".equals(System.getProperty("guard.snapshotIsolation"))) {
            System.setProperty("spring.datasource.hikari.connection-init-sql", "SET SESSION innodb_snapshot_isolation=OFF");
        }
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(Context.class);
        // mocks are registered as they are: the beans of the application inject their fields into what they are given
        SessionService sessionService = Mockito.mock(SessionService.class);
        Mockito.when(sessionService.getScoreUserByUserId(Mockito.any())).thenReturn(REQUESTER);
        Mockito.when(sessionService.getScoreSystemUser()).thenReturn(REQUESTER);
        context.getBeanFactory().registerSingleton("messageCommandService", Mockito.mock(MessageCommandService.class));
        context.getBeanFactory().registerSingleton("sessionService", sessionService);
        context.getBeanFactory().registerSingleton("graphContextRepository", Mockito.mock(GraphContextRepository.class));
        context.getBeanFactory().registerSingleton("componentStateChangeEventPublisher",
                Mockito.mock(ComponentStateChangeEventPublisher.class));
        RedissonClient redisson = Mockito.mock(RedissonClient.class);
        RLock lock = Mockito.mock(RLock.class);
        try {
            Mockito.when(lock.tryLock()).thenReturn(true);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Mockito.when(redisson.getLock(Mockito.anyString())).thenReturn(lock);
        context.getBeanFactory().registerSingleton("redissonClient", redisson);
        context.getBeanFactory().registerSingleton("eventListenerContainer", Mockito.mock(EventListenerContainer.class));
        context.getBeanFactory().registerSingleton("afterCommitEventPublisher", new CollectingPublisher());
        context.refresh();
        return new GuardHarness(context);
    }

    private GuardHarness(AnnotationConfigApplicationContext context) {
        this.context = context;
        this.dsl = context.getBean(DSLContext.class);
        this.factory = context.getBean(RepositoryFactory.class);
        this.ccService = context.getBean(CcCommandService.class);
        this.codeListService = context.getBean(CodeListCommandService.class);
        this.agencyIdListService = context.getBean(AgencyIdListCommandService.class);
        this.releaseService = context.getBean(ReleaseCommandService.class);
        this.publisher = context.getBean(CollectingPublisher.class);
        this.progress = factory.releaseProgressRepository();
        this.recovery = context.getBean(ReleaseProgressRecovery.class);
        this.messages = context.getBean(MessageCommandService.class);
        this.transactionManager = context.getBean(PlatformTransactionManager.class);
    }

    @Override
    public void close() {
        context.close();
    }

    // ---- the data ----

    public long one(String sql, Object... bindings) {
        return dsl.fetchOne(sql, bindings).get(0, Long.class);
    }

    static BigInteger n(long v) {
        return BigInteger.valueOf(v);
    }

    /** A new release of the library, and one new component of every kind, in 'Working', all of them {@code Candidate}. */
    public Arena arrange() {
        long library = one("select library_id from library where name = ?", LIBRARY);
        long working = one("select release_id from `release` where library_id = ? and release_num = 'Working'", library);
        long namespace = one("select namespace_id from `release` where release_id = ?", working);
        ReleaseId release = releaseService.create(REQUESTER, new CreateReleaseRequest(new LibraryId(n(library)),
                new NamespaceId(n(namespace)), "g" + System.nanoTime() + "-" + serial.incrementAndGet(), null, null));

        long anchorAcc = one("select m.acc_manifest_id from acc_manifest m join acc x using (acc_id) where m.release_id = ? "
                + "and x.state = 'Published' and m.prev_acc_manifest_id is not null order by m.acc_manifest_id limit 1", working);
        long anchorDt = one("select m.dt_manifest_id from dt_manifest m join dt x using (dt_id) where m.release_id = ? "
                + "and x.state = 'Published' and m.prev_dt_manifest_id is not null and m.based_dt_manifest_id in "
                + "(select dt_manifest_id from dt_manifest where release_id = " + working + ") and exists "
                + "(select 1 from dt_sc_manifest s where s.owner_dt_manifest_id = m.dt_manifest_id) "
                + "order by m.dt_manifest_id limit 1", working);

        ReleaseId workingId = new ReleaseId(n(working));
        NamespaceId namespaceId = new NamespaceId(n(namespace));
        String term = "Guard " + System.nanoTime() + " " + serial.incrementAndGet();
        Map<K, Long> components = new LinkedHashMap<>();
        components.put(K.ACC, factory.accCommandRepository(REQUESTER)
                .create(workingId, null, term + " a", null, null, null, namespaceId).value().longValue());
        components.put(K.ASCCP, factory.asccpCommandRepository(REQUESTER)
                .create(workingId, new AccManifestId(n(anchorAcc)), term + " b", AsccpType.Default, true, null,
                        namespaceId, null).value().longValue());
        components.put(K.BCCP, factory.bccpCommandRepository(REQUESTER)
                .create(workingId, new DtManifestId(n(anchorDt)), term + " c").value().longValue());
        components.put(K.DT, factory.dtCommandRepository(REQUESTER)
                .create(workingId, new DtManifestId(n(anchorDt))).value().longValue());
        components.put(K.CODE_LIST, factory.codeListCommandRepository(REQUESTER)
                .create(workingId, null).value().longValue());
        components.put(K.AGENCY_ID_LIST, factory.agencyIdListCommandRepository(REQUESTER)
                .create(workingId, null).value().longValue());
        components.forEach((k, id) -> dsl.execute("update `" + k.table + "` set state = 'Candidate', namespace_id = ? "
                + "where `" + k.record() + "` = (select `" + k.record() + "` from `" + k.manifest() + "` where `"
                + k.manifestId() + "` = ?)", namespace, id));

        ReleaseValidationRequest request = new ReleaseValidationRequest();
        request.setReleaseId(release);
        request.setAssignedAccComponentManifestIds(List.of(new AccManifestId(n(components.get(K.ACC)))));
        request.setAssignedAsccpComponentManifestIds(List.of(new AsccpManifestId(n(components.get(K.ASCCP)))));
        request.setAssignedBccpComponentManifestIds(List.of(new BccpManifestId(n(components.get(K.BCCP)))));
        request.setAssignedDtComponentManifestIds(List.of(new DtManifestId(n(components.get(K.DT)))));
        request.setAssignedCodeListComponentManifestIds(List.of(new CodeListManifestId(n(components.get(K.CODE_LIST)))));
        request.setAssignedAgencyIdListComponentManifestIds(
                List.of(new AgencyIdListManifestId(n(components.get(K.AGENCY_ID_LIST)))));
        return new Arena(this, release, working, components, request);
    }

    private List<String> releaseTables() {
        if (releaseTables == null) {
            releaseTables = dsl.fetch("select table_name from information_schema.columns where table_schema = database() "
                    + "and column_name = 'release_id' and table_name not in ('release', 'record_change_log') order by table_name")
                    .map(r -> r.get(0, String.class));
        }
        return releaseTables;
    }

    /** The rows that belong to the release, over every table that has a release. */
    long rowsOf(long release) {
        long rows = 0;
        for (String table : releaseTables()) {
            rows += one("select count(*) from `" + table + "` where release_id = ?", release);
        }
        return rows;
    }

    /** The tables that hold rows of the release, and how many. */
    public Map<String, Long> rowsByTable(long release) {
        Map<String, Long> rows = new LinkedHashMap<>();
        for (String table : releaseTables()) {
            long count = one("select count(*) from `" + table + "` where release_id = ?", release);
            if (count != 0) {
                rows.put(table, count);
            }
        }
        return rows;
    }

    /** Removes what is left of a release: what the application does when a draft goes back to 'Initialized', and the rest. */
    void remove(long releaseValue, Map<K, Long> components) {
        ReleaseId release = new ReleaseId(n(releaseValue));
        try {
            factory.releaseCommandRepository(REQUESTER).deleteDeps(release);
            var tagQuery = factory.tagQueryRepository(REQUESTER);
            var tagCommand = factory.tagCommandRepository(REQUESTER);
            tagQuery.getAccManifestTagList(List.of(release)).forEach(t -> tagCommand.removeTag(t.tagId(), t.accManifestId()));
            tagQuery.getAsccpManifestTagList(List.of(release)).forEach(t -> tagCommand.removeTag(t.tagId(), t.asccpManifestId()));
            tagQuery.getBccpManifestTagList(List.of(release)).forEach(t -> tagCommand.removeTag(t.tagId(), t.bccpManifestId()));
            tagQuery.getDtManifestTagList(List.of(release)).forEach(t -> tagCommand.removeTag(t.tagId(), t.dtManifestId()));
            factory.gitHubIssueLinkCommandRepository(REQUESTER).deleteLinksByRelease(release);
            factory.bieViewOrderCommandRepository(REQUESTER).deleteByRelease(release);
            factory.ccCommandRepository(REQUESTER).clearReplacement(release);
            factory.ccCommandRepository(REQUESTER).delete(release);
        } catch (RuntimeException ignored) {
            // what is left is removed below
        }
        for (int pass = 0; pass < 8; pass++) {
            boolean left = false;
            for (String table : releaseTables()) {
                try {
                    dsl.execute("delete from `" + table + "` where release_id = ?", releaseValue);
                } catch (RuntimeException e) {
                    left = true;
                }
            }
            if (!left) {
                break;
            }
        }
        try {
            dsl.execute("delete from `release` where release_id = ?", releaseValue);
        } catch (RuntimeException ignored) {
            // a release that is still referred to is left
        }
        // the components of the test are not to be taken by a later release, which moves every component that is 'ReleaseDraft'
        components.forEach((k, id) -> dsl.execute("update `" + k.table + "` set state = 'WIP' where state in "
                + "('Candidate', 'ReleaseDraft') and `" + k.record() + "` = (select `" + k.record() + "` from `"
                + k.manifest() + "` where `" + k.manifestId() + "` = ?)", id));
    }

    // ---- what the tests observe ----

    /** What a release and its components are, and how many rows the release has. */
    public record Observed(String releaseState, Map<K, String> states, long rows) {
    }

    public Observed observe(Arena arena) {
        Map<K, String> states = new LinkedHashMap<>();
        arena.components().forEach((k, id) -> states.put(k, dsl.fetchOne("select x.state from `" + k.table + "` x join `"
                + k.manifest() + "` m on m.`" + k.record() + "` = x.`" + k.record() + "` where m.`" + k.manifestId()
                + "` = ? and m.release_id = ?", id, arena.working()).get(0, String.class)));
        Record release = dsl.fetchOne("select state from `release` where release_id = ?", arena.release().value().longValue());
        return new Observed(release == null ? "(deleted)" : release.get(0, String.class), states, rowsOf(arena.release().value().longValue()));
    }

    // ---- what the users do ----

    public void updateState(K kind, long manifest, CcState state) {
        switch (kind) {
            case ACC -> ccService.updateState(REQUESTER, new AccManifestId(n(manifest)), state);
            case ASCCP -> ccService.updateState(REQUESTER, new AsccpManifestId(n(manifest)), state);
            case BCCP -> ccService.updateState(REQUESTER, new BccpManifestId(n(manifest)), state);
            case DT -> ccService.updateState(REQUESTER, new DtManifestId(n(manifest)), state);
            case CODE_LIST -> codeListService.updateState(REQUESTER, new CodeListManifestId(n(manifest)), state);
            case AGENCY_ID_LIST -> agencyIdListService.updateState(REQUESTER, new AgencyIdListManifestId(n(manifest)), state);
        }
    }

    public TransitStateRequest transit(Arena arena, String state) {
        TransitStateRequest request = new TransitStateRequest();
        request.setReleaseId(arena.release());
        request.setState(state);
        request.setValidationRequest(arena.request());
        return request;
    }

    /** The events that have been published and not handled yet. */
    public List<Object> drainEvents() {
        List<Object> drained = new ArrayList<>(publisher.events);
        publisher.events.clear();
        return drained;
    }
}
