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

        // Fetch existing values as a map
        Map<ContextSchemeValueId, ContextSchemeValueDetailsRecord> existingValues = query(requester)
                .getContextSchemeValueList(request.contextSchemeId())
                .stream()
                .collect(Collectors.toMap(ContextSchemeValueDetailsRecord::contextSchemeValueId, Function.identity()));

        // Process updates and additions of context scheme values
        request.contextSchemeValueList().forEach(value -> {
            ContextSchemeValueId valueId = value.contextSchemeValueId();
            if (existingValues.containsKey(valueId)) {
                command.updateValue(valueId, value.value(), value.meaning());
                existingValues.remove(valueId);
            } else {
                command.createValue(request.contextSchemeId(), value.value(), value.meaning());
            }
        });

        // Remove any remaining (deleted) values
        existingValues.keySet().forEach(command::deleteValue);
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

        return command(requester).delete(
                List.copyOf(contextSchemeIdList)
        );
    }
}
