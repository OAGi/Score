package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * What the load of a release package is told by the properties {@code score.release-package-load.*}.
 *
 * @param delta      the number from which the records of the users are numbered (what the generator of a package is given)
 * @param lockWait   how long a statement of the swap waits for a lock that another connection holds, before the swap is given up
 * @param drainWait  how long a load waits, to stop writing, for the writers that are at work (a load that cannot stop writing in this time is
 *                   given up before it changes anything)
 * @param writeWait  how long a writer waits, while writing is stopped, before it is refused
 * @param staleAfter how long a load that goes on may stay as it is before it is taken to have been abandoned
 */
@Component
public record ReleasePackageLoadSettings(
        @Value("${score.release-package-load.delta:100000000}") long delta,
        @Value("${score.release-package-load.lock-wait:PT30S}") Duration lockWait,
        @Value("${score.release-package-load.drain-wait:PT60S}") Duration drainWait,
        @Value("${score.release-package-load.write-wait:PT60S}") Duration writeWait,
        @Value("${score.release-package-load.stale-after:PT2H}") Duration staleAfter) {
}
