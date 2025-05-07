package org.oagi.score.gateway.http.api.context_management.context_scheme.repository;

import org.oagi.score.gateway.http.api.code_list_management.model.CodeListId;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeValueId;

import java.util.Collection;

/**
 * Repository interface for managing context schemes and their associated values.
 * This interface provides methods for creating, updating, and deleting context schemes and context scheme values.
 */
public interface ContextSchemeCommandRepository {

    /**
     * Creates a new context scheme with the provided details.
     *
     * @param contextCategoryId The ID of the context category the scheme belongs to.
     * @param codeListId        The ID of the code list associated with the scheme.
     * @param schemeId          The ID of the context scheme.
     * @param schemeName        The meaning of the context scheme.
     * @param schemeAgencyId    The agency ID of the context scheme.
     * @param schemeVersionId   The version ID of the context scheme.
     * @param description       A description of the context scheme.
     * @return The ID of the created context scheme.
     */
    ContextSchemeId create(ContextCategoryId contextCategoryId,
                           CodeListId codeListId,
                           String schemeId,
                           String schemeName,
                           String schemeAgencyId,
                           String schemeVersionId,
                           String description);

    /**
     * Updates an existing context scheme with the provided details.
     *
     * @param contextSchemeId   The ID of the context scheme to update.
     * @param contextCategoryId The ID of the context category to assign to the scheme.
     * @param codeListId        The ID of the code list to associate with the scheme.
     * @param schemeId          The scheme ID.
     * @param schemeName        The meaning of the context scheme.
     * @param schemeAgencyId    The agency ID of the scheme.
     * @param schemeVersionId   The version ID of the scheme.
     * @param description       The description of the context scheme.
     * @return true if the update was successful, false otherwise.
     */
    boolean update(ContextSchemeId contextSchemeId,
                   ContextCategoryId contextCategoryId,
                   CodeListId codeListId,
                   String schemeId,
                   String schemeName,
                   String schemeAgencyId,
                   String schemeVersionId,
                   String description);

    /**
     * Creates a new value for a specific context scheme.
     *
     * @param contextSchemeId The ID of the context scheme to associate the value with.
     * @param value           The value to create.
     * @param meaning         The meaning of the value.
     * @return The ID of the created context scheme value.
     */
    ContextSchemeValueId createValue(ContextSchemeId contextSchemeId,
                                     String value, String meaning);

    /**
     * Updates an existing context scheme value.
     *
     * @param contextSchemeValueId The ID of the context scheme value to update.
     * @param value                The new value to set.
     * @param meaning              The new meaning to set.
     * @return true if the update was successful, false otherwise.
     */
    boolean updateValue(ContextSchemeValueId contextSchemeValueId,
                        String value, String meaning);

    /**
     * Deletes a context scheme value.
     *
     * @param contextSchemeValueId The ID of the context scheme value to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    boolean deleteValue(ContextSchemeValueId contextSchemeValueId);

    /**
     * Deletes a list of context schemes.
     *
     * @param contextSchemeIdList A collection of context scheme IDs to delete.
     * @return The number of context schemes successfully deleted.
     */
    int delete(Collection<ContextSchemeId> contextSchemeIdList);

}
