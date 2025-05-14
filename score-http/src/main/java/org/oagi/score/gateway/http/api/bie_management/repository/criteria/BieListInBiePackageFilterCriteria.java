package org.oagi.score.gateway.http.api.bie_management.repository.criteria;

import org.oagi.score.gateway.http.api.bie_management.model.BiePackageId;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;

import java.util.Collection;

public record BieListInBiePackageFilterCriteria(
        BiePackageId biePackageId,
        String den, String businessTerm, String version, String remark,
        Collection<String> ownerLoginIdList,
        Collection<String> updaterLoginIdList,
        DateRangeCriteria lastUpdatedTimestampRange) {

}
