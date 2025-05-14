package org.oagi.score.gateway.http.api.context_management.context_scheme.model;

import org.oagi.score.gateway.http.common.model.Guid;

/**
 * Represents the details of a context scheme value, including its ID, associated context scheme, GUID,
 * value, meaning, and usage status.
 */
public record ContextSchemeValueDetailsRecord(ContextSchemeValueId contextSchemeValueId,
                                              ContextSchemeId contextSchemeId,
                                              Guid guid,
                                              String value, String meaning, boolean used) {
}
