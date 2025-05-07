package org.oagi.score.gateway.http.api.agency_id_management.service;

import org.oagi.score.gateway.http.api.agency_id_management.model.*;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.agency_id_management.repository.criteria.AgencyIdListListFilterCriteria;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.service.CcQueryService;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

        List<AgencyIdListSummaryRecord> agencyIdListSummaryList =
                query(requester).getAgencyIdListSummaryListInStates(releaseId, Arrays.asList(Published, Production));
        return agencyIdListSummaryList;
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
