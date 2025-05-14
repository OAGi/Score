package org.oagi.score.gateway.http.api.code_list_management.repository;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.*;
import org.oagi.score.gateway.http.api.code_list_management.repository.criteria.CodeListListFilterCriteria;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;

import java.util.Collection;
import java.util.List;

public interface CodeListQueryRepository {

    CodeListSummaryRecord getCodeListSummary(
            CodeListManifestId codeListManifestId);

    List<CodeListSummaryRecord> getCodeListSummaryList();

    List<CodeListSummaryRecord> getCodeListSummaryList(ReleaseId releaseId);

    List<CodeListSummaryRecord> getCodeListSummaryList(Collection<ReleaseId> releaseIdList);

    List<CodeListSummaryRecord> getCodeListSummaryList(LibraryId libraryId, String releaseNum, CcState state);

    List<CodeListSummaryRecord> availableCodeListByDtManifestId(DtManifestId dtManifestId, List<CcState> states);

    List<CodeListSummaryRecord> availableCodeListByDtScManifestId(DtScManifestId dtScManifestId, List<CcState> states);

    // Details

    CodeListDetailsRecord getCodeListDetails(
            CodeListManifestId codeListManifestId);

    CodeListDetailsRecord getPrevCodeListDetails(
            CodeListManifestId codeListManifestId);

    List<CodeListValueDetailsRecord> getCodeListValueDetailsList(
            CodeListManifestId codeListManifestId);

    CodeListValueDetailsRecord getCodeListValueDetails(
            CodeListValueManifestId codeListValueManifestId);

    ResultAndCount<CodeListListEntryRecord> getCodeListList(
            CodeListListFilterCriteria filterCriteria, PageRequest pageRequest);

    boolean hasRecordsByNamespaceId(NamespaceId namespaceId);

    boolean hasSameCodeList(ReleaseId releaseId,
                            CodeListManifestId codeListManifestId,
                            AgencyIdListValueManifestId agencyIdListValueManifestId,
                            String listId, String versionId);

    boolean hasSameNameCodeList(ReleaseId releaseId,
                                CodeListManifestId codeListManifestId,
                                String codeListName);

}
