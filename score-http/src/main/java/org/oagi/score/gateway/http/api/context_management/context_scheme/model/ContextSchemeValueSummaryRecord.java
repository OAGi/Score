package org.oagi.score.gateway.http.api.context_management.context_scheme.model;

import org.oagi.score.gateway.http.common.model.Guid;

/**
 * Represents a summary of a context scheme value, including its ID, associated context scheme, GUID,
 * value, and meaning.
 */
public record ContextSchemeValueSummaryRecord(ContextSchemeValueId contextSchemeValueId,
                                              ContextSchemeId contextSchemeId,
                                              Guid guid,
                                              String value, String meaning) {
}
