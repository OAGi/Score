package org.oagi.score.gateway.http.api.integration_management.github.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.event.ComponentStateChangeEvent;
import org.oagi.score.gateway.http.api.cc_management.service.ComponentStateChangeEventPublisher;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.integration_management.github.config.GitHubIntegrationProperties;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueId;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueLinkRecord;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the component-state-change queue consumer behind the GitHub status post
 * (issue #1533, sub-tasks 4–5): the {@code {fromState}_{toState}} dispatch, the verbatim
 * pass-through of the user-approved comment, the comment-less opt-out, the fan-out over every
 * linked issue, and the best-effort guarantees (no linked issues / not configured / failures
 * swallowed).
 */
@ExtendWith(MockitoExtension.class)
class GitHubStatusPostEventSubscriberTest {

    private static final UserId USER_ID = new UserId(BigInteger.ONE);

    private static final BigInteger MANIFEST_ID = BigInteger.TEN;

    private static final String COMMENT = "### ACC \"Item Base. Details\" (rev. 2) moved to Candidate\n"
            + "\nChanges from Rev. 1:\n"
            + "\n- Definition: \"old\" → \"new\"\n"
            + "\nEdited by the acting user before confirming.\n";

    private final GitHubStatusPostEventSubscriber subscriber = new GitHubStatusPostEventSubscriber();

    private final GitHubIntegrationProperties properties = mock(GitHubIntegrationProperties.class);

    private final GitHubIntegrationService integrationService = mock(GitHubIntegrationService.class);

    private final GitHubIssueLinkService issueLinkService = mock(GitHubIssueLinkService.class);

    private final SessionService sessionService = mock(SessionService.class);

    private final RedissonClient redissonClient = mock(RedissonClient.class);

    private final RRateLimiter rateLimiter = mock(RRateLimiter.class);

    private final ScoreUser requester = new ScoreUser(
            USER_ID, "user", "User", null, false, List.of(ScoreRole.DEVELOPER));

    GitHubStatusPostEventSubscriberTest() {
        ReflectionTestUtils.setField(subscriber, "properties", properties);
        ReflectionTestUtils.setField(subscriber, "integrationService", integrationService);
        ReflectionTestUtils.setField(subscriber, "issueLinkService", issueLinkService);
        ReflectionTestUtils.setField(subscriber, "sessionService", sessionService);
        ReflectionTestUtils.setField(subscriber, "redissonClient", redissonClient);
    }

    private static ManifestId manifestId(CcType ccType) {
        return switch (ccType) {
            case CODE_LIST -> new CodeListManifestId(MANIFEST_ID);
            case DT -> new DtManifestId(MANIFEST_ID);
            default -> new AccManifestId(MANIFEST_ID);
        };
    }

    private static ComponentStateChangeEvent event(CcType ccType, CcState prevState, CcState nextState) {
        return event(ccType, prevState, nextState, COMMENT);
    }

    private static ComponentStateChangeEvent event(CcType ccType, CcState prevState, CcState nextState, String comment) {
        return new ComponentStateChangeEvent(ccType, manifestId(ccType), prevState, nextState, USER_ID, "event-1", comment);
    }

    private static GitHubIssueLinkRecord link(int issueNumber) {
        return new GitHubIssueLinkRecord(null, new GitHubIssueId(BigInteger.ONE),
                "OAGi", "oagis", issueNumber, null);
    }

    /** Posting set-up shared by the transition tests: configured, two linked issues. */
    private void expectPost(ComponentStateChangeEvent event) {
        when(properties.isConfigured()).thenReturn(true);
        when(sessionService.getScoreUserByUserId(USER_ID)).thenReturn(requester);
        when(issueLinkService.getLinkedIssueRefs(requester, event.getCcType(), event.getManifestId()))
                .thenReturn(List.of(link(1097), link(1533)));
        when(redissonClient.getRateLimiter(anyString())).thenReturn(rateLimiter);
    }

    @Test
    void subscribesToTheEventQueueOnStartupWithTheSharedJsonCodec() {
        RBlockingQueue queue = mock(RBlockingQueue.class);
        when(redissonClient.getBlockingQueue(
                ComponentStateChangeEventPublisher.COMPONENT_STATE_CHANGE_EVENT_QUEUE,
                ComponentStateChangeEventPublisher.COMPONENT_STATE_CHANGE_EVENT_CODEC)).thenReturn(queue);

        subscriber.afterPropertiesSet();

        verify(queue).subscribeOnElements(any(Consumer.class));
        // The publisher and this subscriber must address the queue with the same codec.
        verify(redissonClient).getBlockingQueue(anyString(), any(Codec.class));
    }

    @Test
    void candidatePromotionPostsTheCommentVerbatimOnEveryLinkedIssue() {
        ComponentStateChangeEvent event = event(CcType.ACC, CcState.Draft, CcState.Candidate);
        expectPost(event);

        subscriber.onComponentStateChangeEventReceived(event);

        verify(integrationService).postIssueComment(requester, "OAGi", "oagis", 1097, COMMENT);
        verify(integrationService).postIssueComment(requester, "OAGi", "oagis", 1533, COMMENT);
    }

    @Test
    void revertToWipPostsTheCommentVerbatim() {
        ComponentStateChangeEvent event = event(CcType.CODE_LIST, CcState.Candidate, CcState.WIP,
                "Pulled back for rework.");
        expectPost(event);

        subscriber.onComponentStateChangeEventReceived(event);

        verify(integrationService).postIssueComment(requester, "OAGi", "oagis", 1097, "Pulled back for rework.");
        verify(integrationService).postIssueComment(requester, "OAGi", "oagis", 1533, "Pulled back for rework.");
    }

    @Test
    void aCommentLessEventPostsNothing() {
        // The user cleared the pre-filled comment box: the empty comment is the opt-out.
        subscriber.onComponentStateChangeEventReceived(
                event(CcType.ACC, CcState.Draft, CcState.Candidate, null));

        verifyNoInteractions(properties, redissonClient, sessionService, issueLinkService, integrationService);
    }

    @Test
    void aBlankCommentPostsNothing() {
        // The publisher normalizes blank to null, but a foreign producer might not have.
        subscriber.onComponentStateChangeEventReceived(
                event(CcType.ACC, CcState.Draft, CcState.Candidate, "   \n\t "));

        verifyNoInteractions(properties, redissonClient, sessionService, issueLinkService, integrationService);
    }

    @Test
    void everyUnregisteredTransitionIsSkipped() {
        List<ComponentStateChangeEvent> skipped = List.of(
                event(CcType.ACC, CcState.WIP, CcState.Draft),
                event(CcType.ACC, CcState.Draft, CcState.WIP),
                // The one-step demotion does not post (only Draft_Candidate / Candidate_WIP do).
                event(CcType.ACC, CcState.Candidate, CcState.Draft),
                event(CcType.ACC, CcState.Candidate, CcState.ReleaseDraft),
                // A cancelled release draft moves components back to Candidate in bulk.
                event(CcType.ACC, CcState.ReleaseDraft, CcState.Candidate),
                event(CcType.ACC, CcState.ReleaseDraft, CcState.Published),
                event(CcType.ACC, CcState.Deleted, CcState.WIP));

        for (ComponentStateChangeEvent event : skipped) {
            subscriber.onComponentStateChangeEventReceived(event);
        }

        verifyNoInteractions(properties, redissonClient, sessionService, issueLinkService, integrationService);
    }

    @Test
    void ignoresComponentTypesWithoutIssueLinking() {
        subscriber.onComponentStateChangeEventReceived(event(CcType.ASCC, CcState.Draft, CcState.Candidate));
        subscriber.onComponentStateChangeEventReceived(null);

        verifyNoInteractions(properties, redissonClient, sessionService, issueLinkService, integrationService);
    }

    @Test
    void doesNothingWhenTheIntegrationIsNotConfigured() {
        when(properties.isConfigured()).thenReturn(false);

        subscriber.onComponentStateChangeEventReceived(event(CcType.ACC, CcState.Draft, CcState.Candidate));

        verifyNoInteractions(redissonClient, sessionService, issueLinkService, integrationService);
    }

    @Test
    void doesNothingWhenTheComponentHasNoLinkedIssues() {
        ComponentStateChangeEvent event = event(CcType.DT, CcState.Draft, CcState.Candidate);
        when(properties.isConfigured()).thenReturn(true);
        when(sessionService.getScoreUserByUserId(USER_ID)).thenReturn(requester);
        when(issueLinkService.getLinkedIssueRefs(requester, CcType.DT, event.getManifestId()))
                .thenReturn(List.of());

        subscriber.onComponentStateChangeEventReceived(event);

        verifyNoInteractions(integrationService, redissonClient);
    }

    @Test
    void swallowsFailures() {
        ComponentStateChangeEvent event = event(CcType.ACC, CcState.Draft, CcState.Candidate);
        when(properties.isConfigured()).thenReturn(true);
        when(sessionService.getScoreUserByUserId(USER_ID)).thenReturn(requester);
        when(issueLinkService.getLinkedIssueRefs(requester, CcType.ACC, event.getManifestId()))
                .thenThrow(new IllegalStateException("link lookup failed"));

        assertThatCode(() -> subscriber.onComponentStateChangeEventReceived(event))
                .doesNotThrowAnyException();

        verifyNoInteractions(integrationService);
    }

    @Test
    void aFailedPostOnOneIssueStillPostsOnTheOthers() {
        ComponentStateChangeEvent event = event(CcType.ACC, CcState.Draft, CcState.Candidate);
        expectPost(event);
        // postIssueComment is itself best-effort: the unstubbed mock returns null — the failure mode —
        // for the first issue, and the loop must still go on to the second.

        assertThatCode(() -> subscriber.onComponentStateChangeEventReceived(event))
                .doesNotThrowAnyException();

        verify(integrationService).postIssueComment(requester, "OAGi", "oagis", 1097, COMMENT);
        verify(integrationService).postIssueComment(requester, "OAGi", "oagis", 1533, COMMENT);
    }

}
