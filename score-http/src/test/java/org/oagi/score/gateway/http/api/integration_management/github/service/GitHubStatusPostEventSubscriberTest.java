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
import org.oagi.score.gateway.http.api.integration_management.github.model.ProjectFieldOptions;
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
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the component-state-change queue consumer behind the GitHub status post and the project board
 * fieldOption sync (issue #1533): the verbatim pass-through of the user-approved comment on any transition,
 * the comment-less opt-out, the fieldOption sync / remove / override (force-past-gate) on the destination
 * state, the fan-out over every linked issue, and the best-effort guarantees (no linked issues / not
 * configured / failures swallowed).
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

    private final ProjectFieldOptions projectFieldOptions = new ProjectFieldOptions(Map.of(
            "WIP", "Implementing", "Draft", "Implemented", "Candidate", "Candidate",
            "ReleaseDraft", "Ready for release"), "New");

    GitHubStatusPostEventSubscriberTest() {
        ReflectionTestUtils.setField(subscriber, "properties", properties);
        ReflectionTestUtils.setField(subscriber, "projectFieldOptions", projectFieldOptions);
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
        return event(ccType, prevState, nextState, comment, null);
    }

    private static ComponentStateChangeEvent event(CcType ccType, CcState prevState, CcState nextState,
                                                   String comment, String projectFieldOptionOverride) {
        return new ComponentStateChangeEvent(ccType, manifestId(ccType), prevState, nextState, USER_ID,
                "event-1", comment, projectFieldOptionOverride);
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
    void cancellingARevisionForADeveloperPostsTheCommentVerbatim() {
        // Cancel reverts a WIP revision back to the previously released revision — Published for a
        // developer (issue #1533 follow-up).
        ComponentStateChangeEvent event = event(CcType.ACC, CcState.WIP, CcState.Published,
                "Revision cancelled; reverted to rev. 1.");
        expectPost(event);

        subscriber.onComponentStateChangeEventReceived(event);

        verify(integrationService).postIssueComment(requester, "OAGi", "oagis", 1097, "Revision cancelled; reverted to rev. 1.");
        verify(integrationService).postIssueComment(requester, "OAGi", "oagis", 1533, "Revision cancelled; reverted to rev. 1.");
    }

    @Test
    void cancellingAnAmendmentForAnEndUserPostsTheCommentVerbatim() {
        // For an end-user the released state is Production, so the cancel reverts WIP -> Production.
        ComponentStateChangeEvent event = event(CcType.AGENCY_ID_LIST, CcState.WIP, CcState.Production,
                "Amendment cancelled.");
        expectPost(event);

        subscriber.onComponentStateChangeEventReceived(event);

        verify(integrationService).postIssueComment(requester, "OAGi", "oagis", 1097, "Amendment cancelled.");
        verify(integrationService).postIssueComment(requester, "OAGi", "oagis", 1533, "Amendment cancelled.");
    }

    @Test
    void aCommentLessEventPostsNothing() {
        // The user cleared the pre-filled comment box: the empty comment is the opt-out. FieldOption sync is
        // off here (isProjectConfigured() defaults to false), so neither action does anything.
        when(properties.isConfigured()).thenReturn(true);

        subscriber.onComponentStateChangeEventReceived(
                event(CcType.ACC, CcState.Draft, CcState.Candidate, null));

        verifyNoInteractions(redissonClient, sessionService, issueLinkService, integrationService);
    }

    @Test
    void aBlankCommentPostsNothing() {
        // The publisher normalizes blank to null, but a foreign producer might not have.
        when(properties.isConfigured()).thenReturn(true);

        subscriber.onComponentStateChangeEventReceived(
                event(CcType.ACC, CcState.Draft, CcState.Candidate, "   \n\t "));

        verifyNoInteractions(redissonClient, sessionService, issueLinkService, integrationService);
    }

    @Test
    void aTransitionWithNoCommentAndNoFieldOptionActionIsSkipped() {
        // Configured, but fieldOption sync off and no comment on the event: there is nothing to do, so nothing
        // reaches GitHub (no comment allow-list any more — absence of a comment is the opt-out).
        when(properties.isConfigured()).thenReturn(true);
        List<ComponentStateChangeEvent> skipped = List.of(
                event(CcType.ACC, CcState.WIP, CcState.Draft, null),
                event(CcType.ACC, CcState.Candidate, CcState.Draft, null),
                event(CcType.ACC, CcState.ReleaseDraft, CcState.Candidate, null),
                event(CcType.ACC, CcState.Deleted, CcState.WIP, null));

        for (ComponentStateChangeEvent event : skipped) {
            subscriber.onComponentStateChangeEventReceived(event);
        }

        verifyNoInteractions(redissonClient, sessionService, issueLinkService, integrationService);
    }

    @Test
    void aCommentPostsOnAnyTransitionNotJustPromotionAndRevert() {
        // The dialog now opens on every state change, so a user comment can ride any transition (here a
        // WIP -> Draft demotion, which was never postable before). The subscriber posts whenever a
        // comment is present, with no transition allow-list.
        ComponentStateChangeEvent event = event(CcType.ACC, CcState.WIP, CcState.Draft, "Heads-up on this demotion.");
        expectPost(event);

        subscriber.onComponentStateChangeEventReceived(event);

        verify(integrationService).postIssueComment(requester, "OAGi", "oagis", 1097, "Heads-up on this demotion.");
        verify(integrationService).postIssueComment(requester, "OAGi", "oagis", 1533, "Heads-up on this demotion.");
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

    // --- Projects v2 board fieldOption sync (issue #1533, Feature 2) ---

    /** FieldOption set-up: configured, fieldOption sync on, two linked issues, a rate limiter. */
    private void expectFieldOptionSync(ComponentStateChangeEvent event) {
        when(properties.isConfigured()).thenReturn(true);
        when(properties.isProjectConfigured()).thenReturn(true);
        when(sessionService.getScoreUserByUserId(USER_ID)).thenReturn(requester);
        when(issueLinkService.getLinkedIssueRefs(requester, event.getCcType(), event.getManifestId()))
                .thenReturn(List.of(link(1097), link(1533)));
        when(redissonClient.getRateLimiter(anyString())).thenReturn(rateLimiter);
    }

    @Test
    void fieldOptionSyncMovesEveryLinkedIssueIntoTheDestinationFieldOption() {
        // Draft -> WIP maps the destination state WIP to the 'Implementing' fieldOption. The event carries no
        // comment, so this isolates the fieldOption sync: the board moves, no comment is posted. No override,
        // so force is false (the gate-fieldOption guard stays in effect).
        ComponentStateChangeEvent event = event(CcType.ACC, CcState.Draft, CcState.WIP, null);
        expectFieldOptionSync(event);

        subscriber.onComponentStateChangeEventReceived(event);

        verify(integrationService).moveIssueToFieldOption(requester, "OAGi", "oagis", 1097, null, "Implementing", false);
        verify(integrationService).moveIssueToFieldOption(requester, "OAGi", "oagis", 1533, null, "Implementing", false);
        verify(integrationService, never()).postIssueComment(any(), anyString(), anyString(), anyInt(), anyString());
    }

    @Test
    void releaseDraftMovesEveryLinkedIssueIntoTheReadyForReleaseFieldOption() {
        // Candidate -> ReleaseDraft maps to the 'Ready for release' fieldOption (issue #1533 follow-up).
        ComponentStateChangeEvent event = event(CcType.ACC, CcState.Candidate, CcState.ReleaseDraft);
        expectFieldOptionSync(event);

        subscriber.onComponentStateChangeEventReceived(event);

        verify(integrationService).moveIssueToFieldOption(requester, "OAGi", "oagis", 1097, null, "Ready for release", false);
        verify(integrationService).moveIssueToFieldOption(requester, "OAGi", "oagis", 1533, null, "Ready for release", false);
    }

    @Test
    void publishingRemovesEveryLinkedIssueFromTheBoard() {
        // ReleaseDraft -> Published is a remove state: the cards come off the board (no fieldOption move).
        ComponentStateChangeEvent event = event(CcType.ACC, CcState.ReleaseDraft, CcState.Published);
        expectFieldOptionSync(event);

        subscriber.onComponentStateChangeEventReceived(event);

        verify(integrationService).removeIssueFromProject(requester, "OAGi", "oagis", 1097, null);
        verify(integrationService).removeIssueFromProject(requester, "OAGi", "oagis", 1533, null);
        verify(integrationService, never()).moveIssueToFieldOption(any(), anyString(), anyString(), anyInt(), any(), anyString(), anyBoolean());
    }

    @Test
    void cancellingARevisionResetsEveryLinkedIssueToTheInitialFieldOption() {
        // WIP -> Published (developer cancel-revision revert) resets the card to 'New' rather than
        // removing it — the revert is not a release. (The comment is also posted; that is covered above.)
        ComponentStateChangeEvent event = event(CcType.ACC, CcState.WIP, CcState.Published);
        expectFieldOptionSync(event);

        subscriber.onComponentStateChangeEventReceived(event);

        verify(integrationService).moveIssueToFieldOption(requester, "OAGi", "oagis", 1097, null, "New", false);
        verify(integrationService).moveIssueToFieldOption(requester, "OAGi", "oagis", 1533, null, "New", false);
        verify(integrationService, never()).removeIssueFromProject(any(), anyString(), anyString(), anyInt(), any());
    }

    @Test
    void anEndUserCancelAlsoResetsToTheInitialFieldOption() {
        // WIP -> Production (end-user cancel-revision revert) is symmetric with the developer case.
        ComponentStateChangeEvent event = event(CcType.AGENCY_ID_LIST, CcState.WIP, CcState.Production);
        expectFieldOptionSync(event);

        subscriber.onComponentStateChangeEventReceived(event);

        verify(integrationService).moveIssueToFieldOption(requester, "OAGi", "oagis", 1097, null, "New", false);
        verify(integrationService).moveIssueToFieldOption(requester, "OAGi", "oagis", 1533, null, "New", false);
        verify(integrationService, never()).removeIssueFromProject(any(), anyString(), anyString(), anyInt(), any());
    }

    @Test
    void candidatePromotionBothMovesTheFieldOptionAndPostsTheComment() {
        // Draft -> Candidate is both a fieldOption transition (destination 'Candidate' fieldOption) and a comment
        // transition: both actions run on the one event.
        ComponentStateChangeEvent event = event(CcType.ACC, CcState.Draft, CcState.Candidate);
        expectFieldOptionSync(event);

        subscriber.onComponentStateChangeEventReceived(event);

        verify(integrationService).moveIssueToFieldOption(requester, "OAGi", "oagis", 1097, null, "Candidate", false);
        verify(integrationService).moveIssueToFieldOption(requester, "OAGi", "oagis", 1533, null, "Candidate", false);
        verify(integrationService).postIssueComment(requester, "OAGi", "oagis", 1097, COMMENT);
        verify(integrationService).postIssueComment(requester, "OAGi", "oagis", 1533, COMMENT);
    }

    @Test
    void fieldOptionSyncIsSkippedWhenTheDestinationStateHasNoBoardAction() {
        // WIP -> QA has neither a fieldOption nor a remove action, and (comment-less here) nothing to post:
        // nothing happens, and the linked issues are never even looked up.
        when(properties.isConfigured()).thenReturn(true);
        when(properties.isProjectConfigured()).thenReturn(true);

        subscriber.onComponentStateChangeEventReceived(event(CcType.ACC, CcState.WIP, CcState.QA, null));

        verifyNoInteractions(integrationService, redissonClient, sessionService, issueLinkService);
    }

    @Test
    void fieldOptionSyncDoesNothingWhenProjectSyncIsNotConfigured() {
        // Configured for comments but fieldOption sync off (isProjectConfigured() defaults to false): a
        // comment-less fieldOption-only transition (Draft -> WIP) does nothing.
        when(properties.isConfigured()).thenReturn(true);

        subscriber.onComponentStateChangeEventReceived(event(CcType.ACC, CcState.Draft, CcState.WIP, null));

        verifyNoInteractions(integrationService, redissonClient, sessionService, issueLinkService);
    }

    // --- Per-transition fieldOption override (issue #1533, Feature 2) ---

    @Test
    void anOverrideMovesEveryLinkedIssueToTheChosenFieldOptionAndForcesPastTheGateGuard() {
        // Draft -> Candidate normally lands in 'Candidate'; the user picked 'Implementing' in the dialog.
        // The override wins and force=true so the move bypasses the maintainer gate-fieldOption guard.
        ComponentStateChangeEvent event = event(CcType.ACC, CcState.Draft, CcState.Candidate, COMMENT, "Implementing");
        expectFieldOptionSync(event);

        subscriber.onComponentStateChangeEventReceived(event);

        verify(integrationService).moveIssueToFieldOption(requester, "OAGi", "oagis", 1097, null, "Implementing", true);
        verify(integrationService).moveIssueToFieldOption(requester, "OAGi", "oagis", 1533, null, "Implementing", true);
        // The configured 'Candidate' fieldOption is NOT used when overridden.
        verify(integrationService, never()).moveIssueToFieldOption(any(), anyString(), anyString(), anyInt(), any(), eq("Candidate"), anyBoolean());
    }

    @Test
    void anOverrideTakesPrecedenceOverARemoveTransition() {
        // ReleaseDraft -> Published normally removes the card; an override moves it instead (no removal).
        ComponentStateChangeEvent event = event(CcType.ACC, CcState.ReleaseDraft, CcState.Published, null, "Implemented");
        expectFieldOptionSync(event);

        subscriber.onComponentStateChangeEventReceived(event);

        verify(integrationService).moveIssueToFieldOption(requester, "OAGi", "oagis", 1097, null, "Implemented", true);
        verify(integrationService).moveIssueToFieldOption(requester, "OAGi", "oagis", 1533, null, "Implemented", true);
        verify(integrationService, never()).removeIssueFromProject(any(), anyString(), anyString(), anyInt(), any());
    }

    @Test
    void anOverrideTakesPrecedenceOverTheRevertToInitialFieldOption() {
        // WIP -> Published (cancel-revision revert) normally resets to 'New'; the override wins.
        ComponentStateChangeEvent event = event(CcType.ACC, CcState.WIP, CcState.Published, null, "Candidate");
        expectFieldOptionSync(event);

        subscriber.onComponentStateChangeEventReceived(event);

        verify(integrationService).moveIssueToFieldOption(requester, "OAGi", "oagis", 1097, null, "Candidate", true);
        verify(integrationService).moveIssueToFieldOption(requester, "OAGi", "oagis", 1533, null, "Candidate", true);
        verify(integrationService, never()).moveIssueToFieldOption(any(), anyString(), anyString(), anyInt(), any(), eq("New"), anyBoolean());
    }

    @Test
    void anOverrideDoesNotChangeThePostedComment() {
        // The override only affects the board fieldOption; the comment is still posted verbatim.
        ComponentStateChangeEvent event = event(CcType.ACC, CcState.Draft, CcState.Candidate, COMMENT, "Implementing");
        expectFieldOptionSync(event);

        subscriber.onComponentStateChangeEventReceived(event);

        verify(integrationService).postIssueComment(requester, "OAGi", "oagis", 1097, COMMENT);
        verify(integrationService).postIssueComment(requester, "OAGi", "oagis", 1533, COMMENT);
    }

    @Test
    void aFailingFieldOptionSyncStillPostsTheComment() {
        // The two actions are isolated: a fieldOption-sync blowup must not stop the comment from being posted.
        ComponentStateChangeEvent event = event(CcType.ACC, CcState.Draft, CcState.Candidate);
        when(properties.isConfigured()).thenReturn(true);
        when(properties.isProjectConfigured()).thenReturn(true);
        when(sessionService.getScoreUserByUserId(USER_ID)).thenReturn(requester);
        when(issueLinkService.getLinkedIssueRefs(requester, CcType.ACC, event.getManifestId()))
                .thenReturn(List.of(link(1097)));
        when(redissonClient.getRateLimiter(anyString())).thenReturn(rateLimiter);
        when(integrationService.moveIssueToFieldOption(any(), anyString(), anyString(), anyInt(), any(), anyString(), anyBoolean()))
                .thenThrow(new RuntimeException("GraphQL is down"));

        assertThatCode(() -> subscriber.onComponentStateChangeEventReceived(event))
                .doesNotThrowAnyException();

        verify(integrationService).postIssueComment(requester, "OAGi", "oagis", 1097, COMMENT);
    }

}
