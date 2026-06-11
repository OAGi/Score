package org.oagi.score.gateway.http.api.integration_management.github.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.oagi.score.gateway.http.api.integration_management.github.model.event.GitHubIssueWebhookEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Handles GitHub {@code issues} events <em>asynchronously</em>: it extracts the affected issue and
 * publishes it to Redis, so the webhook request thread returns 200 immediately and a subscriber refreshes
 * the cached metadata off-thread. Cache freshness is best-effort (GitHub does not retry, and the
 * view-time refresh is the reconciliation safety net), so a dropped event is acceptable. The webhook
 * {@code issue} payload is already the full issue representation, so no GitHub call is made here.
 * Issue #1533.
 */
@Component
public class IssuesWebhookEventHandler implements GitHubWebhookEventHandler {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String eventType() {
        return "issues";
    }

    @Override
    public void handle(JsonNode payload) {
        JsonNode issue = payload.path("issue");
        JsonNode repository = payload.path("repository");
        String owner = repository.path("owner").path("login").asText(null);
        String name = repository.path("name").asText(null);
        int number = issue.path("number").asInt(-1);
        if (owner == null || name == null || number <= 0 || !issue.isObject()) {
            return;
        }
        try {
            redisTemplate.convertAndSend(GitHubIssueWebhookEvent.CHANNEL,
                    new GitHubIssueWebhookEvent(owner, name, number, objectMapper.writeValueAsString(issue)));
        } catch (Exception ignored) {
            // best-effort: drop the event if it cannot be published
        }
    }
}
