package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.model.BieListEntryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageListEntryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BieListInBiePackageFilterCriteria;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BiePackageListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;

import java.util.Collection;
import java.util.List;

public interface BiePackageQueryRepository {

    BiePackageSummaryRecord getBiePackageSummary(BiePackageId biePackageId);

    List<BiePackageSummaryRecord> getBiePackageSummaryList(
            Collection<BiePackageId> biePackageIds);

    /**
     * Returns the BIE packages that reference any of the given packages as their previous revision
     * ({@code prev_bie_package_id}) but are NOT themselves part of the given collection. Revision is a real
     * dependency, so these packages must be discarded together with (or before) the ones they reference.
     * Copy/uplift provenance ({@code source_bie_package_id}) is intentionally excluded — breaking it is harmless.
     */
    List<BiePackageSummaryRecord> getBiePackagesReferencingAsPrevious(
            Collection<BiePackageId> biePackageIds);

    ResultAndCount<BiePackageListEntryRecord> getBiePackageList(
            BiePackageListFilterCriteria filterCriteria, PageRequest pageRequest);

    BiePackageDetailsRecord getBiePackageDetails(BiePackageId biePackageId);

    List<TopLevelAsbiepId> getTopLevelAsbiepIdListInBiePackage(BiePackageId biePackageId);

    ResultAndCount<BieListEntryRecord> getBieListInBiePackage(
            BieListInBiePackageFilterCriteria filterCriteria, PageRequest pageRequest);

    boolean exists(BiePackageId biePackageId, TopLevelAsbiepId topLevelAsbiepId);

    boolean hasDuplicateVersion(BiePackageId biePackageId, String versionId);
}
