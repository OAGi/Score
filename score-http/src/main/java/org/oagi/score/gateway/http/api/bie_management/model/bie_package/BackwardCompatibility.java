package org.oagi.score.gateway.http.api.bie_management.model.bie_package;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude
public record BackwardCompatibility(
        // Issue #1733: syntaxIndependent is still computed and carried internally,
        // but its definition is unsettled, so it is intentionally NOT exposed in the
        // generated BIE Package manifest. Only the per-syntax indicators are emitted.
        @JsonIgnore boolean syntaxIndependent,
        boolean xmlSchema,
        boolean jsonSchema) {
}
