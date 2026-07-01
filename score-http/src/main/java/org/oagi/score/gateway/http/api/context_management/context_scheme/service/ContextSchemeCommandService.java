package org.oagi.score.gateway.http.api.context_management.context_scheme.service;

import org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload.CreateContextSchemeRequest;
import org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload.UpdateContextSchemeRequest;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeValueDetailsRecord;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeValueId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.repository.ContextSchemeCommandRepository;
import org.oagi.score.gateway.http.api.context_management.context_scheme.repository.ContextSchemeQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service class for managing context schemes.
 * This service includes methods for creating, updating, and discarding context schemes.
 */
@Service
@Transactional
public class ContextSchemeCommandService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private ContextSchemeCommandRepository command(ScoreUser requester) {
        return repositoryFactory.contextSchemeCommandRepository(requester);
    }

    private ContextSchemeQueryRepository query(ScoreUser requester) {
        return repositoryFactory.contextSchemeQueryRepository(requester);
    }

    /**
     * Creates a new context scheme with the given request details.
     *
     * @param requester The user making the request.
     * @param request   The request containing the context scheme details.
     * @return The ID of the created context scheme.
     */
    public ContextSchemeId create(ScoreUser requester, CreateContextSchemeRequest request) {
        // Server-side uniqueness enforcement (do not rely on the client-side pre-check alone).
        var query = query(requester);
        if (query.hasDuplicate(request.schemeId(), request.schemeAgencyId(), request.schemeVersionId())) {
            throw new IllegalArgumentException(
                    "Another context scheme with the triplet (schemeID, AgencyID, Version) already exists.");
        }
        if (query.hasDuplicateName(request.schemeName(), request.schemeId(),
                request.schemeAgencyId(), request.schemeVersionId())) {
            throw new IllegalArgumentException(
                    "Another context scheme with the same name and triplet (schemeID, AgencyID, Version) already exists.");
        }

        var command = command(requester);

        ContextSchemeId contextSchemeId = command.create(
                request.contextCategoryId(),
                request.codeListId(),
                request.schemeId(),
                request.schemeName(),
                request.schemeAgencyId(),
                request.schemeVersionId(),
                request.description()
        );

        // Create context scheme values
        request.contextSchemeValueList().forEach(valueRequest ->
                command.createValue(contextSchemeId, valueRequest.value(), valueRequest.meaning()));

        return contextSchemeId;
    }

    /**
     * Updates an existing context scheme with the new details provided in the request.
     * This includes updating the main context scheme fields and managing context scheme values.
     *
     * @param requester The user making the request.
     * @param request   The request containing the updated context scheme details.
     */
    public void update(ScoreUser requester, UpdateContextSchemeRequest request) {
        var query = query(requester);

        // Server-side uniqueness enforcement, excluding the scheme being edited (Issue #1744 class).
        if (query.hasDuplicateExcludingCurrent(request.contextSchemeId(),
                request.schemeId(), request.schemeAgencyId(), request.schemeVersionId())) {
            throw new IllegalArgumentException(
                    "Another context scheme with the triplet (schemeID, AgencyID, Version) already exists.");
        }
        if (query.hasDuplicateNameExcludingCurrent(request.contextSchemeId(), request.schemeName(),
                request.schemeId(), request.schemeAgencyId(), request.schemeVersionId())) {
            throw new IllegalArgumentException(
                    "Another context scheme with the same name and triplet (schemeID, AgencyID, Version) already exists.");
        }

        // Fetch existing values, then determine which ones the request removes.
        Map<ContextSchemeValueId, ContextSchemeValueDetailsRecord> existingValues = query
                .getContextSchemeValueList(request.contextSchemeId())
                .stream()
                .collect(Collectors.toMap(ContextSchemeValueDetailsRecord::contextSchemeValueId, Function.identity()));

        Set<ContextSchemeValueId> requestedValueIds = request.contextSchemeValueList().stream()
                .map(v -> v.contextSchemeValueId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<ContextSchemeValueId> removedValueIds = existingValues.keySet().stream()
                .filter(id -> !requestedValueIds.contains(id))
                .collect(Collectors.toSet());

        // Guard: refuse to delete a value that is still referenced by a business context value,
        // which would otherwise raise a raw foreign-key violation and roll back the whole update.
        Set<ContextSchemeValueId> usedRemovedValueIds = query.findUsedContextSchemeValueIds(removedValueIds);
        if (!usedRemovedValueIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot remove context scheme value(s) that are in use by one or more business contexts. " +
                            "Update or remove the referencing business contexts first.");
        }

        var command = command(requester);

        // Update the main context scheme details
        command.update(
                request.contextSchemeId(),
                request.contextCategoryId(),
                request.codeListId(),
                request.schemeId(),
                request.schemeName(),
                request.schemeAgencyId(),
                request.schemeVersionId(),
                request.description()
        );

        // Process updates and additions of context scheme values
        request.contextSchemeValueList().forEach(value -> {
            ContextSchemeValueId valueId = value.contextSchemeValueId();
            if (valueId != null && existingValues.containsKey(valueId)) {
                command.updateValue(valueId, value.value(), value.meaning());
            } else {
                command.createValue(request.contextSchemeId(), value.value(), value.meaning());
            }
        });

        // Remove the values that are no longer present in the request.
        removedValueIds.forEach(command::deleteValue);
    }

    /**
     * Discards a context scheme by its ID.
     *
     * @param requester       The user making the request.
     * @param contextSchemeId The ID of the context scheme to discard.
     * @return True if the discard operation was successful, false otherwise.
     */
    public boolean discard(ScoreUser requester, ContextSchemeId contextSchemeId) {
        if (contextSchemeId == null) {
            throw new IllegalArgumentException("Context scheme ID must not be null.");
        }
        if (query(requester).isContextSchemeUsed(contextSchemeId)) {
            throw new IllegalArgumentException(
                    "Context scheme (ID: " + contextSchemeId + ") is in use by one or more business contexts. " +
                            "Update or remove the referencing business contexts before deletion.");
        }

        return discard(requester, Arrays.asList(contextSchemeId)) == 1;
    }

    /**
     * Discards one or more context schemes by their IDs.
     *
     * @param requester           The user making the request.
     * @param contextSchemeIdList A collection of context scheme IDs to discard.
     * @return The number of context schemes successfully discarded.
     */
    public int discard(ScoreUser requester, Collection<ContextSchemeId> contextSchemeIdList) {
        if (contextSchemeIdList == null || contextSchemeIdList.isEmpty()) {
            throw new IllegalArgumentException("Context scheme ID list must not be null or empty.");
        }

        // Filter out schemes that are in use by any business context.
        var query = query(requester);
        List<ContextSchemeId> deletableSchemes = contextSchemeIdList.stream()
                .filter(id -> !query.isContextSchemeUsed(id))
                .collect(Collectors.toList());

        if (deletableSchemes.isEmpty()) {
            throw new IllegalArgumentException(
                    "All provided context schemes are in use by one or more business contexts. " +
                            "Update or remove the referencing business contexts before deletion.");
        }

        return command(requester).delete(
                List.copyOf(deletableSchemes)
        );
    }
}
