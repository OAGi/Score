package org.oagi.score.gateway.http.api.code_list_management.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListDetailsRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListListEntryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.repository.criteria.CodeListListFilterCriteria;
import org.oagi.score.gateway.http.api.code_list_management.service.CodeListQueryService;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.AccessPrivilege;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.stream.Collectors.toSet;
import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;
import static org.oagi.score.gateway.http.common.util.Utility.separate;

@RestController
@Tag(name = "Code List - Queries", description = "API for retrieving code list-related data")
@RequestMapping("/code-lists")
public class CodeListQueryController {

    @Autowired
    private CodeListQueryService codeListQueryService;

    @Autowired
    private SessionService sessionService;

    @GetMapping(value = "/summaries")
    public List<CodeListSummaryRecord> getCodeListSummaryList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the release.")
            @RequestParam(name = "releaseId") ReleaseId releaseId) {

        return codeListQueryService.getCodeListSummaryList(sessionService.asScoreUser(user), releaseId);
    }

    @GetMapping(value = "/{codeListManifestId:[\\d]+}")
    public CodeListDetailsRecord getCodeListDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @PathVariable("codeListManifestId")
            CodeListManifestId codeListManifestId) {

        return codeListQueryService.getCodeListDetails(
                sessionService.asScoreUser(user), codeListManifestId);
    }

    @GetMapping(value = "/{codeListManifestId:[\\d]+}/prev")
    public CodeListDetailsRecord getPrevCodeListDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @PathVariable("codeListManifestId")
            CodeListManifestId codeListManifestId) {

        return codeListQueryService.getPrevCodeListDetails(
                sessionService.asScoreUser(user), codeListManifestId);
    }

    @GetMapping()
    public PageResponse<CodeListListEntryRecord> getCodeListList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam(name = "libraryId") LibraryId libraryId,
            @RequestParam(name = "releaseId") ReleaseId releaseId,

            @RequestParam(name = "access", required = false)
            String access,

            @RequestParam(name = "states", required = false)
            String states,

            @RequestParam(name = "name", required = false)
            String name,

            @RequestParam(name = "definition", required = false)
            String definition,

            @RequestParam(name = "module", required = false)
            String module,

            @RequestParam(name = "deprecated", required = false)
            String deprecated,

            @RequestParam(name = "extensible", required = false)
            String extensible,

            @RequestParam(name = "ownedByDeveloper", required = false)
            String ownedByDeveloper,

            @RequestParam(name = "newComponent", required = false)
            String newComponent,

            @RequestParam(name = "namespaces", required = false)
            String namespaces,

            @RequestParam(name = "ownerLoginIdList", required = false)
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

            @RequestParam(name = "pageIndex", required = false, defaultValue = "0")
            @Parameter(description = "Index of the page to retrieve (zero-based). " +
                    "If a negative value is provided, pagination is ignored and all results are returned.")
            Integer pageIndex,

            @RequestParam(name = "pageSize", required = false, defaultValue = "10")
            @Parameter(description = "Number of records per page. " +
                    "If a negative value is provided, pagination is ignored and all results are returned.")
            Integer pageSize) {


        CodeListListFilterCriteria filterCriteria = new CodeListListFilterCriteria(
                libraryId, releaseId,
                name, definition, module,
                (hasLength(deprecated)) ? "true".equals(deprecated) : null,
                (hasLength(extensible)) ? "true".equals(extensible) : null,
                (hasLength(ownedByDeveloper)) ? "true".equals(ownedByDeveloper) : null,
                (hasLength(newComponent)) ? "true".equals(newComponent) : null,
                (hasLength(access)) ? AccessPrivilege.valueOf(access) : null,
                separate(states).map(e -> CcState.valueOf(e)).collect(toSet()),
                separate(namespaces).map(e -> NamespaceId.from(e)).collect(toSet()),
                separate(ownerLoginIdList).collect(toSet()),
                separate(updaterLoginIdList).collect(toSet()),
                (StringUtils.hasLength(lastUpdatedOn)) ?
                        DateRangeCriteria.create(lastUpdatedOn) :
                        (StringUtils.hasLength(updateStart) || StringUtils.hasLength(updateEnd)) ?
                                DateRangeCriteria.create(
                                        StringUtils.hasLength(updateStart) ? Long.valueOf(updateStart) : null,
                                        StringUtils.hasLength(updateEnd) ? Long.valueOf(updateEnd) : null) : null
        );

        PageRequest pageRequest =
                (StringUtils.hasLength(orderBy)) ? pageRequest(pageIndex, pageSize, orderBy) :
                        pageRequest(pageIndex, pageSize, sortActive, sortDirection);

        var resultAndCount = codeListQueryService.getCodeListList(sessionService.asScoreUser(user), filterCriteria, pageRequest);

        PageResponse<CodeListListEntryRecord> response = new PageResponse<>();
        response.setList(resultAndCount.result());
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(resultAndCount.count());
        return response;
    }

    @GetMapping(value = "/check-uniqueness")
    public boolean checkUniqueness(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @RequestParam(name = "releaseId") ReleaseId releaseId,

            @RequestParam(name = "codeListManifestId", required = false)
            CodeListManifestId codeListManifestId,

            @RequestParam(name = "listId")
            String listId,

            @RequestParam(name = "agencyIdListValueManifestId", required = false)
            AgencyIdListValueManifestId agencyIdListValueManifestId,

            @RequestParam(name = "versionId")
            String versionId) {

        return codeListQueryService.hasSameCodeList(sessionService.asScoreUser(user),
                releaseId, codeListManifestId, agencyIdListValueManifestId, listId, versionId);
    }

    @GetMapping(value = "/check-name-uniqueness")
    public boolean checkNameUniqueness(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @RequestParam(name = "releaseId") ReleaseId releaseId,

            @RequestParam(name = "codeListManifestId", required = false)
            CodeListManifestId codeListManifestId,

            @RequestParam(name = "codeListName") String codeListName) {

        return codeListQueryService.hasSameNameCodeList(sessionService.asScoreUser(user),
                releaseId, codeListManifestId, codeListName);
    }

}
