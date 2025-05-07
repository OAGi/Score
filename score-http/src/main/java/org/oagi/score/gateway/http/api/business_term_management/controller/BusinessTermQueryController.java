package org.oagi.score.gateway.http.api.business_term_management.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.business_term_management.model.*;
import org.oagi.score.gateway.http.api.business_term_management.repository.criteria.AsbieBbieListFilterCriteria;
import org.oagi.score.gateway.http.api.business_term_management.repository.criteria.AssignedBusinessTermListFilterCriteria;
import org.oagi.score.gateway.http.api.business_term_management.repository.criteria.BusinessTermListFilterCriteria;
import org.oagi.score.gateway.http.api.business_term_management.service.BusinessTermQueryService;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.AccessPrivilege;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toSet;
import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;
import static org.oagi.score.gateway.http.common.util.Utility.separate;
import static org.springframework.util.StringUtils.hasLength;

@RestController
@Tag(name = "Business Term - Queries", description = "API for retrieving business term-related data")
@RequestMapping("/business-terms")
public class BusinessTermQueryController {

    @Autowired
    private BusinessTermQueryService businessTermQueryService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ResourceLoader resourceLoader;

    @GetMapping()
    public PageResponse<BusinessTermListEntryRecord> getBusinessTermList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @RequestParam(name = "businessTerm", required = false) String term,
            @RequestParam(name = "externalReferenceUri", required = false) String externalReferenceUri,
            @RequestParam(name = "externalReferenceId", required = false) String externalReferenceId,
            @RequestParam(name = "definition", required = false) String definition,
            @RequestParam(name = "comment", required = false) String comment,

            @RequestParam(name = "bieTypeList", required = false) String bieTypeList,
            @RequestParam(name = "searchByCC", required = false) Boolean searchByCC,
            @RequestParam(name = "byAssignedAsbieIdList", required = false) String byAssignedAsbieIdList,
            @RequestParam(name = "byAssignedBbieIdList", required = false) String byAssignedBbieIdList,

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

        BusinessTermListFilterCriteria filterCriteria = new BusinessTermListFilterCriteria(
                term, externalReferenceUri, externalReferenceId,
                definition, comment,

                separate(bieTypeList).collect(toSet()),
                searchByCC,
                separate(byAssignedAsbieIdList).map(e -> AsbieId.from(e)).collect(toSet()),
                separate(byAssignedBbieIdList).map(e -> BbieId.from(e)).collect(toSet()),

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

        var resultAndCount = businessTermQueryService.getBusinessTermList(sessionService.asScoreUser(user), filterCriteria, pageRequest);

        PageResponse<BusinessTermListEntryRecord> response = new PageResponse<>();
        response.setList(resultAndCount.result());
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(resultAndCount.count());
        return response;
    }

    @GetMapping(value = "/check-uniqueness")
    public boolean checkUniqueness(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam(name = "businessTermId", required = false) BusinessTermId businessTermId,
            @RequestParam(name = "businessTerm", required = false) String businessTerm,
            @RequestParam(name = "externalReferenceUri", required = false) String externalReferenceUri) {

        return businessTermQueryService.checkUniqueness(sessionService.asScoreUser(user),
                businessTermId, businessTerm, externalReferenceUri);
    }

    @GetMapping(value = "/check-name-uniqueness")
    public boolean checkNameUniqueness(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam(name = "businessTermId", required = false) BusinessTermId businessTermId,
            @RequestParam(name = "businessTerm", required = false) String businessTerm) {

        return businessTermQueryService.checkNameUniqueness(sessionService.asScoreUser(user),
                businessTermId, businessTerm);
    }

    @GetMapping(value = "/{businessTermId:[\\d]+}")
    public BusinessTermDetailsRecord getBusinessTermDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("businessTermId") BusinessTermId businessTermId) {

        return businessTermQueryService.getBusinessTermDetails(sessionService.asScoreUser(user), businessTermId);
    }

    @GetMapping(value = "/assign")
    public PageResponse<AssignedBusinessTermListEntryRecord> getAssignedBusinessTermList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam(name = "bieId", required = false) BigInteger bieId,
            @RequestParam(name = "bieDen", required = false) String bieDen,
            @RequestParam(name = "bieTypeList", required = false) String bieTypeList,
            @RequestParam(name = "businessTerm", required = false) String businessTerm,
            @RequestParam(name = "externalReferenceUri", required = false) String externalReferenceUri,
            @RequestParam(name = "typeCode", required = false) String typeCode,
            @RequestParam(name = "primaryIndicator", required = false) Boolean primaryIndicator,

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

        AssignedBusinessTermListFilterCriteria filterCriteria = new AssignedBusinessTermListFilterCriteria(
                null,
                bieId,
                separate(bieTypeList).collect(toSet()),
                bieDen,
                primaryIndicator,
                typeCode,
                businessTerm,
                externalReferenceUri,

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

        var resultAndCount = businessTermQueryService.getAssignedBusinessTermList(
                sessionService.asScoreUser(user), filterCriteria, pageRequest);

        PageResponse<AssignedBusinessTermListEntryRecord> response = new PageResponse<>();
        response.setList(resultAndCount.result());
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(resultAndCount.count());
        return response;
    }

    @GetMapping(value = "/assign/check-uniqueness")
    public boolean checkAssignmentUniqueness(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam(name = "asbieId", required = false) AsbieId asbieId,
            @RequestParam(name = "bbieId", required = false) BbieId bbieId,
            @RequestParam(name = "businessTermId", required = false) BusinessTermId businessTermId,
            @RequestParam(name = "typeCode", required = false) String typeCode,
            @RequestParam(name = "primaryIndicator", required = false) Boolean primaryIndicator) {

        if (asbieId != null) {
            return businessTermQueryService.checkAssignmentUniqueness(sessionService.asScoreUser(user),
                    asbieId, businessTermId, typeCode, (primaryIndicator != null) ? primaryIndicator : false);
        } else if (bbieId != null) {
            return businessTermQueryService.checkAssignmentUniqueness(sessionService.asScoreUser(user),
                    bbieId, businessTermId, typeCode, (primaryIndicator != null) ? primaryIndicator : false);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @RequestMapping(value = "/assign/{type}/{id:[\\d]+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AssignedBusinessTermDetailsRecord getAssignedBusinessTermDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("type") String bieType,
            @PathVariable("id") BigInteger assignedBizTermId) {

        bieType = bieType.toUpperCase();
        if ("ASBIE".equals(bieType)) {
            return businessTermQueryService.getAssignedBusinessTermDetails(
                    sessionService.asScoreUser(user), new AsbieBusinessTermId(assignedBizTermId));
        } else if ("BBIE".equals(bieType)) {
            return businessTermQueryService.getAssignedBusinessTermDetails(
                    sessionService.asScoreUser(user), new BbieBusinessTermId(assignedBizTermId));
        } else {
            throw new IllegalArgumentException();
        }
    }

    @GetMapping(value = "/asbie-bbie")
    public PageResponse<AsbieBbieListEntryRecord> getAsbieAndBbieList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam(name = "libraryId") LibraryId libraryId,
            @RequestParam(name = "releaseIds", required = false) String releaseIds,
            @RequestParam(name = "types", required = false) String types,
            @RequestParam(name = "topLevelAsccpPropertyTerm", required = false) String propertyTerm,
            @RequestParam(name = "businessContext", required = false) String businessContext,
            @RequestParam(name = "version", required = false) String version,
            @RequestParam(name = "remark", required = false) String remark,
            @RequestParam(name = "den", required = false) String den,
            @RequestParam(name = "access", required = false) String access,
            @RequestParam(name = "states", required = false) String states,
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

        AsbieBbieListFilterCriteria filterCriteria = new AsbieBbieListFilterCriteria(
                libraryId,
                separate(releaseIds).map(e -> ReleaseId.from(e)).collect(toSet()),

                separate(types).collect(toSet()),
                Collections.emptyList(), Collections.emptyList(),

                den, propertyTerm,
                separate(businessContext).collect(toSet()),
                version, remark,
                (hasLength(access)) ? AccessPrivilege.valueOf(access) : null,
                separate(states).map(e -> BieState.valueOf(e)).collect(toSet()),

                hasLength(deprecated) ? ("true".equalsIgnoreCase(deprecated) ? true : false) : null,
                hasLength(ownedByDeveloper) ? ("true".equalsIgnoreCase(ownedByDeveloper) ? true : false) : null,

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

        var resultAndCount = businessTermQueryService.getAsbieBbieList(sessionService.asScoreUser(user), filterCriteria, pageRequest);

        PageResponse<AsbieBbieListEntryRecord> response = new PageResponse<>();
        response.setList(resultAndCount.result());
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(resultAndCount.count());
        return response;
    }

    @GetMapping(value = "/asbie-bbie/confirm")
    public List<AsbieBbieListEntryRecord> getAsbieAndBbieList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @RequestParam(name = "asbieIdList", required = false)
            @Parameter(description = "Comma-separated list of login IDs of owners to filter the results.")
            String asbieIdList,

            @RequestParam(name = "bbieIdList", required = false)
            @Parameter(description = "Comma-separated list of login IDs of owners to filter the results.")
            String bbieIdList) {

        return businessTermQueryService.getAsbieBbieList(sessionService.asScoreUser(user),
                separate(asbieIdList).map(e -> AsbieId.from(e)).collect(toSet()),
                separate(bbieIdList).map(e -> BbieId.from(e)).collect(toSet()));
    }

    @GetMapping(value = "/csv/template")
    public ResponseEntity getTemplateFile(
            @AuthenticationPrincipal AuthenticatedPrincipal user) {

        String templateName = "businessTermTemplateWithExample.csv";
        Resource resource = resourceLoader.getResource("classpath:" + templateName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + templateName + "\"")
                .body(resource);
    }

}
