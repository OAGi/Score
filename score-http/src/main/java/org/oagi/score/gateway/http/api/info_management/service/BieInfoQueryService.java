package org.oagi.score.gateway.http.api.info_management.service;

import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.bie_management.model.BieListEntryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BieListFilterCriteria;
import org.oagi.score.gateway.http.api.bie_management.service.BieQueryService;
import org.oagi.score.gateway.http.api.info_management.model.SummaryBie;
import org.oagi.score.gateway.http.api.info_management.model.SummaryBieInfoRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.tenant_management.service.TenantQueryService;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;

@Service
@Transactional(readOnly = true)
public class BieInfoQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private BieQueryService bieQueryService;

    @Autowired
    private ApplicationConfigurationService configService;

    @Autowired
    private TenantQueryService tenantService;

    public SummaryBieInfoRecord getSummaryBieInfo(ScoreUser requester, LibraryId libraryId, ReleaseId releaseId) {
        if (requester == null) {
            throw new DataAccessForbiddenException("Need authentication to access information.");
        }

        var query = repositoryFactory.bieQueryRepository(requester);
        boolean tenantEnabled = configService.isTenantEnabled(requester);
        List<SummaryBie> summaryBieList = query.getSummaryBieList(
                libraryId, releaseId, tenantEnabled,
                (tenantEnabled && !requester.isAdministrator()) ?
                        tenantService.getUserTenantsRoleByUser(requester) : Collections.emptyList());

        Map<BieState, Integer> numberOfTotalBieByStates =
                summaryBieList.stream().collect(Collectors.toMap(SummaryBie::getState, (e) -> 1, Integer::sum));

        Map<BieState, Integer> numberOfMyBieByStates =
                summaryBieList.stream()
                        .filter(e -> e.getOwnerUserId().equals(requester.userId()))
                        .collect(Collectors.toMap(SummaryBie::getState, (e) -> 1, Integer::sum));

        Map<String, Map<BieState, Integer>> bieByUsersAndStates = summaryBieList.stream()
                .collect(groupingBy(SummaryBie::getOwnerUsername,
                        Collectors.toMap(SummaryBie::getState, (e) -> 1, Integer::sum)));

        List<BieListEntryRecord> myRecentBIEs = bieQueryService.getBieList(requester, BieListFilterCriteria
                .builder(libraryId, (releaseId != null) ? Arrays.asList(releaseId) : Collections.emptyList())
                .ownerLoginIdList(Arrays.asList(String.valueOf(requester.username())))
                .build(), pageRequest(0, 5, "-lastUpdateTimestamp")).result();

        return new SummaryBieInfoRecord(
                numberOfTotalBieByStates, numberOfMyBieByStates, bieByUsersAndStates, myRecentBIEs);
    }
}
