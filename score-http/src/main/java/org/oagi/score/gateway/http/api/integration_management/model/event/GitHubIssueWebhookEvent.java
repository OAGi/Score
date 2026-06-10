package org.oagi.score.gateway.http.api.integration_management.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.oagi.score.gateway.http.common.model.event.Event;

/**
 * A GitHub {@code issues} webhook event, published to Redis so the cached metadata of the affected issue
 * is refreshed off the request thread (best-effort). Carries the raw {@code issue} object JSON straight
 * from the webhook payload, which is the full issue representation — no GitHub call is needed. Issue #1533.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitHubIssueWebhookEvent implements Event {

    /** Redis pub/sub channel this event is published to and consumed from. */
    public static final String CHANNEL = "githubIssueWebhookEvent";

    private String repoOwner;
    private String repoName;
    private int issueNumber;

    /** The raw GitHub {@code issue} object JSON from the webhook payload. */
    private String issuePayload;
}
