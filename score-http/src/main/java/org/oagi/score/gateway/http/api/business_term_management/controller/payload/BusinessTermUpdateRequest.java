package org.oagi.score.gateway.http.api.business_term_management.controller.payload;

import org.oagi.score.gateway.http.api.business_term_management.model.BusinessTermId;

public record BusinessTermUpdateRequest(
        BusinessTermId businessTermId,
        String businessTerm,
        String externalReferenceId,
        String externalReferenceUri,
        String definition,
        String comment) {
}
