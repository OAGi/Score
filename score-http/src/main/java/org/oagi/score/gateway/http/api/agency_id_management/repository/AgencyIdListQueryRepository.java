package org.oagi.score.gateway.http.api.agency_id_management.repository;

import org.oagi.score.gateway.http.api.agency_id_management.model.*;
import org.oagi.score.gateway.http.api.agency_id_management.repository.criteria.AgencyIdListListFilterCriteria;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;

import java.util.Collection;
import java.util.List;

public interface AgencyIdListQueryRepository {

    // Summary

    AgencyIdListSummaryRecord getAgencyIdListSummary(
            AgencyIdListManifestId agencyIdListManifestId);

    List<AgencyIdListSummaryRecord> getAgencyIdListSummaryList();

    List<AgencyIdListSummaryRecord> getAgencyIdListSummaryList(ReleaseId releaseId);

    List<AgencyIdListSummaryRecord> getAgencyIdListSummaryList(Collection<ReleaseId> releaseIdList);

    List<AgencyIdListSummaryRecord> getAgencyIdListSummaryList(LibraryId libraryId, String releaseNum, CcState state);

    List<AgencyIdListSummaryRecord> availableAgencyIdListByDtManifestId(
            DtManifestId dtManifestId, List<CcState> states);

    List<AgencyIdListSummaryRecord> availableAgencyIdListByDtScManifestId(
            DtScManifestId dtScManifestId, List<CcState> states);

    List<AgencyIdListSummaryRecord> getAgencyIdListSummaryListInStates(
            ReleaseId releaseId, List<CcState> states);

    List<AgencyIdListSummaryRecord> getAgencyIdListSummaryListByBccpManifestIdInStates(
            BccpManifestId bccpManifestId, List<CcState> states);

    List<AgencyIdListSummaryRecord> getAgencyIdListSummaryListByDtScManifestIdInStates(
            DtScManifestId dtScManifestId, List<CcState> states);

    // Details

    AgencyIdListDetailsRecord getAgencyIdListDetails(
            AgencyIdListManifestId agencyIdListManifestId);

    AgencyIdListDetailsRecord getPrevAgencyIdListDetails(
            AgencyIdListManifestId agencyIdListManifestId);

    // Values
    List<AgencyIdListValueSummaryRecord> getAgencyIdListValueSummaryList();

    List<AgencyIdListValueSummaryRecord> getAgencyIdListValueSummaryList(ReleaseId releaseId);

    AgencyIdListValueSummaryRecord getAgencyIdListValueSummary(
            AgencyIdListValueManifestId agencyIdListValueManifestId);

    List<AgencyIdListValueDetailsRecord> getAgencyIdListValueDetailsList(
            AgencyIdListManifestId agencyIdListManifestId);

    AgencyIdListValueDetailsRecord getAgencyIdListValueDetails(
            AgencyIdListValueManifestId agencyIdListValueManifestId);

    ResultAndCount<AgencyIdListListEntryRecord> getAgencyIdListList(
            AgencyIdListListFilterCriteria filterCriteria, PageRequest pageRequest);

    boolean hasRecordsByNamespaceId(NamespaceId namespaceId);

    int countActiveAgencyIdLists(AgencyIdListManifestId excludedAgencyIdListManifestId);

    boolean hasSameAgencyIdList(ReleaseId releaseId,
                                AgencyIdListManifestId agencyIdListManifestId,
                                AgencyIdListValueManifestId agencyIdListValueManifestId,
                                String listId, String versionId);

    boolean hasSameNameAgencyIdList(ReleaseId releaseId,
                                    AgencyIdListManifestId agencyIdListManifestId,
                                    String agencyIdListName);

}
