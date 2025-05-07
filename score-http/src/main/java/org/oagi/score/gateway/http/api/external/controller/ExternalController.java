package org.oagi.score.gateway.http.api.external.controller;

import io.swagger.v3.oas.annotations.Parameter;
import org.jooq.exception.IOException;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.BieListRequest;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.expression.BieGenerateExpressionResult;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.cc_management.model.CcList;
import org.oagi.score.gateway.http.api.cc_management.model.CcListRequest;
import org.oagi.score.gateway.http.api.cc_management.model.CcListTypes;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.service.CcListService;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.external.data.BieList;
import org.oagi.score.gateway.http.api.external.service.ExternalService;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.AccessPrivilege;
import org.oagi.score.gateway.http.common.model.ExportStandaloneSchemaResponse;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.common.util.DeleteOnCloseFileSystemResource;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;
import static org.springframework.util.StringUtils.hasLength;

@RestController
public class ExternalController {

    @Autowired
    private ExternalService service;

    @Autowired
    private CcListService ccService;

    @Autowired
    private SessionService sessionService;

    @RequestMapping(value = "/ext/core_component", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<CcList> getCcList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam(name = "releaseId") ReleaseId releaseId,
            @RequestParam(name = "den", required = false) String den,
            @RequestParam(name = "definition", required = false) String definition,
            @RequestParam(name = "module", required = false) String module,
            @RequestParam(name = "types", required = false) String types,
            @RequestParam(name = "states", required = false) String states,
            @RequestParam(name = "deprecated", required = false) String deprecated,
            @RequestParam(name = "newComponent", required = false) String newComponent,
            @RequestParam(name = "ownerLoginIdList", required = false) String ownerLoginIdList,
            @RequestParam(name = "updaterLoginIdList", required = false) String updaterLoginIdList,
            @RequestParam(name = "updateStart", required = false) String updateStart,
            @RequestParam(name = "updateEnd", required = false) String updateEnd,
            @RequestParam(name = "tags", required = false) String tags,
            @RequestParam(name = "namespaces", required = false) String namespaces,
            @RequestParam(name = "componentTypes", required = false) String componentTypes,
            @RequestParam(name = "dtTypes", required = false) String dtTypes,
            @RequestParam(name = "asccpTypes", required = false) String asccpTypes,
            @RequestParam(name = "excludes", required = false) String excludes,
            @RequestParam(name = "isBIEUsable", required = false) String isBIEUsable,
            @RequestParam(name = "commonlyUsed", required = false) String commonlyUsed,

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

        CcListRequest request = new CcListRequest();

        request.setReleaseId(releaseId);
        request.setTypes(CcListTypes.fromString(types));
        if (StringUtils.hasLength(states)) {
            List<String> stateStrings = new ArrayList<>(Arrays.asList(states.split(",")));
            request.setStates(stateStrings.stream()
                    .map(e -> CcState.valueOf(e.trim())).collect(Collectors.toList()));
        }
        if (StringUtils.hasLength(deprecated)) {
            if ("true".equalsIgnoreCase(deprecated.toLowerCase())) {
                request.setDeprecated(true);
            } else if ("false".equalsIgnoreCase(deprecated.toLowerCase())) {
                request.setDeprecated(false);
            }
        }
        if (StringUtils.hasLength(newComponent)) {
            if ("true".equalsIgnoreCase(newComponent.toLowerCase())) {
                request.setNewComponent(true);
            } else if ("false".equalsIgnoreCase(newComponent.toLowerCase())) {
                request.setNewComponent(false);
            }
        }
        if (StringUtils.hasLength(isBIEUsable)) {
            if ("true".equalsIgnoreCase(isBIEUsable.toLowerCase())) {
                request.setIsBIEUsable(true);
            } else if ("false".equalsIgnoreCase(isBIEUsable.toLowerCase())) {
                request.setIsBIEUsable(false);
            }
        }
        if (StringUtils.hasLength(commonlyUsed)) {
            if ("true".equalsIgnoreCase(commonlyUsed.toLowerCase())) {
                request.setCommonlyUsed(true);
            } else if ("false".equalsIgnoreCase(commonlyUsed.toLowerCase())) {
                request.setCommonlyUsed(false);
            }
        }
        request.setOwnerLoginIdList(!StringUtils.hasLength(ownerLoginIdList) ? Collections.emptyList()
                : Arrays.asList(ownerLoginIdList.split(",")).stream().map(e -> e.trim())
                        .filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList()));
        request.setUpdaterLoginIdList(!StringUtils.hasLength(updaterLoginIdList) ? Collections.emptyList()
                : Arrays.asList(updaterLoginIdList.split(",")).stream().map(e -> e.trim())
                        .filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList()));
        request.setDen(den);
        request.setDefinition(definition);
        request.setModule(module);
        request.setTags(!StringUtils.hasLength(tags) ? Collections.emptyList()
                : Arrays.asList(tags.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e))
                        .collect(Collectors.toList()));
        request.setNamespaces(!StringUtils.hasLength(namespaces) ? Collections.emptyList()
                : Arrays.asList(namespaces.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e))
                        .map(e -> new BigInteger(e)).collect(Collectors.toList()));
        request.setComponentTypes(componentTypes);
        request.setDtTypes(!StringUtils.hasLength(dtTypes) ? Collections.emptyList()
                : Arrays.asList(dtTypes.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e))
                        .collect(Collectors.toList()));
        if ((request.getTypes().isCdt() || request.getTypes().isBdt()) && request.getDtTypes().isEmpty()) {
            List<String> dtTypeList = new ArrayList<String>();
            if (request.getTypes().isCdt()) {
                dtTypeList.add("CDT");
            }
            if (request.getTypes().isBdt()) {
                dtTypeList.add("BDT");
            }
            request.setDtTypes(dtTypeList);
        }

        request.setAsccpTypes(!StringUtils.hasLength(asccpTypes) ? Collections.emptyList()
                : Arrays.asList(asccpTypes.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e))
                        .collect(Collectors.toList()));
        request.setExcludes(!StringUtils.hasLength(excludes) ? Collections.emptyList()
                : Arrays.asList(excludes.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e))
                        .collect(Collectors.toList()));

        if (StringUtils.hasLength(updateStart)) {
            request.setUpdateStartDate(new Date(Long.valueOf(updateStart)));
        }
        if (StringUtils.hasLength(updateEnd)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf(updateEnd));
            calendar.add(Calendar.DATE, 1);
            request.setUpdateEndDate(calendar.getTime());
        }

        PageRequest pageRequest =
                (hasLength(orderBy)) ? pageRequest(pageIndex, pageSize, orderBy) :
                        pageRequest(pageIndex, pageSize, sortActive, sortDirection);
        request.setPageRequest(pageRequest);

        return ccService.getCcListWithLastUpdatedAndSince(sessionService.asScoreUser(user), request);
    }

    /*
     * @RequestMapping(value =
     * "/ext/core_component/changes_in_release/{releaseId:[\\d]+}", method =
     * RequestMethod.GET,
     * produces = MediaType.APPLICATION_JSON_VALUE)
     * public CcChangesResponse getCcChanges(
     * 
     * @AuthenticationPrincipal AuthenticatedPrincipal user,
     * 
     * @PathVariable("releaseId") ReleaseId releaseId) {
     * return service.getCcChanges(sessionService.asScoreUser(user), releaseId);
     * }
     */

    @RequestMapping(value = "/ext/core_component/export/standalone", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DeleteOnCloseFileSystemResource> exportStandaloneSchema(
            @RequestParam(name = "asccpManifestIdList", required = true) String asccpManifestIdList) throws Exception {

        ExportStandaloneSchemaResponse response = service.exportStandaloneSchema(
                sessionService.getScoreSystemUser(),
                Arrays.stream(asccpManifestIdList.split(",")).map(e -> new AsccpManifestId(new BigInteger(e))).collect(Collectors.toList()));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.filename() + "\"")
                .contentType(MediaType.parseMediaType(
                        (response.filename().endsWith(".zip") ? "application/zip" : "application/xml")))
                .contentLength(response.file().length())
                .body(new DeleteOnCloseFileSystemResource(response.file()));
    }

    @RequestMapping(value = "/ext/releases", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getReleases() {
        return service.getReleases();
    }

    @RequestMapping(value = "/ext/latest_release", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getLatestRelease() {
        return service.getLatestRelease();
    }

    @RequestMapping(value = "/ext/auth", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> auth() {
        return ResponseEntity.ok("Authenticated");
    }


    @RequestMapping(value = "/ext/bie_list",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<BieList> getBieList(
            @RequestParam(name = "den", required = false) String den,
            @RequestParam(name = "propertyTerm", required = false) String propertyTerm,
            @RequestParam(name = "businessContext", required = false) String businessContext,
            @RequestParam(name = "asccpManifestId", required = false) AsccpManifestId asccpManifestId,
            @RequestParam(name = "access", required = false) String access,
            @RequestParam(name = "states", required = false) String states,
            @RequestParam(name = "excludePropertyTerms", required = false) String excludePropertyTerms,
            @RequestParam(name = "topLevelAsbiepIds", required = false) String topLevelAsbiepIds,
            @RequestParam(name = "excludeTopLevelAsbiepIds", required = false) String excludeTopLevelAsbiepIds,
            @RequestParam(name = "ownerLoginIdList", required = false) String ownerLoginIdList,
            @RequestParam(name = "updaterLoginIdList", required = false) String updaterLoginIdList,
            @RequestParam(name = "updateStart", required = false) String updateStart,
            @RequestParam(name = "updateEnd", required = false) String updateEnd,
            @RequestParam(name = "ownedByDeveloper", required = false) Boolean ownedByDeveloper,
            @RequestParam(name = "releaseIds", required = false) String releaseIds,

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

        BieListRequest request = new BieListRequest();

        request.setDen(den);
        request.setPropertyTerm(propertyTerm);
        request.setBusinessContext(businessContext);
        request.setAsccpManifestId(asccpManifestId);
        request.setAccess(StringUtils.hasLength(access) ? AccessPrivilege.valueOf(access) : null);
        request.setStates(StringUtils.hasLength(states) ?
                Arrays.asList(states.split(",")).stream()
                        .map(e -> BieState.valueOf(e)).collect(Collectors.toList()) : Collections.emptyList());
        request.setExcludePropertyTerms(!StringUtils.hasLength(excludePropertyTerms) ? Collections.emptyList() :
                Arrays.asList(excludePropertyTerms.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList()));
        request.setTopLevelAsbiepIds(!StringUtils.hasLength(topLevelAsbiepIds) ? Collections.emptyList() :
                Arrays.asList(topLevelAsbiepIds.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).map(e -> TopLevelAsbiepId.from(e)).collect(Collectors.toList()));
        request.setExcludeTopLevelAsbiepIds(!StringUtils.hasLength(excludeTopLevelAsbiepIds) ? Collections.emptyList() :
                Arrays.asList(excludeTopLevelAsbiepIds.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).map(e -> TopLevelAsbiepId.from(e)).collect(Collectors.toList()));
        request.setOwnerLoginIdList(!StringUtils.hasLength(ownerLoginIdList) ? Collections.emptyList() :
                Arrays.asList(ownerLoginIdList.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList()));
        request.setUpdaterLoginIdList(!StringUtils.hasLength(updaterLoginIdList) ? Collections.emptyList() :
                Arrays.asList(updaterLoginIdList.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList()));
        request.setOwnedByDeveloper(ownedByDeveloper);
        request.setReleaseIds(!StringUtils.hasLength(releaseIds) ? Collections.emptyList() :
                Arrays.asList(releaseIds.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).map(e -> ReleaseId.from(e)).collect(Collectors.toList()));

        if (StringUtils.hasLength(updateStart)) {
            request.setUpdateStartDate(new Date(Long.valueOf(updateStart)));
        }
        if (StringUtils.hasLength(updateEnd)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf(updateEnd));
            calendar.add(Calendar.DATE, 1);
            request.setUpdateEndDate(calendar.getTime());
        }

        PageRequest pageRequest =
                (hasLength(orderBy)) ? pageRequest(pageIndex, pageSize, orderBy) :
                        pageRequest(pageIndex, pageSize, sortActive, sortDirection);
        request.setPageRequest(pageRequest);

        return service.getBieList(request);
    }


    @RequestMapping(value = "/ext/bie/generate", method = RequestMethod.GET) 
    public ResponseEntity<InputStreamResource> generate(
                                                        @RequestParam(name="bizCtxIds", required=false) String bizCtxIds,
                                                        @RequestParam(name="topLevelAsbiepId", required=true) TopLevelAsbiepId topLevelAsbiepId
                                                        ) 
                                                        throws IOException, FileNotFoundException {

        Map<TopLevelAsbiepId, BusinessContextId> bizCtxMap = new HashMap<>();
        if (null != bizCtxIds) {
            for (String bizCtxIdStr : bizCtxIds.split(",")) {
                BusinessContextId bizCtxId = BusinessContextId.from(bizCtxIdStr);
                bizCtxMap.put(topLevelAsbiepId, bizCtxId);
            }
        }

        GenerateExpressionOption option = new GenerateExpressionOption();
        option.setBizCtxIds(bizCtxMap);
        option.setPackageOption("EACH");
        option.setExpressionOption("XML");

        BieGenerateExpressionResult bieGenerateExpressionResult = service.generate(
                sessionService.getScoreSystemUser(), topLevelAsbiepId, option);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + bieGenerateExpressionResult.filename() + "\"")
                .contentType(MediaType.parseMediaType(bieGenerateExpressionResult.contentType()))
                .contentLength(bieGenerateExpressionResult.file().length())
                .body(new InputStreamResource(new FileInputStream(bieGenerateExpressionResult.file())));
    }
    


}
