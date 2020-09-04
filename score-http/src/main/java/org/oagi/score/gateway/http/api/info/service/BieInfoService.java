package org.oagi.score.gateway.http.api.info.service;

import org.oagi.score.data.AppUser;
import org.oagi.score.data.BieState;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.bie_management.data.BieListRequest;
import org.oagi.score.gateway.http.api.bie_management.service.BieRepository;
import org.oagi.score.gateway.http.api.bie_management.service.BieService;
import org.oagi.score.gateway.http.api.common.data.PageRequest;
import org.oagi.score.gateway.http.api.context_management.repository.BusinessContextRepository;
import org.oagi.score.gateway.http.api.info.data.SummaryBie;
import org.oagi.score.gateway.http.api.info.data.SummaryBieInfo;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class BieInfoService {

    @Autowired
    private BieRepository repository;

    @Autowired
    private BieService bieService;

    @Autowired
    private BusinessContextRepository bizCtxRepository;

    @Autowired
    private SessionService sessionService;

    public SummaryBieInfo getSummaryBieInfo(AuthenticatedPrincipal user) {
        if (user == null) {
            throw new DataAccessForbiddenException("Need authentication to access information.");
        }

        List<SummaryBie> summaryBieList = repository.getSummaryBieList();
        AppUser requester = sessionService.getAppUser(user);

        SummaryBieInfo info = new SummaryBieInfo();
        Map<BieState, Integer> numberOfTotalBieByStates =
                summaryBieList.stream().collect(Collectors.toMap(SummaryBie::getState, (e) -> 1, Integer::sum));
        info.setNumberOfTotalBieByStates(numberOfTotalBieByStates);

        Map<BieState, Integer> numberOfMyBieByStates =
                summaryBieList.stream()
                        .filter(e -> e.getOwnerUserId() == requester.getAppUserId())
                        .collect(Collectors.toMap(SummaryBie::getState, (e) -> 1, Integer::sum));
        info.setNumberOfMyBieByStates(numberOfMyBieByStates);

        Map<String, Map<BieState, Integer>> bieByUsersAndStates = summaryBieList.stream()
                .collect(groupingBy(SummaryBie::getOwnerUsername,
                        Collectors.toMap(SummaryBie::getState, (e) -> 1, Integer::sum)));
        info.setBieByUsersAndStates(bieByUsersAndStates);

        BieListRequest request = new BieListRequest();
        PageRequest pageRequest = new PageRequest();
        pageRequest.setSortActive("lastUpdateTimestamp");
        pageRequest.setSortDirection("desc");
        pageRequest.setPageIndex(0);
        pageRequest.setPageSize(5);
        request.setOwnerLoginIds(Arrays.asList(requester.getLoginId()));
        request.setPageRequest(pageRequest);

        info.setMyRecentBIEs(bieService.getBieList(user, request).getList());

        return info;
    }
}
