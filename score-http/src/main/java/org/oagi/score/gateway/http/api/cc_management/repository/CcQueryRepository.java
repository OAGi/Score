package org.oagi.score.gateway.http.api.cc_management.repository;

import org.oagi.score.gateway.http.api.cc_management.controller.payload.CcChangesResponse;
import org.oagi.score.gateway.http.api.cc_management.model.CcListEntryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.GraphManifestIds;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
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

    /**
     * The components a graph rooted at the given ACC, ASCCP, BCCP or DT reaches, found in one query.
     * It follows the manifests as they are, so it may reach more than a graph does (which stops at the releases it
     * is made for and, for a developer, at the user extension groups); never less.
     */
    GraphManifestIds getGraphManifestIds(CcType type, ManifestId root);

}
