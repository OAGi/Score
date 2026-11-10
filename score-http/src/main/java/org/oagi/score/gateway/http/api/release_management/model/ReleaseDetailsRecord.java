package org.oagi.score.gateway.http.api.release_management.model;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

/**
 * Record representing detailed information about a release.
 * Includes metadata such as release notes, license, associated library,
 * namespace, and audit details on creation and last update.
 */
public record ReleaseDetailsRecord(ReleaseId releaseId, LibraryId libraryId, Guid guid,
                                   String releaseNum,
                                   ReleaseState state,
                                   String releaseNote,
                                   String releaseLicense,
                                   NamespaceId namespaceId,
                                   WhoAndWhen created,
                                   WhoAndWhen lastUpdated,
                                   ReleaseSummaryRecord prev,
                                   ReleaseSummaryRecord next,
                                   boolean busy) implements ReleaseBaseRecord {

    /**
     * The same release, told to be busy or not. A release is busy while a transaction holds it, which is while the work for it is going
     * on (as opposed to a release that is in the middle of its work with nothing doing it, because the server stopped). It is not read
     * with the rest of the release: it is only asked for a release that is in the middle of its work.
     */
    public ReleaseDetailsRecord withBusy(boolean busy) {
        return new ReleaseDetailsRecord(releaseId, libraryId, guid, releaseNum, state, releaseNote, releaseLicense, namespaceId,
                created, lastUpdated, prev, next, busy);
    }

    /**
     * Determines if this release is the latest release in the sequence.
     *
     * @return {@code true} if this release is the latest, otherwise {@code false}.
     */
    public boolean isLatestRelease() {
        return (next != null) ? next.isWorkingRelease() : false;
    }
}
