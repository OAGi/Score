package org.oagi.score.gateway.http.api.info.service;

import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.bie_management.data.BieListRequest;
import org.oagi.score.gateway.http.api.bie_management.service.BieRepository;
import org.oagi.score.gateway.http.api.bie_management.service.BieService;
import org.oagi.score.gateway.http.api.info.data.SummaryBie;
import org.oagi.score.gateway.http.api.info.data.SummaryBieInfo;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.service.common.data.AppUser;
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
public class BieInfoService {

    @Autowired
    private BieRepository repository;

    @Autowired
    private BieService bieService;

    @Autowired
    private SessionService sessionService;

    public SummaryBieInfo getSummaryBieInfo(AuthenticatedPrincipal user, BigInteger releaseId) {
        AppUser requester = sessionService.getAppUserByUsername(user);
        if (user == null || requester == null) {
            throw new DataAccessForbiddenException("Need authentication to access information.");
        }

        List<SummaryBie> summaryBieList = repository.getSummaryBieList(releaseId, requester);

        SummaryBieInfo info = new SummaryBieInfo();
        Map<BieState, Integer> numberOfTotalBieByStates =
                summaryBieList.stream().collect(Collectors.toMap(SummaryBie::getState, (e) -> 1, Integer::sum));
        info.setNumberOfTotalBieByStates(numberOfTotalBieByStates);

        Map<BieState, Integer> numberOfMyBieByStates =
                summaryBieList.stream()
                        .filter(e -> e.getOwnerUserId().equals(requester.getAppUserId()))
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
        request.setOwnerLoginIds(Arrays.asList(String.valueOf(requester.getLoginId())));
        request.setPageRequest(pageRequest);
        if (releaseId != null) {
            request.setReleaseIds(Arrays.asList(releaseId));
        }

        info.setMyRecentBIEs(bieService.getBieList(user, request).getList());

        return info;
    }
}
