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
                                   ReleaseSummaryRecord next) implements ReleaseBaseRecord {

    /**
     * Determines if this release is the latest release in the sequence.
     *
     * @return {@code true} if this release is the latest, otherwise {@code false}.
     */
    public boolean isLatestRelease() {
        return (next != null) ? next.isWorkingRelease() : false;
    }
}
