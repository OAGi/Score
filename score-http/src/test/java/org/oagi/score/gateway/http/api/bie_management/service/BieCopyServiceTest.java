package org.oagi.score.gateway.http.api.bie_management.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.event.BieCopyRequestEvent;
import org.oagi.score.gateway.http.api.bie_management.repository.BieCommandRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.TopLevelAsbiepCommandRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.TopLevelAsbiepQueryRepository;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.NotFoundException;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.oagi.score.gateway.http.common.model.event.AfterCommitEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The copy request is published after the new BIE row is committed, an unknown source is a 404,
 * and a failed copy neither stays half-written nor leaves the Initiating BIE behind.
 */
class BieCopyServiceTest {

    private static final TopLevelAsbiepId SOURCE_ID = new TopLevelAsbiepId(BigInteger.TEN);
    private static final TopLevelAsbiepId COPY_ID = new TopLevelAsbiepId(BigInteger.valueOf(11));
    private static final List<BusinessContextId> BIZ_CTX = List.of(new BusinessContextId(BigInteger.ONE));

    private final UserId userId = new UserId(BigInteger.ONE);
    private final ScoreUser requester = new ScoreUser(userId, "eu", "End User", "eu@example.com", true, List.of(ScoreRole.END_USER));

    private BieCopyService service;
    private RepositoryFactory repositoryFactory;
    private TopLevelAsbiepQueryRepository topLevelQuery;
    private TopLevelAsbiepCommandRepository topLevelCommand;
    private BieCommandRepository bieCommand;
    private RedisTemplate redisTemplate;
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        service = new BieCopyService();
        repositoryFactory = mock(RepositoryFactory.class);
        topLevelQuery = mock(TopLevelAsbiepQueryRepository.class);
        topLevelCommand = mock(TopLevelAsbiepCommandRepository.class);
        bieCommand = mock(BieCommandRepository.class);
        redisTemplate = mock(RedisTemplate.class);
        transactionManager = mock(PlatformTransactionManager.class);
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));

        when(repositoryFactory.topLevelAsbiepQueryRepository(any())).thenReturn(topLevelQuery);
        when(repositoryFactory.topLevelAsbiepCommandRepository(any())).thenReturn(topLevelCommand);
        when(repositoryFactory.bieCommandRepository(any())).thenReturn(bieCommand);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        AfterCommitEventPublisher eventPublisher = new AfterCommitEventPublisher();
        ReflectionTestUtils.setField(eventPublisher, "redisTemplate", redisTemplate);
        ReflectionTestUtils.setField(service, "eventPublisher", eventPublisher);
        ReflectionTestUtils.setField(service, "transactionManager", transactionManager);
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private TopLevelAsbiepSummaryRecord source() {
        TopLevelAsbiepSummaryRecord source = mock(TopLevelAsbiepSummaryRecord.class);
        ReleaseSummaryRecord release = mock(ReleaseSummaryRecord.class);
        when(release.releaseId()).thenReturn(new ReleaseId(BigInteger.ONE));
        when(source.release()).thenReturn(release);
        return source;
    }

    @Test
    void anUnknownSourceBieIsNotFound() {
        when(topLevelQuery.getTopLevelAsbiepSummary(SOURCE_ID)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.copyBie(requester, SOURCE_ID, BIZ_CTX));
        verify(redisTemplate, never()).convertAndSend(any(String.class), any(Object.class));
    }

    @Test
    void theCopyRequestIsPublishedOnlyAfterTheCommit() {
        TopLevelAsbiepSummaryRecord source = source();
        when(topLevelQuery.getTopLevelAsbiepSummary(SOURCE_ID)).thenReturn(source);
        when(topLevelCommand.insertTopLevelAsbiep(any())).thenReturn(COPY_ID);
        TransactionSynchronizationManager.initSynchronization();

        service.copyBie(requester, SOURCE_ID, BIZ_CTX);

        verify(redisTemplate, never()).convertAndSend(any(String.class), any(Object.class));
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        verify(redisTemplate).convertAndSend(eq("bieCopyRequestEvent"), any(Object.class));
    }

    @Test
    void aFailedCopyRemovesTheInitiatingBie() {
        SessionService sessionService = mock(SessionService.class);
        when(sessionService.getScoreUserByUserId(userId)).thenReturn(requester);
        RedissonClient redissonClient = mock(RedissonClient.class);
        RLock lock = mock(RLock.class);
        when(lock.tryLock()).thenReturn(true);
        when(redissonClient.getLock(any(String.class))).thenReturn(lock);
        ReflectionTestUtils.setField(service, "sessionService", sessionService);
        ReflectionTestUtils.setField(service, "redissonClient", redissonClient);
        // the source cannot be read, so the copy fails half-way
        when(topLevelQuery.getTopLevelAsbiepSummary(SOURCE_ID)).thenThrow(new IllegalStateException("boom"));

        service.onEventReceived(new BieCopyRequestEvent(userId, SOURCE_ID, COPY_ID, BIZ_CTX));

        verify(bieCommand).deleteByTopLevelAsbiepIdList((Collection<TopLevelAsbiepId>) List.of(COPY_ID));
        verify(lock).unlock();
    }
}
