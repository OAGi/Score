package org.oagi.score.gateway.http.api.context_management.business_context.model;

import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeValueId;

/**
 * Represents a business context value, including its ID, associated business context, context category,
 * context scheme, and the value and meaning of the context scheme value.
 */
public record BusinessContextValueRecord(BusinessContextValueId businessContextValueId,
                                         BusinessContextId businessContextId,
                                         ContextCategoryId contextCategoryId,
                                         String contextCategoryName,
                                         ContextSchemeId contextSchemeId,
                                         String contextSchemeName,
                                         ContextSchemeValueId contextSchemeValueId,
                                         String contextSchemeValue,
                                         String contextSchemeValueMeaning) {
}
