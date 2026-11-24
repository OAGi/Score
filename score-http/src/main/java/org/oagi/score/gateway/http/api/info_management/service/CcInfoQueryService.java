package org.oagi.score.gateway.http.api.info_management.service;

import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.cc_management.model.CcListEntryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcListTypes;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.repository.criteria.CcListFilterCriteria;
import org.oagi.score.gateway.http.api.cc_management.service.CcListService;
import org.oagi.score.gateway.http.api.cc_management.service.CcQueryService;
import org.oagi.score.gateway.http.api.info_management.model.SummaryCc;
import org.oagi.score.gateway.http.api.info_management.model.SummaryCcExt;
import org.oagi.score.gateway.http.api.info_management.model.SummaryCcExtCount;
import org.oagi.score.gateway.http.api.info_management.model.SummaryCcExtInfoRecord;
import org.oagi.score.gateway.http.api.info_management.model.SummaryCcInfoRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;

@Service
@Transactional(readOnly = true)
public class CcInfoQueryService {

    private static final Set<String> LIST_TYPES = Set.of("CODE_LIST", "AGENCY_ID_LIST");

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private CcListService ccListService;

    @Autowired
    private CcQueryService ccQueryService;

    public SummaryCcInfoRecord getSummaryCcInfo(ScoreUser requester, LibraryId libraryId) {
        if (requester == null) {
            throw new DataAccessForbiddenException("Need authentication to access information.");
        }

        var ccQuery = repositoryFactory.ccQueryRepository(requester);
        List<SummaryCc> summaryCcList = ccQuery.getSummaryCcList(libraryId);
        // The totals are of the components; the Code Lists and the Agency ID Lists are only in the counts by type.
        List<SummaryCc> componentList = summaryCcList.stream()
                .filter(e -> !LIST_TYPES.contains(e.getType()))
                .toList();

        Map<CcState, Integer> numberOfTotalCcByStates =
                componentList.stream().collect(Collectors.toMap(SummaryCc::getState, (e) -> 1, Integer::sum));

        Map<CcState, Integer> numberOfMyCcByStates =
                componentList.stream()
                        .filter(e -> e.getOwnerUserId().equals(requester.userId()))
                        .collect(Collectors.toMap(SummaryCc::getState, (e) -> 1, Integer::sum));

        Map<String, Map<CcState, Integer>> ccByUsersAndStates = componentList.stream()
                .collect(groupingBy(SummaryCc::getOwnerUsername,
                        Collectors.toMap(SummaryCc::getState, (e) -> 1, Integer::sum)));

        Map<String, Map<String, Map<CcState, Integer>>> ccByTypesUsersAndStates = summaryCcList.stream()
                .collect(groupingBy(SummaryCc::getType,
                        groupingBy(SummaryCc::getOwnerUsername,
                                Collectors.toMap(SummaryCc::getState, (e) -> 1, Integer::sum))));

        ReleaseSummaryRecord workingRelease = repositoryFactory.releaseQueryRepository(requester)
                .getReleaseSummary(libraryId, "Working");

        List<CcListEntryRecord> myRecentCCs =
                ccQueryService.getCcList(requester, CcListFilterCriteria.builder(workingRelease.releaseId())
                        .types(CcListTypes.fromString("ACC,ASCCP,BCCP,DT"))
                        .states(Arrays.asList(CcState.WIP, CcState.Draft, CcState.Candidate))
                        .ownerLoginIdList(Arrays.asList(String.valueOf(requester.username())))
                        .build(), pageRequest(0, 5, "-lastUpdateTimestamp")).result();

        return new SummaryCcInfoRecord(
                numberOfTotalCcByStates, numberOfMyCcByStates, ccByUsersAndStates, myRecentCCs,
                ccByTypesUsersAndStates);
    }

    public SummaryCcExtInfoRecord getSummaryCcExtInfo(ScoreUser requester, LibraryId libraryId, ReleaseId releaseId) {
        if (requester == null) {
            throw new DataAccessForbiddenException("Need authentication to access information.");
        }

        var ccQuery = repositoryFactory.ccQueryRepository(requester);
        // What is counted is how many User Extensions each release has, not how many are different: the same one in two releases is two.
        List<SummaryCcExtCount> ccExtCounts = ccQuery.getSummaryCcExtCounts(libraryId).stream()
                .filter(e -> releaseId == null || releaseId.value().longValue() <= 0 || e.releaseId().equals(releaseId))
                .toList();

        Map<CcState, Integer> numberOfTotalCcExtByStates = ccExtCounts.stream()
                .collect(Collectors.toMap(SummaryCcExtCount::state, SummaryCcExtCount::count, Integer::sum));

        Map<CcState, Integer> numberOfMyCcExtByStates = ccExtCounts.stream()
                .filter(e -> e.ownerUsername().equals(requester.username()))
                .collect(Collectors.toMap(SummaryCcExtCount::state, SummaryCcExtCount::count, Integer::sum));

        Map<String, Map<CcState, Integer>> ccExtByUsersAndStates = ccExtCounts.stream()
                .collect(groupingBy(SummaryCcExtCount::ownerUsername,
                        Collectors.toMap(SummaryCcExtCount::state, SummaryCcExtCount::count, Integer::sum)));

        List<SummaryCcExt> myExtensionsUnusedInBIEs = ccListService.getMyExtensionsUnusedInBIEs(requester, libraryId);

        return new SummaryCcExtInfoRecord(
                numberOfTotalCcExtByStates, numberOfMyCcExtByStates, ccExtByUsersAndStates, myExtensionsUnusedInBIEs,
                ccExtCounts);
    }
}
