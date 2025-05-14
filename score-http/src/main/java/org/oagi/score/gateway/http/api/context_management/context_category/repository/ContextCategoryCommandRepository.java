package org.oagi.score.gateway.http.api.context_management.context_category.repository;

import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;

import java.util.Collection;

/**
 * Repository interface for managing context categories.
 * This interface provides methods for creating, updating, and deleting context categories.
 */
public interface ContextCategoryCommandRepository {

    /**
     * Creates a new context category with the provided name and description.
     *
     * @param name        The name of the new context category.
     * @param description A description of the new context category.
     * @return The ID of the created context category.
     */
    ContextCategoryId create(String name,
                             String description);

    /**
     * Updates an existing context category with the provided details.
     *
     * @param contextCategoryId The ID of the context category to update.
     * @param name              The new name for the context category.
     * @param description       The new description for the context category.
     * @return true if the update was successful, false otherwise.
     */
    boolean update(ContextCategoryId contextCategoryId,
                   String name,
                   String description);

    /**
     * Deletes a context category by its ID.
     *
     * @param contextCategoryId The ID of the context category to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    boolean delete(ContextCategoryId contextCategoryId);

    /**
     * Deletes multiple context categories by their IDs.
     *
     * @param contextCategoryIdList A collection of context category IDs to delete.
     * @return The number of context categories successfully deleted.
     */
    int delete(Collection<ContextCategoryId> contextCategoryIdList);

}
