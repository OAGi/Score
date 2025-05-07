package org.oagi.score.gateway.http.api.release_management.model;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

/**
 * Record representing a single entry in a paginated list of releases.
 * Provides key release details along with audit metadata.
 */
public record ReleaseListEntryRecord(ReleaseId releaseId, LibraryId libraryId, Guid guid,
                                     String releaseNum,
                                     ReleaseState state,
                                     NamespaceId namespaceId,
                                     WhoAndWhen created,
                                     WhoAndWhen lastUpdated) implements ReleaseBaseRecord {
}
