package org.oagi.score.gateway.http.api.info.service;

import org.oagi.score.data.AppUser;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.bie_management.service.BieRepository;
import org.oagi.score.gateway.http.api.cc_management.data.CcState;
import org.oagi.score.gateway.http.api.cc_management.repository.CoreComponentRepository;
import org.oagi.score.gateway.http.api.cc_management.service.CcListService;
import org.oagi.score.gateway.http.api.context_management.repository.BusinessContextRepository;
import org.oagi.score.gateway.http.api.info.data.SummaryCcExt;
import org.oagi.score.gateway.http.api.info.data.SummaryCcExtInfo;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

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
    private BieRepository bieRepository;

    @Autowired
    private BusinessContextRepository bizCtxRepository;

    @Autowired
    private SessionService sessionService;

    public SummaryCcExtInfo getSummaryCcExtInfo(User user) {
        if (user == null) {
            throw new DataAccessForbiddenException("Need authentication to access information.");
        }

        List<SummaryCcExt> summaryCcExtList = ccRepository.getSummaryCcExtList();
        AppUser requester = sessionService.getAppUser(user);

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
                        .filter(e -> e.getOwnerUserId() == requester.getAppUserId())
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
