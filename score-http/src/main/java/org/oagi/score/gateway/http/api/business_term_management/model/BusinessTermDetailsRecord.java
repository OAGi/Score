package org.oagi.score.gateway.http.api.business_term_management.model;

import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record BusinessTermDetailsRecord(
        BusinessTermId businessTermId,
        Guid guid,
        String businessTerm,
        String definition,
        String comment,
        String externalReferenceId,
        String externalReferenceUri,
        WhoAndWhen created,
        WhoAndWhen lastUpdated) {

}
