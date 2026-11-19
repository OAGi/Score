package org.oagi.score.gateway.http.api.release_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserId;

import java.time.LocalDateTime;

/**
 * A load of a release package: who started it, which package, which schemas it uses, and where it is.
 *
 * @param stagingSchema   where the package is loaded, until the load is over
 * @param backupSchema    where the tables that the swap took out are kept
 * @param discardedSchema where the tables of the load are kept when the swap has been undone
 */
public record MigrationLoadJob(MigrationLoadJobId id, MigrationLoadState state, String message, String packageFileName,
                               String releaseNum, String stagingSchema, String backupSchema, String discardedSchema,
                               String nodeId, UserId ownerUserId, LocalDateTime creationTimestamp,
                               LocalDateTime lastUpdateTimestamp) {
}
