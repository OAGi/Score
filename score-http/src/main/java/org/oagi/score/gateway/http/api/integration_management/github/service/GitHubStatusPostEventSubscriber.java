package org.oagi.score.gateway.http.api.integration_management.github.service;

import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.event.ComponentStateChangeEvent;
import org.oagi.score.gateway.http.api.cc_management.service.ComponentStateChangeEventPublisher;
import org.oagi.score.gateway.http.api.integration_management.github.config.GitHubIntegrationProperties;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueLinkRecord;
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
 * Posts the GitHub status comments for issue #1533 (sub-task 4, and the backend notification half
 * of sub-task 5). Consumes the {@link ComponentStateChangeEvent} queue — a point-to-point queue,
 * so exactly <em>one</em> backend instance receives each event no matter how many are running —
 * and dispatches on the {@code {fromState}_{toState}} transition: a transition in
 * {@link #POSTABLE_TRANSITIONS} posts the event's comment on every GitHub issue linked to the
 * component, every other transition is skipped. Currently {@code Draft_Candidate} (the promotion)
 * and {@code Candidate_WIP} (the revert) post; adding a transition is one set entry.
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

    /**
     * The {@code {fromState}_{toState}} transitions that post the event's comment; any transition
     * of the {@link CcState} machine is dispatchable here, and those without an entry are skipped.
     */
    private static final Set<String> POSTABLE_TRANSITIONS = Set.of(
            transitionKey(CcState.Draft, CcState.Candidate),
            transitionKey(CcState.Candidate, CcState.WIP));

    /** Cluster-wide pacing of comment creation: GitHub asks for ≥1s between content writes. */
    private static final String POST_RATE_LIMITER_KEY = "score:integration:github:status-post-rate";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GitHubIntegrationProperties properties;

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
        if (!POSTABLE_TRANSITIONS.contains(transitionKey(event.getPrevState(), event.getNextState()))) {
            return;
        }
        // The user cleared the comment box: post nothing (opt-out). The publisher already
        // normalized blank to null, but a foreign producer might not have.
        if (event.getComment() == null || event.getComment().isBlank()) {
            return;
        }
        if (!properties.isConfigured()) {
            return;
        }

        try {
            post(event);
        } catch (Exception e) {
            logger.warn("Failed to post the GitHub status update for {} {} ({} -> {}).",
                    event.getCcType(), event.getManifestId(), event.getPrevState(), event.getNextState(), e);
        }
    }

    private static String transitionKey(CcState fromState, CcState toState) {
        return fromState + "_" + toState;
    }

    private void post(ComponentStateChangeEvent event) {
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
