package org.oagi.score.gateway.http.api.agency_id_management.service;

import org.oagi.score.gateway.http.api.agency_id_management.model.*;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.agency_id_management.repository.criteria.AgencyIdListListFilterCriteria;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.service.CcQueryService;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseQueryRepository;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.cc_management.model.CcState.Deleted;
import static org.oagi.score.gateway.http.api.cc_management.model.CcState.Production;
import static org.oagi.score.gateway.http.api.cc_management.model.CcState.Published;

@Service
@Transactional(readOnly = true)
public class AgencyIdListQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private AgencyIdListQueryRepository query(ScoreUser requester) {
        return repositoryFactory.agencyIdListQueryRepository(requester);
    }

    private ReleaseQueryRepository releaseQuery(ScoreUser requester) {
        return repositoryFactory.releaseQueryRepository(requester);
    }

    @Autowired
    private CcQueryService ccQueryService;

    public List<AgencyIdListSummaryRecord> getAgencyIdListSummaryList(
            ScoreUser requester, ReleaseId releaseId) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null");
        }
        if (releaseId == null) {
            throw new IllegalArgumentException("`releaseId` must not be null");
        }

        ReleaseSummaryRecord release = releaseQuery(requester).getReleaseSummary(releaseId);
        if (release == null) {
            return Collections.emptyList();
        }
        if (!release.isWorkingRelease()) {
            return query(requester).getAgencyIdListSummaryListInStates(releaseId, Arrays.asList(Published, Production));
        }

        var agencyIdListQuery = query(requester);
        Map<AgencyIdListManifestId, AgencyIdListSummaryRecord> visibleAgencyIdLists = new LinkedHashMap<>();

        agencyIdListQuery.getAgencyIdListSummaryList(releaseId).stream()
                .filter(agencyIdList -> agencyIdList.state() != Deleted)
                .forEach(agencyIdList -> visibleAgencyIdLists.put(agencyIdList.agencyIdListManifestId(), agencyIdList));

        Set<ReleaseId> dependencyReleaseIds = releaseQuery(requester).getIncludedReleaseSummaryList(releaseId).stream()
                .map(ReleaseSummaryRecord::releaseId)
                .filter(includedReleaseId -> !releaseId.equals(includedReleaseId))
                .collect(Collectors.toSet());
        if (!dependencyReleaseIds.isEmpty()) {
            agencyIdListQuery.getAgencyIdListSummaryList(dependencyReleaseIds).stream()
                    .filter(agencyIdList -> agencyIdList.state() == Published || agencyIdList.state() == Production)
                    .forEach(agencyIdList -> visibleAgencyIdLists.putIfAbsent(agencyIdList.agencyIdListManifestId(), agencyIdList));
        }

        return visibleAgencyIdLists.values().stream()
                .sorted(Comparator.comparing(AgencyIdListSummaryRecord::name, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(AgencyIdListSummaryRecord::listId, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(AgencyIdListSummaryRecord::versionId, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public List<AgencyIdListSummaryRecord> availableAgencyIdListListByDtManifestId(
            ScoreUser requester, DtManifestId dtManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null");
        }
        if (dtManifestId == null) {
            throw new IllegalArgumentException("`dtManifestId` must not be null");
        }

        List<CcState> states;
        if (requester.isDeveloper()) {
            states = Arrays.asList(Published, Production);
        } else {
            states = Collections.emptyList();
        }

        var query = query(requester);
        return query.availableAgencyIdListByDtManifestId(dtManifestId, states);
    }

    public List<AgencyIdListSummaryRecord> availableAgencyIdListListByDtScManifestId(
            ScoreUser requester, DtScManifestId dtScManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null");
        }
        if (dtScManifestId == null) {
            throw new IllegalArgumentException("`dtScManifestId` must not be null");
        }

        List<CcState> states;
        if (requester.isDeveloper()) {
            states = Arrays.asList(Published);
        } else {
            states = Collections.emptyList();
        }

        var query = query(requester);
        return query.availableAgencyIdListByDtScManifestId(dtScManifestId, states);
    }

    /**
     * @param requester
     * @param agencyIdListManifestId
     * @return
     * @throws IllegalArgumentException
     * @throws EmptyResultDataAccessException
     */
    public AgencyIdListDetailsRecord getAgencyIdListDetails(
            ScoreUser requester, AgencyIdListManifestId agencyIdListManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null");
        }
        if (agencyIdListManifestId == null) {
            throw new IllegalArgumentException("`agencyIdListManifestId` must not be null");
        }

        AgencyIdListDetailsRecord agencyIdListDetails =
                query(requester).getAgencyIdListDetails(agencyIdListManifestId);
        if (agencyIdListDetails == null) {
            throw new EmptyResultDataAccessException(1);
        }
        return agencyIdListDetails;
    }

    public AgencyIdListDetailsRecord getPrevAgencyIdListDetails(
            ScoreUser requester, AgencyIdListManifestId agencyIdListManifestId) {

        AgencyIdListDetailsRecord prevAgencyIdListDetails =
                query(requester).getPrevAgencyIdListDetails(agencyIdListManifestId);
        if (prevAgencyIdListDetails == null) {
            throw new EmptyResultDataAccessException(1);
        }
        return prevAgencyIdListDetails;
    }

    public ResultAndCount<AgencyIdListListEntryRecord> getAgencyIdListList(
            ScoreUser requester, AgencyIdListListFilterCriteria filterCriteria, PageRequest pageRequest) {

        return query(requester).getAgencyIdListList(filterCriteria, pageRequest);
    }

    public boolean hasSameAgencyIdList(
            ScoreUser requester, ReleaseId releaseId,
            AgencyIdListManifestId agencyIdListManifestId,
            AgencyIdListValueManifestId agencyIdListValueManifestId,
            String listId, String versionId) {

        return query(requester).hasSameAgencyIdList(
                releaseId, agencyIdListManifestId, agencyIdListValueManifestId, listId, versionId);
    }

    public boolean hasSameNameAgencyIdList(
            ScoreUser requester, ReleaseId releaseId,
            AgencyIdListManifestId agencyIdListManifestId,
            String agencyIdListName) {

        return query(requester).hasSameNameAgencyIdList(
                releaseId, agencyIdListManifestId, agencyIdListName);
    }
}
