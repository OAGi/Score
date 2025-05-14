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
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;

@Service
@Transactional(readOnly = true)
public class CcInfoQueryService {

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

        Map<CcState, Integer> numberOfTotalCcByStates =
                summaryCcList.stream().collect(Collectors.toMap(SummaryCc::getState, (e) -> 1, Integer::sum));

        Map<CcState, Integer> numberOfMyCcByStates =
                summaryCcList.stream()
                        .filter(e -> e.getOwnerUserId().equals(requester.userId()))
                        .collect(Collectors.toMap(SummaryCc::getState, (e) -> 1, Integer::sum));

        Map<String, Map<CcState, Integer>> ccByUsersAndStates = summaryCcList.stream()
                .collect(groupingBy(SummaryCc::getOwnerUsername,
                        Collectors.toMap(SummaryCc::getState, (e) -> 1, Integer::sum)));

        ReleaseSummaryRecord workingRelease = repositoryFactory.releaseQueryRepository(requester)
                .getReleaseSummary(libraryId, "Working");

        List<CcListEntryRecord> myRecentCCs =
                ccQueryService.getCcList(requester, CcListFilterCriteria.builder(workingRelease.releaseId())
                        .types(CcListTypes.fromString("ACC,ASCCP,BCCP,DT"))
                        .states(Arrays.asList(CcState.WIP, CcState.Draft, CcState.Candidate))
                        .ownerLoginIdList(Arrays.asList(String.valueOf(requester.username())))
                        .build(), pageRequest(0, 5, "-lastUpdateTimestamp")).result();

        return new SummaryCcInfoRecord(
                numberOfTotalCcByStates, numberOfMyCcByStates, ccByUsersAndStates, myRecentCCs);
    }

    public SummaryCcExtInfoRecord getSummaryCcExtInfo(ScoreUser requester, LibraryId libraryId, ReleaseId releaseId) {
        if (requester == null) {
            throw new DataAccessForbiddenException("Need authentication to access information.");
        }

        var ccQuery = repositoryFactory.ccQueryRepository(requester);
        List<SummaryCcExt> summaryCcExtList = ccQuery.getSummaryCcExtList(libraryId, releaseId);

        Map<CcState, Integer> numberOfTotalCcExtByStates =
                summaryCcExtList.stream()
                        .collect(Collectors.toMap(SummaryCcExt::getState, (e) -> 1, Integer::sum));

        Map<CcState, Integer> numberOfMyCcExtByStates =
                summaryCcExtList.stream().collect(Collectors.toMap(SummaryCcExt::getState, (e) -> 1, Integer::sum));

//        Map<CcState, Integer> numberOfMyBieByStates =
//                summaryCcExtList.stream()
//                        .filter(e -> e.getOwnerUserId().equals(requester.userId()))
//                        .collect(Collectors.toMap(SummaryCcExt::getState, (e) -> 1, Integer::sum));

        Map<String, Map<CcState, Integer>> ccExtByUsersAndStates = summaryCcExtList.stream()
                .collect(groupingBy(SummaryCcExt::getOwnerUsername,
                        Collectors.toMap(SummaryCcExt::getState, (e) -> 1, Integer::sum)));

        List<SummaryCcExt> myExtensionsUnusedInBIEs = ccListService.getMyExtensionsUnusedInBIEs(requester, libraryId);

        return new SummaryCcExtInfoRecord(
                numberOfTotalCcExtByStates, numberOfMyCcExtByStates, ccExtByUsersAndStates, myExtensionsUnusedInBIEs);
    }
}
