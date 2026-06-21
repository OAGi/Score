package org.oagi.score.gateway.http.api.library_management.controller.payload;

import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import java.util.List;

/**
 * Request payload for replacing the release dependencies of a library's working release.
 *
 * @param releaseIds the release IDs that should remain assigned as dependencies.
 */
public record UpdateLibraryReleaseDependenciesRequest(List<ReleaseId> releaseIds) {
}
