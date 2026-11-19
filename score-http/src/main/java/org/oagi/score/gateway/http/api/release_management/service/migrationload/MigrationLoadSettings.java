package org.oagi.score.gateway.http.api.release_management.service.migrationload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * What the load of a release package is told by the properties {@code score.migration-load.*}.
 *
 * @param enabled    whether a package can be loaded at all (off unless it is turned on, since a load swaps the schema of the
 *                   application and needs what a deployment has to give it, see the plan)
 * @param delta      the number from which the records of the users are numbered (what the generator of a package is given)
 * @param lockWait   how long a statement of the swap waits for a lock that another connection holds, before the swap is given up
 * @param staleAfter how long a load that goes on may stay as it is before it is taken to have been abandoned
 */
@Component
public record MigrationLoadSettings(
        @Value("${score.migration-load.enabled:false}") boolean enabled,
        @Value("${score.migration-load.delta:100000000}") long delta,
        @Value("${score.migration-load.lock-wait:PT30S}") Duration lockWait,
        @Value("${score.migration-load.stale-after:PT2H}") Duration staleAfter) {
}
