package org.oagi.score.gateway.http.api.context_management.context_category.service;

import org.oagi.score.gateway.http.api.context_management.context_category.controller.payload.CreateContextCategoryRequest;
import org.oagi.score.gateway.http.api.context_management.context_category.controller.payload.UpdateContextCategoryRequest;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;
import org.oagi.score.gateway.http.api.context_management.context_category.repository.ContextCategoryCommandRepository;
import org.oagi.score.gateway.http.api.context_management.context_scheme.repository.ContextSchemeQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasLength;

/**
 * Service class for managing context categories.
 * This service includes methods for creating, updating, and discarding context categories.
 */
@Service
@Transactional
public class ContextCategoryCommandService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private ContextCategoryCommandRepository command(ScoreUser requester) {
        return repositoryFactory.contextCategoryCommandRepository(requester);
    }

    private ContextSchemeQueryRepository schemeQuery(ScoreUser requester) {
        return repositoryFactory.contextSchemeQueryRepository(requester);
    }

    /**
     * Creates a new context category with the provided details.
     *
     * @param requester The user making the request.
     * @param request   The request containing the context category details.
     * @return The ID of the created context category.
     * @throws IllegalArgumentException if the context category name is empty.
     */
    public ContextCategoryId create(ScoreUser requester, CreateContextCategoryRequest request) {

        if (!hasLength(request.name())) {
            throw new IllegalArgumentException("Context category name cannot be empty.");
        }

        return command(requester).create(
                request.name(),
                request.description());
    }

    /**
     * Updates an existing context category with the new details provided in the request.
     *
     * @param requester The user making the request.
     * @param request   The request containing the updated context category details.
     * @return true if the update was successful, false otherwise.
     * @throws IllegalArgumentException if the context category ID is null or the name is empty.
     */
    public boolean update(ScoreUser requester, UpdateContextCategoryRequest request) {

        if (request.contextCategoryId() == null) {
            throw new IllegalArgumentException("Context category ID must not be null.");
        }
        if (!hasLength(request.name())) {
            throw new IllegalArgumentException("Context category name cannot be empty.");
        }

        return command(requester).update(
                request.contextCategoryId(),
                request.name(),
                request.description());
    }

    /**
     * Discards a context category by its ID.
     * The category must not be in use by any context schemes.
     *
     * @param requester         The user making the request.
     * @param contextCategoryId The ID of the context category to discard.
     * @return true if the discard operation was successful, false otherwise.
     * @throws IllegalArgumentException if the context category ID is null.
     * @throws IllegalStateException    if the context category is in use by any context schemes.
     */
    public boolean discard(ScoreUser requester, ContextCategoryId contextCategoryId) {

        if (contextCategoryId == null) {
            throw new IllegalArgumentException("Context category ID must not be null.");
        }
        if (isContextCategoryInUse(requester, contextCategoryId)) {
            throw new IllegalStateException(
                    "Context category (ID: " + contextCategoryId + ") is in use. " +
                            "Update or remove related context schemes before deletion.");
        }

        return discard(requester, Arrays.asList(contextCategoryId)) == 1;
    }

    /**
     * Discards one or more context categories by their IDs.
     * The categories must not be in use by any context schemes.
     *
     * @param requester             The user making the request.
     * @param contextCategoryIdList A collection of context category IDs to discard.
     * @return The number of context categories successfully discarded.
     * @throws IllegalArgumentException if the context category ID list is null or empty.
     * @throws IllegalStateException    if all provided context categories are in use.
     */
    public int discard(ScoreUser requester, Collection<ContextCategoryId> contextCategoryIdList) {

        if (contextCategoryIdList == null || contextCategoryIdList.isEmpty()) {
            throw new IllegalArgumentException("Context category ID list must not be null or empty.");
        }

        // Filter out categories that are in use
        List<ContextCategoryId> deletableCategories = contextCategoryIdList.stream()
                .filter(id -> !isContextCategoryInUse(requester, id))
                .collect(Collectors.toList());

        if (deletableCategories.isEmpty()) {
            throw new IllegalStateException(
                    "All provided context categories are in use. " +
                            "Update or remove related context schemes before deletion.");
        }

        return command(requester).delete(
                List.copyOf(deletableCategories) // Use immutable copy
        );
    }

    /**
     * Checks if a context category is currently in use by any context schemes.
     *
     * @param contextCategoryId The ID of the context category to check.
     * @return true if the context category is in use, false otherwise.
     */
    private boolean isContextCategoryInUse(ScoreUser requester, ContextCategoryId contextCategoryId) {
        return !schemeQuery(requester).getContextSchemeSummaryList(contextCategoryId).isEmpty();
    }
}
