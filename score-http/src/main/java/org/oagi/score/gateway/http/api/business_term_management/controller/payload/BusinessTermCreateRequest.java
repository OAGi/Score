package org.oagi.score.gateway.http.api.business_term_management.controller.payload;

public record BusinessTermCreateRequest(
        String businessTerm,
        String externalReferenceId,
        String externalReferenceUri,
        String definition,
        String comment) {
}
