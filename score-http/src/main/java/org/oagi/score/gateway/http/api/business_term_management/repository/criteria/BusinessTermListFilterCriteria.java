package org.oagi.score.gateway.http.api.business_term_management.repository.criteria;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;

import java.util.Collection;

/**
 * Represents filtering criteria for querying a list of business terms.
 */
public record BusinessTermListFilterCriteria(
        String businessTerm,
        String externalReferenceUri,
        String externalReferenceId,
        String definition,
        String comment,

        @Nullable Collection<String> bieTypeList,
        @Nullable Boolean searchByCC,
        @Nullable Collection<AsbieId> byAssignedAsbieIdList,
        @Nullable Collection<BbieId> byAssignedBbieIdList,

        @Nullable Collection<String> updaterLoginIdList,
        @Nullable DateRangeCriteria lastUpdatedTimestampRange) {

}