package org.oagi.score.gateway.http.api.cc_management.repository;

import org.oagi.score.gateway.http.api.cc_management.controller.payload.CcChangesResponse;
import org.oagi.score.gateway.http.api.cc_management.model.CcListEntryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.repository.criteria.CcListFilterCriteria;
import org.oagi.score.gateway.http.api.info_management.model.SummaryCc;
import org.oagi.score.gateway.http.api.info_management.model.SummaryCcExt;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;

import java.util.Collection;
import java.util.List;

public interface CcQueryRepository {

    ResultAndCount<CcListEntryRecord> getCcList(
            CcListFilterCriteria filterCriteria, PageRequest pageRequest);

    List<CcListEntryRecord> getBaseAccList(AccManifestId accManifestId);

    List<SummaryCc> getSummaryCcList(LibraryId libraryId);

    List<SummaryCcExt> getSummaryCcExtList(LibraryId libraryId, ReleaseId releaseId);

    Collection<CcChangesResponse.CcChange> getCcChanges(ReleaseId releaseId);

}
