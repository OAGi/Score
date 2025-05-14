package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.model.BieListEntryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BieSet;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BieListFilterCriteria;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BieListInBiePackageFilterCriteria;
import org.oagi.score.gateway.http.api.info_management.model.SummaryBie;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;

import java.util.List;

public interface BieQueryRepository {

    ResultAndCount<BieListEntryRecord> getBieList(
            BieListFilterCriteria filterCriteria, PageRequest pageRequest);

    ResultAndCount<BieListEntryRecord> getBieList(
            BieListInBiePackageFilterCriteria filterCriteria, PageRequest pageRequest);

    List<SummaryBie> getSummaryBieList(
            LibraryId libraryId, ReleaseId releaseId, boolean tenantEnabled, List<TenantId> userTenantIds);

    BieSet getBieSet(TopLevelAsbiepId topLevelAsbiepId, boolean used);

}
