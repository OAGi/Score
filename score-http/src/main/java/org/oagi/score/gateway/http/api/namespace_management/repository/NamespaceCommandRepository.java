package org.oagi.score.gateway.http.api.namespace_management.repository;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

/**
 * Repository interface for managing namespaceIds, including creation, updating, deletion,
 * and transferring ownership.
 */
public interface NamespaceCommandRepository {

    /**
     * Creates a new namespace.
     *
     * @param libraryId   The ID of the associated library.
     * @param uri         The URI of the namespace.
     * @param prefix      The prefix of the namespace.
     * @param description A description of the namespace.
     * @param standard    Indicates whether the namespace is a standard namespace.
     * @return The ID of the newly created namespace.
     */
    NamespaceId create(LibraryId libraryId,
                       String uri,
                       String prefix,
                       String description,
                       boolean standard);

    /**
     * Updates an existing namespace.
     *
     * @param namespaceId The ID of the namespace to be updated.
     * @param uri         The new URI of the namespace.
     * @param prefix      The new prefix of the namespace.
     * @param description The new description of the namespace.
     * @return {@code true} if the update was successful, {@code false} otherwise.
     */
    boolean update(NamespaceId namespaceId,
                   String uri,
                   String prefix,
                   String description);

    /**
     * Deletes a namespace.
     *
     * @param namespaceId The ID of the namespace to be deleted.
     * @return
     */
    boolean delete(NamespaceId namespaceId);

    /**
     * Transfers ownership of a namespace to a new owner.
     *
     * @param newOwnerUserId The ID of the new owner.
     * @param namespaceId    The ID of the namespace to be transferred.
     * @return {@code true} if the transfer was successful, {@code false} otherwise.
     */
    boolean updateOwnership(UserId newOwnerUserId,
                            NamespaceId namespaceId);
}
