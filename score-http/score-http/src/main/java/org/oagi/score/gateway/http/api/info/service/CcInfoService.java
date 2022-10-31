package org.oagi.score.gateway.http.api.info.service;

import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.gateway.http.api.cc_management.service.CcListService;
import org.oagi.score.gateway.http.api.info.data.SummaryCcExt;
import org.oagi.score.gateway.http.api.info.data.SummaryCcExtInfo;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.CoreComponentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class CcInfoService {

    @Autowired
    private CoreComponentRepository ccRepository;

    @Autowired
    private CcListService ccListService;

    @Autowired
    private SessionService sessionService;

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
