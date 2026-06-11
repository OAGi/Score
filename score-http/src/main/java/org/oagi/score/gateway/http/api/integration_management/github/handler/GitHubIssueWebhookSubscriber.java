package org.oagi.score.gateway.http.api.integration_management.github.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.oagi.score.gateway.http.api.integration_management.github.model.event.GitHubIssueWebhookEvent;
import org.oagi.score.gateway.http.api.integration_management.github.service.GitHubIssueLinkService;
import org.oagi.score.gateway.http.common.model.event.EventListenerContainer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

/**
 * Redis subscriber that processes {@link GitHubIssueWebhookEvent}s off the webhook request thread:
 * it refreshes the affected issue's cached metadata from the payload carried by the event. Best-effort —
 * a failure leaves the cache as-is (the next view-time refresh reconciles it). Issue #1533.
 */
@Component
public class GitHubIssueWebhookSubscriber implements InitializingBean {

    @Autowired
    private EventListenerContainer eventListenerContainer;

    @Autowired
    private GitHubIssueLinkService issueLinkService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterPropertiesSet() {
        eventListenerContainer.addMessageListener(this, "onGitHubIssueWebhookEvent",
                new ChannelTopic(GitHubIssueWebhookEvent.CHANNEL));
    }

    public void onGitHubIssueWebhookEvent(GitHubIssueWebhookEvent event) {
        try {
            JsonNode issue = objectMapper.readTree(event.getIssuePayload());
            issueLinkService.updateIssueCacheFromGitHub(
                    event.getRepoOwner(), event.getRepoName(), event.getIssueNumber(), issue);
        } catch (Exception ignored) {
            // best-effort: leave the cache as-is
        }
    }
}
