package org.oagi.score.gateway.http.api.release_management.repository;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;

import java.util.Collection;

/**
 * Repository interface for executing commands related to release management.
 * Provides methods for creating, updating, and deleting releases.
 */
public interface ReleaseCommandRepository {

    /**
     * Creates a new release with the specified details.
     *
     * @param libraryId      the library associated with the release.
     * @param namespaceId    the namespace associated with the release.
     * @param releaseNum     the unique release number.
     * @param releaseNote    additional notes about the release.
     * @param releaseLicense the license information of the release.
     * @return the unique identifier of the newly created release.
     */
    ReleaseId create(LibraryId libraryId,
                     NamespaceId namespaceId,
                     String releaseNum,
                     String releaseNote,
                     String releaseLicense);

    /**
     * Updates an existing release with new details.
     *
     * @param releaseId      the unique identifier of the release to update.
     * @param namespaceId    the new namespace associated with the release.
     * @param releaseNum     the updated release number.
     * @param releaseNote    updated notes about the release.
     * @param releaseLicense the updated license information of the release.
     */
    boolean update(ReleaseId releaseId,
                   NamespaceId namespaceId,
                   String releaseNum,
                   String releaseNote,
                   String releaseLicense);

    boolean updateState(ReleaseId releaseId,
                        ReleaseState releaseState);

    /**
     * Deletes an existing release by its unique identifier.
     *
     * @param releaseId the unique identifier of the release to be deleted.
     */
    boolean delete(ReleaseId releaseId);

    int delete(Collection<ReleaseId> releaseIdList);

    void copyDepsFromWorking(ReleaseId releaseId, ReleaseId workingReleaseId);

    void deleteDeps(ReleaseId releaseId);

}
