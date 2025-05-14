package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.model.*;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BiePackageListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;

import java.util.Collection;
import java.util.List;

public interface BiePackageQueryRepository {

    BiePackageSummaryRecord getBiePackageSummary(BiePackageId biePackageId);

    List<BiePackageSummaryRecord> getBiePackageSummaryList(
            Collection<BiePackageId> biePackageIds);

    ResultAndCount<BiePackageListEntryRecord> getBiePackageList(
            BiePackageListFilterCriteria filterCriteria, PageRequest pageRequest);

    BiePackageDetailsRecord getBiePackageDetails(BiePackageId biePackageId);

    List<TopLevelAsbiepId> getTopLevelAsbiepIdListInBiePackage(BiePackageId biePackageId);

}
