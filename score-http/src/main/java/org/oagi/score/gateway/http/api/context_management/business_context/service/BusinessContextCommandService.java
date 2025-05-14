package org.oagi.score.gateway.http.api.context_management.business_context.service;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.context_management.business_context.controller.payload.CreateBusinessContextRequest;
import org.oagi.score.gateway.http.api.context_management.business_context.controller.payload.UpdateBusinessContextRequest;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextValueId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextValueRecord;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.BusinessContextCommandRepository;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.BusinessContextQueryRepository;
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
 * Service class for managing business contexts.
 * This service includes methods for creating, updating, and discarding business contexts.
 */
@Service
@Transactional
public class BusinessContextCommandService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private BusinessContextCommandRepository command(ScoreUser requester) {
        return repositoryFactory.businessContextCommandRepository(requester);
    }

    private BusinessContextQueryRepository query(ScoreUser requester) {
        return repositoryFactory.businessContextQueryRepository(requester);
    }

    /**
     * Creates a new business context with the provided details and associated values.
     *
     * @param requester The user making the request.
     * @param request   The request containing the business context details.
     * @return The ID of the created business context.
     */
    public BusinessContextId create(ScoreUser requester, CreateBusinessContextRequest request) {

        var command = command(requester);

        BusinessContextId businessContextId = command.create(request.name());

        // Create business context values
        request.businessContextValueList().forEach(valueRequest -> {
            command.createValue(businessContextId, valueRequest.contextSchemeValueId());
        });
        return businessContextId;
    }

    /**
     * Updates an existing business context with the new details provided in the request.
     * This includes updating the business context name and managing the context scheme values.
     *
     * @param requester The user making the request.
     * @param request   The request containing the updated business context details.
     */
    public void update(ScoreUser requester, UpdateBusinessContextRequest request) {

        var command = command(requester);

        // Update the main business context details
        command.update(
                request.businessContextId(),
                request.name()
        );

        // Fetch existing values as a map
        Map<BusinessContextValueId, BusinessContextValueRecord> existingValues = query(requester)
                .getBusinessContextValueList(request.businessContextId())
                .stream()
                .collect(Collectors.toMap(BusinessContextValueRecord::businessContextValueId, Function.identity()));

        // Process updates and additions of business context values
        request.businessContextValueList().forEach(value -> {
            BusinessContextValueId valueId = value.businessContextValueId();
            if (existingValues.containsKey(valueId)) {
                command.updateValue(valueId, value.contextSchemeValueId());
                existingValues.remove(valueId);
            } else {
                command.createValue(request.businessContextId(), value.contextSchemeValueId());
            }
        });

        // Remove any remaining (deleted) values
        existingValues.keySet().forEach(command::deleteValue);
    }

    /**
     * Discards a business context by its ID.
     * The business context ID must not be null.
     *
     * @param requester         The user making the request.
     * @param businessContextId The ID of the business context to discard.
     * @return true if the discard operation was successful, false otherwise.
     * @throws IllegalArgumentException if the business context ID is null.
     */
    public boolean discard(ScoreUser requester, BusinessContextId businessContextId) {

        if (businessContextId == null) {
            throw new IllegalArgumentException("Business context ID must not be null.");
        }

        return discard(requester, Arrays.asList(businessContextId)) == 1;
    }

    /**
     * Discards one or more business contexts by their IDs.
     * The business context ID list must not be null or empty.
     *
     * @param requester             The user making the request.
     * @param businessContextIdList A collection of business context IDs to discard.
     * @return The number of business contexts successfully discarded.
     * @throws IllegalArgumentException if the business context ID list is null or empty.
     */
    public int discard(ScoreUser requester, Collection<BusinessContextId> businessContextIdList) {

        if (businessContextIdList == null || businessContextIdList.isEmpty()) {
            throw new IllegalArgumentException("Business context ID list must not be null or empty.");
        }

        return command(requester).delete(
                List.copyOf(businessContextIdList)
        );
    }

    /**
     * Assigns a business context to a top-level ASBIEP.
     *
     * @param requester         The user making the request.
     * @param businessContextId The ID of the business context to assign.
     * @param topLevelAsbiepId  The ID of the top-level ASBIEP to assign the business context to.
     */
    public void assignBusinessContext(
            ScoreUser requester, BusinessContextId businessContextId, TopLevelAsbiepId topLevelAsbiepId) {

        command(requester).createAssignment(businessContextId, topLevelAsbiepId);
    }

    /**
     * Unassigns a business context from a top-level ASBIEP.
     *
     * @param requester         The user making the request.
     * @param businessContextId The ID of the business context to unassign.
     * @param topLevelAsbiepId  The ID of the top-level ASBIEP to unassign the business context from.
     */
    public void unassignBusinessContext(
            ScoreUser requester, BusinessContextId businessContextId, TopLevelAsbiepId topLevelAsbiepId) {

        command(requester).deleteAssignment(businessContextId, topLevelAsbiepId);
    }

    /**
     * Deletes the assignment of business contexts from a list of top-level ASBIEP IDs.
     *
     * @param requester            The user making the request.
     * @param topLevelAsbiepIdList A collection of top-level ASBIEP IDs to remove assignments for.
     * @return The number of assignments deleted.
     */
    public int discardAssignment(ScoreUser requester, Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {
        return command(requester).deleteAssignmentList(topLevelAsbiepIdList);
    }

}
