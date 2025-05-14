package org.oagi.score.gateway.http.api.context_management.business_context.repository;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextAssignmentId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextValueId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeValueId;

import java.util.Collection;
import java.util.List;

/**
 * Repository interface for managing business contexts, business context values,
 * and assignments between business contexts and top-level ASBIEPs.
 * This interface includes methods for creating, updating, and deleting business contexts and their values,
 * as well as managing business context assignments.
 */
public interface BusinessContextCommandRepository {

    /**
     * Creates a new business context with the provided name.
     *
     * @param name The name of the new business context.
     * @return The ID of the created business context.
     */
    BusinessContextId create(String name);

    /**
     * Updates an existing business context with the provided name.
     *
     * @param businessContextId The ID of the business context to update.
     * @param name              The new name for the business context.
     * @return true if the update was successful, false otherwise.
     */
    boolean update(BusinessContextId businessContextId, String name);

    /**
     * Deletes one or more business contexts.
     *
     * @param businessContextIds A list of business context IDs to delete.
     * @return The number of business contexts successfully deleted.
     */
    int delete(List<BusinessContextId> businessContextIds);

    /**
     * Creates a new value for a specific business context, associating it with a context scheme value.
     *
     * @param businessContextId    The ID of the business context to associate the value with.
     * @param contextSchemeValueId The ID of the context scheme value to associate with the business context.
     * @return The ID of the created business context value.
     */
    BusinessContextValueId createValue(
            BusinessContextId businessContextId, ContextSchemeValueId contextSchemeValueId);

    /**
     * Updates an existing business context value, associating it with a new context scheme value.
     *
     * @param valueId              The ID of the business context value to update.
     * @param contextSchemeValueId The new context scheme value ID to associate with the business context.
     * @return true if the update was successful, false otherwise.
     */
    boolean updateValue(BusinessContextValueId valueId, ContextSchemeValueId contextSchemeValueId);

    /**
     * Deletes a specific business context value by its ID.
     *
     * @param businessContextValueId The ID of the business context value to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    boolean deleteValue(BusinessContextValueId businessContextValueId);

    /**
     * Creates a new assignment of a business context to a top-level ASBIEP.
     *
     * @param businessContextId The ID of the business context to assign.
     * @param topLevelAsbiepId  The ID of the top-level ASBIEP to assign the business context to.
     * @return The ID of the created business context assignment.
     */
    BusinessContextAssignmentId createAssignment(BusinessContextId businessContextId, TopLevelAsbiepId topLevelAsbiepId);

    /**
     * Deletes a specific assignment of a business context to a top-level ASBIEP.
     *
     * @param businessContextId The ID of the business context to unassign.
     * @param topLevelAsbiepId  The ID of the top-level ASBIEP to unassign the business context from.
     * @return true if the deletion was successful, false otherwise.
     */
    boolean deleteAssignment(BusinessContextId businessContextId, TopLevelAsbiepId topLevelAsbiepId);

    /**
     * Deletes multiple business context assignments from a list of top-level ASBIEPs.
     *
     * @param topLevelAsbiepIdList A collection of top-level ASBIEP IDs to unassign the business context from.
     * @return The number of assignments successfully deleted.
     */
    int deleteAssignmentList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList);

}
