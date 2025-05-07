package org.oagi.score.gateway.http.api.business_term_management.model;

import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextSummaryRecord;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

import java.math.BigInteger;
import java.util.List;

public record AssignedBusinessTermDetailsRecord(
        BusinessTermId assignedBizTermId,
        BigInteger bieId,
        String bieType,
        String den,
        boolean primaryIndicator,
        String typeCode,
        BigInteger businessTermId,
        String businessTerm,
        String externalReferenceUri,
        List<BusinessContextSummaryRecord> businessContextList,

        WhoAndWhen created,
        WhoAndWhen lastUpdated) {
}
