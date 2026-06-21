package org.oagi.score.gateway.http.api.log_management.model;

import java.math.BigInteger;

/**
 * One LOG row's identity and stored snapshot, used to diff two user-selected revisions
 * (issue #1533). {@code reference} is the component GUID the log chain belongs to.
 */
public record LogSnapshotEntry(
        BigInteger logId,
        String reference,
        int revisionNum,
        int revisionTrackingNum,
        String snapshot) {
}
