package org.oagi.score.gateway.http.api.business_term_management.repository.criteria;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;

import java.math.BigInteger;
import java.util.Collection;

public record AssignedBusinessTermListFilterCriteria(
        BigInteger assignedBusinessTermId,
        BigInteger bieId,
        Collection<String> bieTypeList,
        String bieDen,
        Boolean primaryIndicator,
        String typeCode,
        String businessTerm,
        String externalReferenceUri,

        @Nullable Collection<String> updaterLoginIdList,
        @Nullable DateRangeCriteria lastUpdatedTimestampRange) {
}
