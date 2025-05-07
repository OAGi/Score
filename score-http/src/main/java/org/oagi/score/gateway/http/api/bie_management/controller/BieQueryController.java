package org.oagi.score.gateway.http.api.bie_management.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.bie_management.model.BieListEntryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BieListFilterCriteria;
import org.oagi.score.gateway.http.api.bie_management.service.BieQueryService;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.plantuml.service.PlantUmlService;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.tenant_management.service.TenantQueryService;
import org.oagi.score.gateway.http.common.model.*;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static java.util.stream.Collectors.toSet;
import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;
import static org.oagi.score.gateway.http.common.util.Utility.separate;
import static org.springframework.util.StringUtils.hasLength;

@RestController
@Tag(name = "Business Information Entity - Queries", description = "API for retrieving business information entity-related data")
@RequestMapping("/bies")
public class BieQueryController {

    @Autowired
    private BieQueryService bieQueryService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ApplicationConfigurationService configService;

    @Autowired
    private TenantQueryService tenantService;

    @Autowired
    private PlantUmlService plantUmlService;

    @GetMapping()
    public PageResponse<BieListEntryRecord> getBieList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam(name = "libraryId") LibraryId libraryId,
            @RequestParam(name = "releaseIds", required = false) String releaseIds,
            @RequestParam(name = "den", required = false) String den,
            @RequestParam(name = "propertyTerm", required = false) String propertyTerm,
            @RequestParam(name = "businessContext", required = false) String businessContext,
            @RequestParam(name = "version", required = false) String version,
            @RequestParam(name = "remark", required = false) String remark,
            @RequestParam(name = "asccpManifestId", required = false) AsccpManifestId asccpManifestId,
            @RequestParam(name = "access", required = false) String access,
            @RequestParam(name = "states", required = false) String states,
            @RequestParam(name = "topLevelAsbiepIds", required = false) String topLevelAsbiepIds,
            @RequestParam(name = "basedTopLevelAsbiepIds", required = false) String basedTopLevelAsbiepIds,
            @RequestParam(name = "excludeTopLevelAsbiepIds", required = false) String excludeTopLevelAsbiepIds,
            @RequestParam(name = "excludePropertyTerms", required = false) String excludePropertyTerms,
            @RequestParam(name = "deprecated", required = false) String deprecated,
            @RequestParam(name = "ownedByDeveloper", required = false) String ownedByDeveloper,

            @RequestParam(name = "ownerLoginIdList", required = false)
            @Parameter(description = "Comma-separated list of login IDs of owners to filter the results.")
            String ownerLoginIdList,

            @RequestParam(name = "updaterLoginIdList", required = false)
            @Parameter(description = "Comma-separated list of login IDs of updaters to filter the results.")
            String updaterLoginIdList,

            @RequestParam(name = "updateStart", required = false)
            @Parameter(deprecated = true,
                    description = "Deprecated. Replaced by 'lastUpdatedOn'. " +
                            "Filter results to include only releases updated after this timestamp (milliseconds since epoch).")
            String updateStart,

            @RequestParam(name = "updateEnd", required = false)
            @Parameter(deprecated = true,
                    description = "Deprecated. Replaced by 'lastUpdatedOn'. " +
                            "Filter results to include only releases updated before this timestamp (milliseconds since epoch).")
            String updateEnd,

            @RequestParam(name = "lastUpdatedOn", required = false)
            @Parameter(description = "Filter results by last update timestamp range in epoch milliseconds. " +
                    "Format: `[after~before]`. Use `after` to specify the lower bound and `before` for the upper bound.")
            String lastUpdatedOn,

            @RequestParam(name = "sortActive", required = false)
            @Parameter(deprecated = true,
                    description = "Deprecated. Replaced by 'orderBy'. Previously used to specify the active sorting property.")
            String sortActive,

            @RequestParam(name = "sortDirection", required = false)
            @Parameter(deprecated = true,
                    description = "Deprecated. Replaced by 'orderBy'. Previously used to specify sorting direction (ASC/DESC).")
            String sortDirection,

            @RequestParam(name = "orderBy", required = false)
            @Parameter(description = "Sorting criteria for the results. " +
                    "Supports multiple comma-separated properties with an optional '+' (ascending) or '-' (descending) prefix. " +
                    "If no prefix is specified, ascending order is applied by default. " +
                    "Example: `-releaseNum,+lastUpdateTimestamp,state` is equivalent to `releaseNum desc, lastUpdateTimestamp asc, state asc`.")
            String orderBy,

            @RequestParam(name = "pageIndex", required = false)
            @Parameter(description = "Index of the page to retrieve (zero-based). " +
                    "If a negative value is provided, pagination is ignored and all results are returned.")
            Integer pageIndex,

            @RequestParam(name = "pageSize", required = false)
            @Parameter(description = "Number of records per page. " +
                    "If a negative value is provided, pagination is ignored and all results are returned.")
            Integer pageSize) {

        ScoreUser requester = sessionService.asScoreUser(user);
        boolean tenantEnabled = configService.isTenantEnabled(requester);
        BieListFilterCriteria filterCriteria = new BieListFilterCriteria(
                libraryId,
                separate(releaseIds).map(e -> ReleaseId.from(e)).collect(toSet()),
                den, propertyTerm,
                separate(businessContext).collect(toSet()),
                version, remark, asccpManifestId,
                (hasLength(access)) ? AccessPrivilege.valueOf(access) : null,
                separate(states).map(e -> BieState.valueOf(e)).collect(toSet()),
                separate(excludePropertyTerms).collect(toSet()),

                separate(topLevelAsbiepIds).map(e -> TopLevelAsbiepId.from(e)).collect(toSet()),
                separate(basedTopLevelAsbiepIds).map(e -> TopLevelAsbiepId.from(e)).collect(toSet()),
                separate(excludeTopLevelAsbiepIds).map(e -> TopLevelAsbiepId.from(e)).collect(toSet()),

                hasLength(deprecated) ? ("true".equalsIgnoreCase(deprecated) ? true : false) : null,
                hasLength(ownedByDeveloper) ? ("true".equalsIgnoreCase(ownedByDeveloper) ? true : false) : null,
                tenantEnabled,
                (tenantEnabled && !requester.isAdministrator()) ?
                        tenantService.getUserTenantsRoleByUser(requester) : Collections.emptyList(),

                separate(ownerLoginIdList).collect(toSet()),
                separate(updaterLoginIdList).collect(toSet()),
                (hasLength(lastUpdatedOn)) ?
                        DateRangeCriteria.create(lastUpdatedOn) :
                        (hasLength(updateStart) || hasLength(updateEnd)) ?
                                DateRangeCriteria.create(
                                        hasLength(updateStart) ? Long.valueOf(updateStart) : null,
                                        hasLength(updateEnd) ? Long.valueOf(updateEnd) : null) : null
        );

        PageRequest pageRequest =
                (hasLength(orderBy)) ? pageRequest(pageIndex, pageSize, orderBy) :
                        pageRequest(pageIndex, pageSize, sortActive, sortDirection);

        var resultAndCount = bieQueryService.getBieList(sessionService.asScoreUser(user), filterCriteria, pageRequest);

        PageResponse<BieListEntryRecord> response = new PageResponse<>();
        response.setList(resultAndCount.result());
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(resultAndCount.count());
        return response;
    }

    @GetMapping(value = "/{topLevelAsbiepId:[\\d]+}/plantuml")
    public Map<String, String> generatePlantUml(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId,
            @RequestParam(value = "topLevelAsbiepLinkTemplate", defaultValue = "/profile_bie/{topLevelAsbiepId}") String topLevelAsbiepLinkTemplate) throws IOException {

        String text = bieQueryService.generatePlantUmlText(sessionService.asScoreUser(user),
                topLevelAsbiepId, topLevelAsbiepLinkTemplate);

        return Map.of("text", text,
                "encodedText", plantUmlService.getEncodedText(text));
    }

}
