package org.oagi.score.gateway.http.api.integration_management.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Outcome of a conditional GitHub issue fetch (ETag / {@code If-None-Match}). Issue #1533.
 * <ul>
 *   <li>{@code MODIFIED} — the issue changed; {@link #issue()} holds the fresh representation.</li>
 *   <li>{@code NOT_MODIFIED} — GitHub returned {@code 304}; the cached copy is still current (cheap, and
 *       does not count against the rate limit for authenticated requests).</li>
 *   <li>{@code UNAVAILABLE} — the viewer is not connected, or the call failed/timed out; keep the cache.</li>
 * </ul>
 */
public record IssueFetchResult(Status status, JsonNode issue) {

    public enum Status {
        MODIFIED, NOT_MODIFIED, UNAVAILABLE
    }

    public static IssueFetchResult modified(JsonNode issue) {
        return new IssueFetchResult(Status.MODIFIED, issue);
    }

    public static IssueFetchResult notModified() {
        return new IssueFetchResult(Status.NOT_MODIFIED, null);
    }

    public static IssueFetchResult unavailable() {
        return new IssueFetchResult(Status.UNAVAILABLE, null);
    }

    public boolean isModified() {
        return status == Status.MODIFIED;
    }
}
