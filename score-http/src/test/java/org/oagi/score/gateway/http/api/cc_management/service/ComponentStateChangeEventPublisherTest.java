package org.oagi.score.gateway.http.api.cc_management.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.event.ComponentStateChangeEvent;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the publishing rules of {@link ComponentStateChangeEventPublisher} (issue #1533):
 * implicit release moves are filtered out, the message is deferred to after-commit when a
 * transaction is active, every event gets a unique id, a Redis failure never propagates, and the
 * user-approved GitHub status comment is normalized here — and only here — before the event goes
 * out (trimmed, blank-to-null, truncated to {@code MAX_COMMENT_LENGTH}).
 */
@ExtendWith(MockitoExtension.class)
class ComponentStateChangeEventPublisherTest {

    private static final UserId USER_ID = new UserId(BigInteger.ONE);

    private final ComponentStateChangeEventPublisher publisher = new ComponentStateChangeEventPublisher();

    private final RedissonClient redissonClient = mock(RedissonClient.class);

    private final RBlockingQueue queue = mock(RBlockingQueue.class);

    ComponentStateChangeEventPublisherTest() {
        ReflectionTestUtils.setField(publisher, "redissonClient", redissonClient);
    }

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private void expectQueue() {
        when(redissonClient.getBlockingQueue(
                ComponentStateChangeEventPublisher.COMPONENT_STATE_CHANGE_EVENT_QUEUE,
                ComponentStateChangeEventPublisher.COMPONENT_STATE_CHANGE_EVENT_CODEC)).thenReturn(queue);
    }

    private ComponentStateChangeEvent publishedEvent() {
        ArgumentCaptor<ComponentStateChangeEvent> captor = ArgumentCaptor.forClass(ComponentStateChangeEvent.class);
        verify(queue).offer(captor.capture());
        return captor.getValue();
    }

    @Test
    void publishesImmediatelyWithoutAnActiveTransaction() {
        expectQueue();
        AccManifestId manifestId = new AccManifestId(BigInteger.TEN);

        publisher.publish(CcType.ACC, manifestId, CcState.Draft, CcState.Candidate, USER_ID);

        ComponentStateChangeEvent event = publishedEvent();
        assertThat(event.getCcType()).isEqualTo(CcType.ACC);
        assertThat(event.getManifestId()).isEqualTo(manifestId);
        assertThat(event.getPrevState()).isEqualTo(CcState.Draft);
        assertThat(event.getNextState()).isEqualTo(CcState.Candidate);
        assertThat(event.getUserId()).isEqualTo(USER_ID);
        assertThat(event.getEventId()).isNotBlank();
        // The comment-less overload publishes without a comment.
        assertThat(event.getComment()).isNull();
    }

    @Test
    void everyEventGetsItsOwnId() {
        expectQueue();
        AccManifestId manifestId = new AccManifestId(BigInteger.TEN);

        publisher.publish(CcType.ACC, manifestId, CcState.Draft, CcState.Candidate, USER_ID);
        publisher.publish(CcType.ACC, manifestId, CcState.Draft, CcState.Candidate, USER_ID);

        ArgumentCaptor<ComponentStateChangeEvent> captor = ArgumentCaptor.forClass(ComponentStateChangeEvent.class);
        verify(queue, times(2)).offer(captor.capture());
        assertThat(captor.getAllValues().get(0).getEventId())
                .isNotEqualTo(captor.getAllValues().get(1).getEventId());
    }

    @Test
    void skipsTheImplicitReleaseMovesByDefault() {
        AccManifestId manifestId = new AccManifestId(BigInteger.TEN);

        publisher.publish(CcType.ACC, manifestId, CcState.Candidate, CcState.ReleaseDraft, USER_ID);
        publisher.publish(CcType.ACC, manifestId, CcState.ReleaseDraft, CcState.Published, USER_ID);
        publisher.publish(CcType.ACC, manifestId, CcState.ReleaseDraft, CcState.Candidate, USER_ID);

        verifyNoInteractions(redissonClient);
    }

    /** Sets every input of the publisher's readiness mirror so isProjectFieldOptionSyncReady() is true. */
    private void enableProjectFieldOptionSync() {
        ReflectionTestUtils.setField(publisher, "integrationEnabled", true);
        ReflectionTestUtils.setField(publisher, "clientId", "id");
        ReflectionTestUtils.setField(publisher, "clientSecret", "secret");
        ReflectionTestUtils.setField(publisher, "projectEnabled", true);
        ReflectionTestUtils.setField(publisher, "projectUrl", "https://github.com/orgs/OAGi/projects/8");
    }

    @Test
    void publishesTheImplicitReleaseMovesWhenProjectFieldOptionSyncIsReady() {
        // With the GitHub project fieldOption sync fully configured, the release-lifecycle implicit moves are
        // published too (so the board can react to ReleaseDraft / Published); the comment path still
        // ignores them.
        enableProjectFieldOptionSync();
        expectQueue();
        AccManifestId manifestId = new AccManifestId(BigInteger.TEN);

        publisher.publish(CcType.ACC, manifestId, CcState.Candidate, CcState.ReleaseDraft, USER_ID);
        publisher.publish(CcType.ACC, manifestId, CcState.ReleaseDraft, CcState.Published, USER_ID);

        verify(queue, times(2)).offer(any(ComponentStateChangeEvent.class));
    }

    @Test
    void aPartialProjectConfigStillSuppressesImplicitReleaseMoves() {
        // project-enabled is on but the rest is not fully configured (here a blank project-url; likewise
        // missing credentials or integration disabled). The consumer would drop every event
        // (isProjectConfigured() is false), so the publisher must keep suppressing the bulk
        // release-lifecycle moves rather than re-flood the queue with events no one acts on.
        ReflectionTestUtils.setField(publisher, "integrationEnabled", true);
        ReflectionTestUtils.setField(publisher, "clientId", "id");
        ReflectionTestUtils.setField(publisher, "clientSecret", "secret");
        ReflectionTestUtils.setField(publisher, "projectEnabled", true);
        ReflectionTestUtils.setField(publisher, "projectUrl", "");   // does not parse -> not ready

        AccManifestId manifestId = new AccManifestId(BigInteger.TEN);
        publisher.publish(CcType.ACC, manifestId, CcState.Candidate, CcState.ReleaseDraft, USER_ID);
        publisher.publish(CcType.ACC, manifestId, CcState.ReleaseDraft, CcState.Published, USER_ID);

        verifyNoInteractions(redissonClient);
    }

    @Test
    void anImplicitMoveWithACommentIsPublishedEvenWhenFieldOptionSyncIsNotReady() {
        // Comment and fieldOption sync are independent: an implicit (release-lifecycle) move is normally
        // suppressed when fieldOption sync is off, but a comment-bearing one must still go out so the comment
        // is posted. (Implicit moves are comment-less in practice, but the publisher must not drop one.)
        expectQueue();
        AccManifestId manifestId = new AccManifestId(BigInteger.TEN);

        publisher.publish(CcType.ACC, manifestId, CcState.ReleaseDraft, CcState.Published, USER_ID,
                "Released — closing this out.");

        assertThat(publishedEvent().getComment()).isEqualTo("Released — closing this out.");
    }

    @Test
    void defersToAfterCommitWhenATransactionIsActive() {
        expectQueue();
        TransactionSynchronizationManager.initSynchronization();
        publisher.publish(CcType.DT, new DtManifestId(BigInteger.TWO), CcState.Candidate, CcState.WIP, USER_ID);

        verifyNoInteractions(redissonClient);

        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }
        verify(queue).offer(any(ComponentStateChangeEvent.class));
    }

    @Test
    void aRedisFailureNeverPropagatesToTheCommittedStateChange() {
        when(redissonClient.getBlockingQueue(anyString(), any(Codec.class)))
                .thenThrow(new IllegalStateException("redis down"));

        assertThatCode(() -> publisher.publish(
                CcType.BCCP, new AccManifestId(BigInteger.ONE), CcState.Draft, CcState.Candidate, USER_ID))
                .doesNotThrowAnyException();
    }

    // ----- Comment normalization (issue #1533, sub-task 5) -----

    @Test
    void aCommentPassesThroughToTheEvent() {
        expectQueue();

        publisher.publish(CcType.ACC, new AccManifestId(BigInteger.TEN),
                CcState.Draft, CcState.Candidate, USER_ID, "Resolves the **mismatch** reported here.");

        assertThat(publishedEvent().getComment()).isEqualTo("Resolves the **mismatch** reported here.");
    }

    @Test
    void aCommentIsTrimmedButItsInnerStructureIsKept() {
        expectQueue();

        publisher.publish(CcType.ACC, new AccManifestId(BigInteger.TEN),
                CcState.Draft, CcState.Candidate, USER_ID, "  line one\n\nline two \n");

        assertThat(publishedEvent().getComment()).isEqualTo("line one\n\nline two");
    }

    @Test
    void aBlankCommentBecomesNull() {
        expectQueue();

        publisher.publish(CcType.ACC, new AccManifestId(BigInteger.TEN),
                CcState.Draft, CcState.Candidate, USER_ID, "   \n\t  ");

        assertThat(publishedEvent().getComment()).isNull();
    }

    @Test
    void anOverlongCommentIsTruncatedToTheMaximumLength() {
        expectQueue();
        String overlong = "x".repeat(ComponentStateChangeEventPublisher.MAX_COMMENT_LENGTH + 500);

        publisher.publish(CcType.ACC, new AccManifestId(BigInteger.TEN),
                CcState.Draft, CcState.Candidate, USER_ID, overlong);

        assertThat(publishedEvent().getComment())
                .hasSize(ComponentStateChangeEventPublisher.MAX_COMMENT_LENGTH)
                .isEqualTo("x".repeat(ComponentStateChangeEventPublisher.MAX_COMMENT_LENGTH));
    }

    @Test
    void theMaximumLengthStaysUnderGitHubsCommentLimit() {
        // The comment is the whole rendered GitHub post; GitHub rejects bodies over 65,536 chars.
        assertThat(ComponentStateChangeEventPublisher.MAX_COMMENT_LENGTH)
                .isEqualTo(60_000)
                .isLessThan(65_536);
    }

    @Test
    void truncationNeverCutsThroughASurrogatePair() {
        expectQueue();
        // The emoji's high surrogate sits exactly at the truncation boundary — the cut must move
        // back one char so the comment stays valid UTF-16 (an unpaired surrogate can't reach GitHub).
        String comment = "x".repeat(ComponentStateChangeEventPublisher.MAX_COMMENT_LENGTH - 1) + "🎉" + "tail";

        publisher.publish(CcType.ACC, new AccManifestId(BigInteger.TEN),
                CcState.Draft, CcState.Candidate, USER_ID, comment);

        assertThat(publishedEvent().getComment())
                .hasSize(ComponentStateChangeEventPublisher.MAX_COMMENT_LENGTH - 1)
                .isEqualTo("x".repeat(ComponentStateChangeEventPublisher.MAX_COMMENT_LENGTH - 1));
    }

    @Test
    void aNullCommentStaysNull() {
        expectQueue();

        publisher.publish(CcType.ACC, new AccManifestId(BigInteger.TEN),
                CcState.Draft, CcState.Candidate, USER_ID, null);

        assertThat(publishedEvent().getComment()).isNull();
    }

    // ----- Project fieldOption override normalization (issue #1533, Feature 2) -----

    @Test
    void aProjectFieldOptionOverridePassesThroughToTheEvent() {
        expectQueue();

        publisher.publish(CcType.ACC, new AccManifestId(BigInteger.TEN),
                CcState.Draft, CcState.Candidate, USER_ID, null, "Implementing");

        ComponentStateChangeEvent event = publishedEvent();
        assertThat(event.getProjectFieldOptionOverride()).isEqualTo("Implementing");
        assertThat(event.getComment()).isNull();
    }

    @Test
    void aProjectFieldOptionOverrideIsTrimmedAndBlankBecomesNull() {
        expectQueue();

        publisher.publish(CcType.ACC, new AccManifestId(BigInteger.TEN),
                CcState.Draft, CcState.Candidate, USER_ID, null, "  Ready for release  ");
        assertThat(publishedEvent().getProjectFieldOptionOverride()).isEqualTo("Ready for release");
    }

    @Test
    void aBlankProjectFieldOptionOverrideBecomesNull() {
        expectQueue();

        publisher.publish(CcType.ACC, new AccManifestId(BigInteger.TEN),
                CcState.Draft, CcState.Candidate, USER_ID, null, "   ");
        assertThat(publishedEvent().getProjectFieldOptionOverride()).isNull();
    }

    @Test
    void anOverlongProjectFieldOptionOverrideIsTruncated() {
        expectQueue();
        String overlong = "y".repeat(ComponentStateChangeEventPublisher.MAX_PROJECT_FIELD_OPTION_OVERRIDE_LENGTH + 50);

        publisher.publish(CcType.ACC, new AccManifestId(BigInteger.TEN),
                CcState.Draft, CcState.Candidate, USER_ID, null, overlong);

        assertThat(publishedEvent().getProjectFieldOptionOverride())
                .hasSize(ComponentStateChangeEventPublisher.MAX_PROJECT_FIELD_OPTION_OVERRIDE_LENGTH);
    }

    @Test
    void theCommentOnlyOverloadLeavesTheOverrideNull() {
        expectQueue();

        publisher.publish(CcType.ACC, new AccManifestId(BigInteger.TEN),
                CcState.Draft, CcState.Candidate, USER_ID, "a comment");

        assertThat(publishedEvent().getProjectFieldOptionOverride()).isNull();
    }

}
