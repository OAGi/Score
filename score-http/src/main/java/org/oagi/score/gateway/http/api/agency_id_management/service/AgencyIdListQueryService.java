package org.oagi.score.gateway.http.api.agency_id_management.service;

import org.oagi.score.gateway.http.api.agency_id_management.model.*;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.agency_id_management.repository.criteria.AgencyIdListListFilterCriteria;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
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
import java.util.Collection;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.function.Function;
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

        List<CcState> states = availableStates(requester);
        var query = query(requester);
        List<AgencyIdListSummaryRecord> availableAgencyIdLists =
                query.availableAgencyIdListByDtManifestId(dtManifestId, states);
        if (!availableAgencyIdLists.isEmpty() || query.hasAgencyIdListAvailabilityByDtManifestId(dtManifestId)) {
            return availableAgencyIdLists;
        }

        DtSummaryRecord dt = repositoryFactory.dtQueryRepository(requester).getDtSummary(dtManifestId);
        return dt == null ? Collections.emptyList() : filterByStates(
                query.getAgencyIdListSummaryList(dt.release().releaseId()), states);
    }

    /**
     * Filters a caller-provided release-wide list without querying the database.
     * A null allowed-id set means unrestricted; an empty set means restricted
     * with no agency-id-list option.
     */
    public List<AgencyIdListSummaryRecord> availableAgencyIdListListByDtManifestId(
            ScoreUser requester, DtManifestId dtManifestId,
            List<AgencyIdListSummaryRecord> agencyIdListList,
            Set<AgencyIdListManifestId> allowedManifestIds) {
        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null");
        }
        if (dtManifestId == null) {
            throw new IllegalArgumentException("`dtManifestId` must not be null");
        }
        return agencyIdListList == null
                ? availableAgencyIdListListByDtManifestId(requester, dtManifestId)
                : filterByAllowedManifestIds(agencyIdListList, allowedManifestIds);
    }

    public List<AgencyIdListSummaryRecord> availableAgencyIdListListByDtManifestId(
            ScoreUser requester, DtManifestId dtManifestId,
            List<AgencyIdListSummaryRecord> agencyIdListList,
            List<DtAwdPriSummaryRecord> approvedPrimitives) {
        return availableAgencyIdListListByDtManifestId(requester, dtManifestId, agencyIdListList,
                extractManifestIds(approvedPrimitives, DtAwdPriSummaryRecord::agencyIdListManifestId));
    }

    public List<AgencyIdListSummaryRecord> availableAgencyIdListListByDtScManifestId(
            ScoreUser requester, DtScManifestId dtScManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null");
        }
        if (dtScManifestId == null) {
            throw new IllegalArgumentException("`dtScManifestId` must not be null");
        }

        // DT and DT_SC use the same visibility policy.  A supplementary data
        // type must expose the same published/production candidates as a data
        // type when resolving an unrestricted value domain.
        List<CcState> states = availableStates(requester);
        var query = query(requester);
        List<AgencyIdListSummaryRecord> availableAgencyIdLists =
                query.availableAgencyIdListByDtScManifestId(dtScManifestId, states);
        if (!availableAgencyIdLists.isEmpty() || query.hasAgencyIdListAvailabilityByDtScManifestId(dtScManifestId)) {
            return availableAgencyIdLists;
        }

        DtScSummaryRecord dtSc = repositoryFactory.dtQueryRepository(requester).getDtScSummary(dtScManifestId);
        return dtSc == null ? Collections.emptyList() : filterByStates(
                query.getAgencyIdListSummaryList(dtSc.release().releaseId()), states);
    }

    /** Database-free counterpart for a preloaded target release list. */
    public List<AgencyIdListSummaryRecord> availableAgencyIdListListByDtScManifestId(
            ScoreUser requester, DtScManifestId dtScManifestId,
            List<AgencyIdListSummaryRecord> agencyIdListList,
            Set<AgencyIdListManifestId> allowedManifestIds) {
        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null");
        }
        if (dtScManifestId == null) {
            throw new IllegalArgumentException("`dtScManifestId` must not be null");
        }
        return agencyIdListList == null
                ? availableAgencyIdListListByDtScManifestId(requester, dtScManifestId)
                : filterByAllowedManifestIds(agencyIdListList, allowedManifestIds);
    }

    public List<AgencyIdListSummaryRecord> availableAgencyIdListListByDtScManifestId(
            ScoreUser requester, DtScManifestId dtScManifestId,
            List<AgencyIdListSummaryRecord> agencyIdListList,
            List<DtScAwdPriSummaryRecord> approvedPrimitives) {
        return availableAgencyIdListListByDtScManifestId(requester, dtScManifestId, agencyIdListList,
                extractManifestIds(approvedPrimitives, DtScAwdPriSummaryRecord::agencyIdListManifestId));
    }

    private List<CcState> availableStates(ScoreUser requester) {
        return requester.isDeveloper()
                ? Arrays.asList(Published, Production)
                : Collections.emptyList();
    }

    private List<AgencyIdListSummaryRecord> filterByStates(
            List<AgencyIdListSummaryRecord> agencyIdLists, List<CcState> states) {
        if (states.isEmpty()) {
            return agencyIdLists;
        }
        return agencyIdLists.stream()
                .filter(agencyIdList -> agencyIdList != null && states.contains(agencyIdList.state()))
                .collect(Collectors.toList());
    }

    private List<AgencyIdListSummaryRecord> filterByAllowedManifestIds(
            Collection<AgencyIdListSummaryRecord> agencyIdLists,
            Set<AgencyIdListManifestId> allowedManifestIds) {
        Set<AgencyIdListManifestId> expandedAllowedManifestIds = expandDerivedAgencyIdLists(
                agencyIdLists, allowedManifestIds);
        return agencyIdLists.stream()
                .filter(Objects::nonNull)
                .filter(agencyIdList -> expandedAllowedManifestIds == null
                        || expandedAllowedManifestIds.contains(agencyIdList.agencyIdListManifestId()))
                .collect(Collectors.toList());
    }

    private Set<AgencyIdListManifestId> expandDerivedAgencyIdLists(
            Collection<AgencyIdListSummaryRecord> agencyIdLists,
            Set<AgencyIdListManifestId> allowedManifestIds) {
        if (allowedManifestIds == null) {
            return null;
        }
        Set<AgencyIdListManifestId> expanded = new LinkedHashSet<>(allowedManifestIds);
        boolean changed;
        do {
            changed = false;
            for (AgencyIdListSummaryRecord agencyIdList : agencyIdLists) {
                if (agencyIdList != null && agencyIdList.basedAgencyIdListManifestId() != null
                        && expanded.contains(agencyIdList.basedAgencyIdListManifestId())) {
                    changed |= expanded.add(agencyIdList.agencyIdListManifestId());
                }
            }
        } while (changed);
        return expanded;
    }

    private <P> Set<AgencyIdListManifestId> extractManifestIds(
            Collection<P> approvedPrimitives,
            Function<P, AgencyIdListManifestId> manifestIdExtractor) {
        if (approvedPrimitives == null || approvedPrimitives.isEmpty()) {
            return null;
        }
        Set<AgencyIdListManifestId> manifestIds = approvedPrimitives.stream()
                .filter(Objects::nonNull)
                .map(manifestIdExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return manifestIds.isEmpty() ? null : manifestIds;
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
