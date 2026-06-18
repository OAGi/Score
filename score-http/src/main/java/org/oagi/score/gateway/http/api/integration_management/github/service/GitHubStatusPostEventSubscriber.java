package org.oagi.score.gateway.http.api.integration_management.github.service;

import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.event.ComponentStateChangeEvent;
import org.oagi.score.gateway.http.api.cc_management.service.ComponentStateChangeEventPublisher;
import org.oagi.score.gateway.http.api.integration_management.github.config.GitHubIntegrationProperties;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueLinkRecord;
import org.oagi.score.gateway.http.api.integration_management.github.model.ProjectFieldOptions;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Reacts to component state changes on the GitHub side for issue #1533. Consumes the
 * {@link ComponentStateChangeEvent} queue — a point-to-point queue, so exactly <em>one</em> backend
 * instance receives each event no matter how many are running — and runs two independent,
 * best-effort actions on each event:
 *
 * <ol>
 *   <li><b>Project board fieldOption sync</b> (Feature 2): moves every linked issue into the board fieldOption
 *   matching the destination state ({@link ProjectFieldOptions}), or the user-chosen override fieldOption. Gated on
 *   the destination state having a board action (or an override) and independent of any comment.</li>
 *   <li><b>Status comment</b> (sub-task 4, and the backend half of sub-task 5): posts the event's
 *   comment on every linked issue whenever a comment is present. The state-change dialog now appears
 *   on every transition (when a component has linked issues), so a comment can ride any transition;
 *   the event's comment — present only when the acting user wrote one — is the opt-in, and a
 *   comment-less event posts nothing.</li>
 * </ol>
 *
 * <p>The two actions are isolated in separate try/catch blocks, so one failing or being disabled
 * never affects the other.
 *
 * <p>The comment body is posted <em>verbatim</em>: the frontend pre-fills the state-change dialog
 * with the rendered change summary, the acting user edits the Markdown freely, and what they
 * confirmed travels on the event — this subscriber composes nothing. An event without a comment
 * (the user cleared the box) posts nothing: the empty comment is the opt-out.</p>
 *
 * <p>Posting is best-effort and fully decoupled from the state transition: the event is queued
 * after the transaction committed, this handler runs on the queue-consumer thread, the comment is
 * made with the acting user's own GitHub token ({@code postIssueComment} returns {@code null}
 * instead of throwing when the user has not connected GitHub or GitHub is unreachable), and any
 * other failure is logged and swallowed — a GitHub problem can never affect the state change
 * itself. Re-promoting after a revert posts again by design: each post documents one transition.
 * Comment creation is paced through a cluster-wide rate limiter because GitHub's secondary rate
 * limit rejects unspaced content writes, e.g. when a bulk promote fans out over many components
 * linked to the same issue.</p>
 */
@Component
public class GitHubStatusPostEventSubscriber implements InitializingBean, DisposableBean {

    /** Component types for which GitHub issue linking is implemented. */
    private static final Set<CcType> SUPPORTED_TYPES = EnumSet.of(
            CcType.ACC, CcType.ASCCP, CcType.BCCP, CcType.DT, CcType.CODE_LIST, CcType.AGENCY_ID_LIST);

    /** Cluster-wide pacing of comment creation: GitHub asks for ≥1s between content writes. */
    private static final String POST_RATE_LIMITER_KEY = "score:integration:github:status-post-rate";

    /**
     * Cluster-wide pacing of project board writes, kept separate from the comment limiter so the two
     * content-write streams (comment + fieldOption move) do not serialize against each other.
     */
    private static final String FIELD_OPTION_SYNC_RATE_LIMITER_KEY = "score:integration:github:fieldOption-sync-rate";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GitHubIntegrationProperties properties;

    @Autowired
    private ProjectFieldOptions projectFieldOptions;

    @Autowired
    private GitHubIntegrationService integrationService;

    @Autowired
    private GitHubIssueLinkService issueLinkService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private RedissonClient redissonClient;

    private RBlockingQueue<ComponentStateChangeEvent> eventQueue;

    private int subscriptionId;

    private ExecutorService executor;

    @Override
    public void afterPropertiesSet() {
        // The subscription callback runs on a Redisson listener thread, which forbids Redisson's
        // own synchronous calls (the rate limiter would throw immediately) and must not be blocked
        // by GitHub HTTP calls — hand each event off to a dedicated worker. A single thread keeps
        // this instance's posts sequential.
        executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "github-status-post");
            thread.setDaemon(true);
            return thread;
        });
        eventQueue = redissonClient.getBlockingQueue(
                ComponentStateChangeEventPublisher.COMPONENT_STATE_CHANGE_EVENT_QUEUE,
                ComponentStateChangeEventPublisher.COMPONENT_STATE_CHANGE_EVENT_CODEC);
        subscriptionId = eventQueue.subscribeOnElements((Consumer<ComponentStateChangeEvent>) event ->
                executor.execute(() -> onComponentStateChangeEventReceived(event)));
    }

    @Override
    public void destroy() {
        if (eventQueue != null) {
            eventQueue.unsubscribe(subscriptionId);
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    /**
     * This method is invoked with each element taken from the 'componentStateChangeEventQueueV2'.
     *
     * @param event the committed component state change
     */
    public void onComponentStateChangeEventReceived(ComponentStateChangeEvent event) {
        if (event == null || !SUPPORTED_TYPES.contains(event.getCcType())) {
            return;
        }
        if (!properties.isConfigured()) {
            return;
        }

        // Two independent best-effort actions on the same event: move the linked issue into the
        // matching project board fieldOption, and post the status comment. Each is isolated in its own
        // try/catch and gates on its own conditions, so one failing (or being disabled) never blocks
        // the other.
        try {
            syncProjectFieldOption(event);
        } catch (Exception e) {
            logger.warn("Failed to sync the GitHub project fieldOption for {} {} ({} -> {}).",
                    event.getCcType(), event.getManifestId(), event.getPrevState(), event.getNextState(), e);
        }
        try {
            postComment(event);
        } catch (Exception e) {
            logger.warn("Failed to post the GitHub status update for {} {} ({} -> {}).",
                    event.getCcType(), event.getManifestId(), event.getPrevState(), event.getNextState(), e);
        }
    }

    /**
     * Drives the board action for the destination state on every linked issue (issue #1533, Feature 2):
     * move the card into the matching fieldOption ({@link ProjectFieldOptions#fieldOptionFor}), or remove it from the board
     * ({@link ProjectFieldOptions#isRemoveState}, e.g. once Published). An explicit per-transition fieldOption
     * override the acting user picked in the state-change dialog ({@code event.getProjectFieldOptionOverride()})
     * takes precedence over all config-derived actions and force-moves the card even past a maintainer
     * gate fieldOption. Gated only on the destination state having an action (or an override being present) —
     * backward dev transitions and the release lifecycle move the board too — and independent of any
     * comment. The board write uses the acting user's own GitHub token (they need the {@code project}
     * scope). No-ops when fieldOption sync is not configured, the state has no board action and there is no
     * override, or the component has no linked issues.
     */
    private void syncProjectFieldOption(ComponentStateChangeEvent event) {
        if (!properties.isProjectConfigured()) {
            return;
        }
        CcState prevState = event.getPrevState();
        CcState nextState = event.getNextState();
        // Already normalized (trimmed, blank-to-null) by the publisher; non-null = a deliberate user choice.
        String override = event.getProjectFieldOptionOverride();
        String desiredFieldOption;
        boolean remove = false;
        if (override != null) {
            // The user's dialog choice wins over every config-derived action (fieldOption / remove / revert).
            desiredFieldOption = override;
        } else if (projectFieldOptions.isRevertToInitial(prevState, nextState)) {
            // A cancelled revision reverts to the previously released state: reset the card to the
            // initial fieldOption rather than removing it (the release path Published is what removes).
            desiredFieldOption = projectFieldOptions.getDefaultFieldOption();
        } else if (projectFieldOptions.isRemoveState(nextState)) {
            desiredFieldOption = null;
            remove = true;
        } else {
            desiredFieldOption = projectFieldOptions.fieldOptionFor(nextState);
        }
        if (desiredFieldOption == null && !remove) {
            return;
        }
        ScoreUser requester = sessionService.getScoreUserByUserId(event.getUserId());
        List<GitHubIssueLinkRecord> links = issueLinkService.getLinkedIssueRefs(
                requester, event.getCcType(), event.getManifestId());
        if (links.isEmpty()) {
            return;
        }

        RRateLimiter rateLimiter = redissonClient.getRateLimiter(FIELD_OPTION_SYNC_RATE_LIMITER_KEY);
        rateLimiter.trySetRate(RateType.OVERALL, 1, 1, RateIntervalUnit.SECONDS);
        for (GitHubIssueLinkRecord link : links) {
            rateLimiter.acquire();
            if (remove) {
                boolean removed = integrationService.removeIssueFromProject(requester,
                        link.repoOwner(), link.repoName(), link.issueNumber(), link.cachedMetadata());
                if (removed) {
                    logger.info("Removed {}/{}#{} from the GitHub project board for {} {} (-> {}).",
                            link.repoOwner(), link.repoName(), link.issueNumber(),
                            event.getCcType(), event.getManifestId(), nextState);
                } else {
                    logger.warn("Could not remove {}/{}#{} from the GitHub project board (not on the " +
                                    "board, the acting user is not connected / lacks the project scope, " +
                                    "or the GitHub call failed).",
                            link.repoOwner(), link.repoName(), link.issueNumber());
                }
            } else {
                String movedFieldOption = integrationService.moveIssueToFieldOption(requester,
                        link.repoOwner(), link.repoName(), link.issueNumber(), link.cachedMetadata(),
                        desiredFieldOption, /* force past gate fieldOption on an explicit override */ override != null);
                if (movedFieldOption != null) {
                    logger.info("Moved {}/{}#{} to the '{}' GitHub project fieldOption for {} {} (-> {}).",
                            link.repoOwner(), link.repoName(), link.issueNumber(), movedFieldOption,
                            event.getCcType(), event.getManifestId(), nextState);
                } else {
                    logger.warn("Could not move {}/{}#{} to the '{}' GitHub project fieldOption (the acting user " +
                                    "is not connected / lacks the project scope, or the GitHub call failed).",
                            link.repoOwner(), link.repoName(), link.issueNumber(), desiredFieldOption);
                }
            }
        }
    }

    private void postComment(ComponentStateChangeEvent event) {
        // The dialog now opens on every state change, so a comment can accompany ANY transition; the
        // presence of a comment is the opt-in (there is no transition allow-list). An event without a
        // comment — the user cleared the box, or a non-dialog state change — posts nothing. The
        // publisher already normalized blank to null, but a foreign producer might not have.
        if (event.getComment() == null || event.getComment().isBlank()) {
            return;
        }
        ScoreUser requester = sessionService.getScoreUserByUserId(event.getUserId());
        List<GitHubIssueLinkRecord> links = issueLinkService.getLinkedIssueRefs(
                requester, event.getCcType(), event.getManifestId());
        if (links.isEmpty()) {
            return;
        }

        String body = event.getComment();

        RRateLimiter rateLimiter = redissonClient.getRateLimiter(POST_RATE_LIMITER_KEY);
        rateLimiter.trySetRate(RateType.OVERALL, 1, 1, RateIntervalUnit.SECONDS);
        for (GitHubIssueLinkRecord link : links) {
            rateLimiter.acquire();
            String commentUrl = integrationService.postIssueComment(
                    requester, link.repoOwner(), link.repoName(), link.issueNumber(), body);
            if (commentUrl != null) {
                logger.info("Posted the {} status of {} {} on {}/{}#{}: {}",
                        event.getNextState(), event.getCcType(), event.getManifestId(),
                        link.repoOwner(), link.repoName(), link.issueNumber(), commentUrl);
            } else {
                logger.warn("Could not post the {} status of {} {} on {}/{}#{} " +
                                "(the acting user is not connected to GitHub, or the GitHub call failed).",
                        event.getNextState(), event.getCcType(), event.getManifestId(),
                        link.repoOwner(), link.repoName(), link.issueNumber());
            }
        }
    }

}
