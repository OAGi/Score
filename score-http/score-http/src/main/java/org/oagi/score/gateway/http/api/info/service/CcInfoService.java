package org.oagi.score.gateway.http.api.info.service;

import org.oagi.score.data.Release;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.bie_management.data.BieListRequest;
import org.oagi.score.gateway.http.api.cc_management.data.CcListRequest;
import org.oagi.score.gateway.http.api.cc_management.data.CcListTypes;
import org.oagi.score.gateway.http.api.cc_management.service.CcListService;
import org.oagi.score.gateway.http.api.info.data.*;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.CoreComponentRepository;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.component.release.ReleaseRepository;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.common.data.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class CcInfoService {

    @Autowired
    private CoreComponentRepository ccRepository;

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private CcListService ccListService;

    @Autowired
    private SessionService sessionService;

    public SummaryCcInfo getSummaryCcInfo(AuthenticatedPrincipal user) {
        AppUser requester = sessionService.getAppUserByUsername(user);
        if (user == null || requester == null) {
            throw new DataAccessForbiddenException("Need authentication to access information.");
        }

        List<SummaryCc> summaryCcList = ccRepository.getSummaryCcList(requester);

        SummaryCcInfo info = new SummaryCcInfo();
        Map<CcState, Integer> numberOfTotalCcByStates =
                summaryCcList.stream().collect(Collectors.toMap(SummaryCc::getState, (e) -> 1, Integer::sum));
        info.setNumberOfTotalCcByStates(numberOfTotalCcByStates);

        Map<CcState, Integer> numberOfMyCcByStates =
                summaryCcList.stream()
                        .filter(e -> e.getOwnerUserId().equals(requester.getAppUserId()))
                        .collect(Collectors.toMap(SummaryCc::getState, (e) -> 1, Integer::sum));
        info.setNumberOfMyCcByStates(numberOfMyCcByStates);

        Map<String, Map<CcState, Integer>> ccByUsersAndStates = summaryCcList.stream()
                .collect(groupingBy(SummaryCc::getOwnerUsername,
                        Collectors.toMap(SummaryCc::getState, (e) -> 1, Integer::sum)));
        info.setCcByUsersAndStates(ccByUsersAndStates);

        CcListRequest request = new CcListRequest();
        request.setTypes(CcListTypes.fromString("ACC,ASCCP,BCCP,BDT"));
        request.setStates(Arrays.asList(CcState.WIP, CcState.Draft, CcState.Candidate));
        PageRequest pageRequest = new PageRequest();
        pageRequest.setSortActive("lastUpdateTimestamp");
        pageRequest.setSortDirection("desc");
        pageRequest.setPageIndex(0);
        pageRequest.setPageSize(5);
        request.setOwnerLoginIds(Arrays.asList(String.valueOf(requester.getLoginId())));
        request.setPageRequest(pageRequest);

        Release workingRelease = releaseRepository.findByReleaseNum("Working").stream().findFirst().get();
        request.setReleaseId(workingRelease.getReleaseId());

        info.setMyRecentCCs(ccListService.getCcList(request).getList());

        return info;
    }

    public SummaryCcExtInfo getSummaryCcExtInfo(AuthenticatedPrincipal user, BigInteger releaseId) {
        AppUser requester = sessionService.getAppUserByUsername(user);
        if (user == null || requester == null) {
            throw new DataAccessForbiddenException("Need authentication to access information.");
        }

        List<SummaryCcExt> summaryCcExtList = ccRepository.getSummaryCcExtList(releaseId);

        SummaryCcExtInfo info = new SummaryCcExtInfo();
        Map<CcState, Integer> numberOfCcExtByStates =
                summaryCcExtList.stream().collect(Collectors.toMap(SummaryCcExt::getState, (e) -> 1, Integer::sum));
        info.setNumberOfTotalCcExtByStates(numberOfCcExtByStates);

        Map<CcState, Integer> numberOfTotalCcExtByStates =
                summaryCcExtList.stream()
                        .collect(Collectors.toMap(SummaryCcExt::getState, (e) -> 1, Integer::sum));
        info.setNumberOfTotalCcExtByStates(numberOfTotalCcExtByStates);

        Map<CcState, Integer> numberOfMyBieByStates =
                summaryCcExtList.stream()
                        .filter(e -> e.getOwnerUserId().equals(requester.getAppUserId()))
                        .collect(Collectors.toMap(SummaryCcExt::getState, (e) -> 1, Integer::sum));
        info.setNumberOfMyCcExtByStates(numberOfMyBieByStates);

        Map<String, Map<CcState, Integer>> ccExtByUsersAndStates = summaryCcExtList.stream()
                .collect(groupingBy(SummaryCcExt::getOwnerUsername,
                        Collectors.toMap(SummaryCcExt::getState, (e) -> 1, Integer::sum)));
        info.setCcExtByUsersAndStates(ccExtByUsersAndStates);

        info.setMyExtensionsUnusedInBIEs(ccListService.getMyExtensionsUnusedInBIEs(user));

        return info;
    }
}
