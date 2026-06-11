package org.oagi.score.gateway.http.api.integration_management.github.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Routes an inbound GitHub webhook event to the {@link GitHubWebhookEventHandler}(s) that handle its
 * type; unsupported event types are silently ignored. Handlers are grouped by {@link
 * GitHubWebhookEventHandler#eventType()} into a map at startup, so dispatch is an O(1) lookup that
 * invokes every handler registered for the event type — a single event may have multiple handlers (e.g.
 * one updating the cache and one auditing). Adding support for a new GitHub event is just a new handler
 * bean — no controller change. Issue #1533.
 */
@Component
public class GitHubWebhookEventDispatcher {

    private final Map<String, List<GitHubWebhookEventHandler>> handlersByEventType;

    public GitHubWebhookEventDispatcher(List<GitHubWebhookEventHandler> handlers) {
        this.handlersByEventType = handlers.stream()
                .collect(Collectors.groupingBy(GitHubWebhookEventHandler::eventType));
    }

    /**
     * Dispatches the event to every handler registered for its type. No-op if the event type or payload
     * is null, or if no handler is registered for the event type.
     *
     * @param githubEvent the {@code X-GitHub-Event} header value.
     * @param payload     the parsed webhook request body.
     */
    public void dispatch(String githubEvent, JsonNode payload) {
        if (githubEvent == null || payload == null) {
            return;
        }
        for (GitHubWebhookEventHandler handler : handlersByEventType.getOrDefault(githubEvent, List.of())) {
            handler.handle(payload);
        }
    }
}
