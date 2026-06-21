package org.oagi.score.gateway.http.api.integration_management.github.handler;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Handles one (or more) GitHub webhook event type(s) (issue #1533). Each implementation decides whether
 * to process its event <em>synchronously</em> (inline, when the result must be ready before the 200 is
 * returned) or <em>asynchronously</em> (e.g. by publishing to Redis pub/sub and returning immediately).
 * The webhook endpoint stays a thin, fast acknowledger and delegates routing to
 * {@link GitHubWebhookEventDispatcher}.
 */
public interface GitHubWebhookEventHandler {

    /**
     * The {@code X-GitHub-Event} type this handler processes (e.g. {@code "issues"}). The dispatcher keys
     * its handler map by this value, so each event type must be claimed by at most one handler.
     */
    String eventType();

    /**
     * Processes the webhook payload (the parsed JSON root). Implementations must be fast and must not
     * throw — the webhook endpoint always acknowledges with 200 since GitHub does not retry failures.
     *
     * @param payload the parsed webhook request body.
     */
    void handle(JsonNode payload);
}
