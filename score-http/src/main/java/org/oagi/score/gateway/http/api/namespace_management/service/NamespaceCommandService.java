package org.oagi.score.gateway.http.api.namespace_management.service;

import org.oagi.score.gateway.http.api.namespace_management.controller.payload.CreateNamespaceRequest;
import org.oagi.score.gateway.http.api.namespace_management.controller.payload.UpdateNamespaceRequest;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceDetailsRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.repository.NamespaceCommandRepository;
import org.oagi.score.gateway.http.api.namespace_management.repository.NamespaceQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static org.oagi.score.gateway.http.common.model.ScoreRole.ADMINISTRATOR;
import static org.oagi.score.gateway.http.common.model.ScoreRole.DEVELOPER;

/**
 * Service class for handling namespace creation, updating, and deletion.
 */
@Service
@Transactional
public class NamespaceCommandService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private NamespaceCommandRepository command(ScoreUser requester) {
        return repositoryFactory.namespaceCommandRepository(requester);
    };

    private NamespaceQueryRepository query(ScoreUser requester) {
        return repositoryFactory.namespaceQueryRepository(requester);
    };

    /**
     * Creates a new namespace.
     *
     * @param requester The user making the request.
     * @param request   The request payload containing namespace details.
     * @return The ID of the newly created namespace.
     */
    public NamespaceId create(ScoreUser requester, CreateNamespaceRequest request) {
        if (query(requester).hasDuplicateUri(request.libraryId(), request.uri())) {
            throw new IllegalArgumentException("Namespace URI '" + request.uri() + "' already exists.");
        }
        if (query(requester).hasDuplicatePrefix(request.libraryId(), request.prefix())) {
            throw new IllegalArgumentException("Namespace Prefix '" + request.prefix() + "' already exists.");
        }

        return command(requester).create(
                request.libraryId(),
                request.uri(),
                request.prefix(),
                request.description(),
                requester.hasRole(DEVELOPER));
    }

    /**
     * Updates an existing namespace.
     *
     * @param requester The user making the request.
     * @param request   The request payload containing updated namespace details.
     */
    public void update(ScoreUser requester, UpdateNamespaceRequest request) {
        if (request.namespaceId() == null) {
            throw new IllegalArgumentException("'namespaceId' is required.");
        }
        if (!query(requester).exists(request.namespaceId())) {
            throw new IllegalArgumentException("'" + request.namespaceId() + "' does not exist.");
        }
        if (query(requester).hasDuplicateUriExcludingCurrent(request.namespaceId(), request.uri())) {
            throw new IllegalArgumentException("Namespace URI '" + request.uri() + "' already exists.");
        }
        if (query(requester).hasDuplicatePrefixExcludingCurrent(request.namespaceId(), request.prefix())) {
            throw new IllegalArgumentException("Namespace Prefix '" + request.prefix() + "' already exists.");
        }

        if (!command(requester).update(
                request.namespaceId(),
                request.uri(),
                request.prefix(),
                request.description())) {
            throw new AccessDeniedException("Access is denied");
        }
    }

    /**
     * Discards a namespace if it is not in use.
     *
     * @param requester   The user making the request.
     * @param namespaceId The ID of the namespace to be discarded.
     */
    public void discard(ScoreUser requester, NamespaceId namespaceId) {
        NamespaceDetailsRecord namespaceDetails =
                query(requester).getNamespaceDetails(namespaceId, requester.userId());
        if (namespaceDetails == null) {
            throw new EmptyResultDataAccessException(1);
        }
        if (!namespaceDetails.canEdit()) {
            throw new AccessDeniedException("Access is denied");
        }

        if (repositoryFactory.releaseQueryRepository(requester).hasRecordsByNamespaceId(namespaceId) ||
                repositoryFactory.accQueryRepository(requester).hasRecordsByNamespaceId(namespaceId) ||
                repositoryFactory.asccpQueryRepository(requester).hasRecordsByNamespaceId(namespaceId) ||
                repositoryFactory.bccpQueryRepository(requester).hasRecordsByNamespaceId(namespaceId) ||
                repositoryFactory.dtQueryRepository(requester).hasRecordsByNamespaceId(namespaceId) ||
                repositoryFactory.codeListQueryRepository(requester).hasRecordsByNamespaceId(namespaceId) ||
                repositoryFactory.agencyIdListQueryRepository(requester).hasRecordsByNamespaceId(namespaceId)) {
            throw new IllegalArgumentException("The namespace in use cannot be discarded.");
        }

        command(requester).delete(namespaceId);
    }

    /**
     * Transfers ownership of a namespace from the requester to the target user.
     *
     * @param requester   The user initiating the transfer.
     * @param targetUser  The user who will receive ownership of the namespace.
     * @param namespaceId The ID of the namespace to be transferred.
     * @throws AccessDeniedException    If the requester does not have permission.
     * @throws IllegalArgumentException If the namespace type (standard or non-standard)
     *                                  is incompatible with the target user's role.
     */
    public void transferOwnership(ScoreUser requester, ScoreUser targetUser, NamespaceId namespaceId) {
        // Fetch namespace details and validate requester permissions
        NamespaceDetailsRecord namespaceDetails =
                query(requester).getNamespaceDetails(namespaceId, requester.userId());

        if (!requester.hasRole(ADMINISTRATOR) && !Objects.equals(namespaceDetails.owner().userId(), requester.userId())) {
            throw new AccessDeniedException("Only the namespace owner can transfer ownership.");
        }

        // Ensure namespace transfer is allowed based on type and target user role
        if (namespaceDetails.standard() != targetUser.hasRole(DEVELOPER)) {
            throw new IllegalArgumentException(
                    namespaceDetails.standard()
                            ? "Standard namespaceIds cannot be transferred to End Users."
                            : "Non-standard namespaceIds cannot be transferred to Developers."
            );
        }

        // Perform ownership transfer
        boolean success = command(requester).updateOwnership(
                targetUser.userId(),
                namespaceId);

        if (!success) {
            throw new AccessDeniedException("Ownership transfer failed due to insufficient permissions.");
        }
    }

}
