package org.oagi.score.gateway.http.api.cc_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.service.AgencyIdListQueryService;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.CcChangesResponse;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.CcRefactorValidationResponse;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.CcVerifyAppendResponse;
import org.oagi.score.gateway.http.api.cc_management.model.CcListEntryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcListTypes;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpType;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.repository.criteria.CcListFilterCriteria;
import org.oagi.score.gateway.http.api.cc_management.service.CcQueryService;
import org.oagi.score.gateway.http.api.cc_management.service.dsl.CcQueryInterpreter;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.service.CodeListQueryService;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.plantuml.service.PlantUmlService;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseQueryService;
import org.oagi.score.gateway.http.common.model.*;
import org.oagi.score.gateway.http.common.util.DeleteOnCloseFileSystemResource;
import org.oagi.score.gateway.http.common.util.StringUtils;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;
import static org.oagi.score.gateway.http.common.util.Utility.separate;
import static org.springframework.util.StringUtils.hasLength;

@RestController
@Tag(name = "Core Component - Queries", description = "API for retrieving core component-related data")
@RequestMapping("/core-components")
public class CcQueryController {

    @Autowired
    private CcQueryService ccQueryService;

    @Autowired
    private CcQueryInterpreter interpreter;

    @Autowired
    private CodeListQueryService codeListQueryService;

    @Autowired
    private AgencyIdListQueryService agencyIdListQueryService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ReleaseQueryService releaseQueryService;

    @Autowired
    private PlantUmlService plantUmlService;

    @Operation(
            summary = "Retrieve a paginated list of Core Components",
            description = "Returns a paginated list of Core Components based on the specified filter criteria. "
                    + "Supports various filtering options including library, release, name, module, type, and state."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of Core Component list"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping()
    public PageResponse<CcListEntryRecord> getCcList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "Library ID to filter Core Components.")
            @RequestParam(name = "libraryId") LibraryId libraryId,

            @Parameter(description = "Release ID to filter Core Components.")
            @RequestParam(name = "releaseId") ReleaseId releaseId,

            @Parameter(description = "DEN (Dictionary Entry Name) filter.", required = false)
            @RequestParam(name = "den", required = false) String den,

            @Parameter(description = "Definition filter.", required = false)
            @RequestParam(name = "definition", required = false) String definition,

            @Parameter(description = "Module name filter.", required = false)
            @RequestParam(name = "module", required = false) String module,

            @Parameter(description = "Component types filter (comma-separated). Example: `ACC,ASCCP`.", required = false)
            @RequestParam(name = "types", required = false) String types,

            @Parameter(description = "States filter (comma-separated). Example: `WIP,Published`.", required = false)
            @RequestParam(name = "states", required = false) String states,

            @Parameter(description = "Filter for reusable components (true/false).", required = false)
            @RequestParam(name = "reusable", required = false) String reusable,

            @Parameter(description = "Filter for deprecated components (true/false).", required = false)
            @RequestParam(name = "deprecated", required = false) String deprecated,

            @Parameter(description = "Filter for newly created components (true/false).", required = false)
            @RequestParam(name = "newComponent", required = false) String newComponent,

            @Parameter(description = "Filter by tags (comma-separated). Example: `Noun,BOD`.", required = false)
            @RequestParam(name = "tags", required = false) String tags,

            @Parameter(description = "Filter by namespaces (comma-separated).", required = false)
            @RequestParam(name = "namespaces", required = false) String namespaces,

            @Parameter(description = "Filter by component types (comma-separated). Example: `Base,Semantics`.", required = false)
            @RequestParam(name = "componentTypes", required = false) String componentTypes,

            @Parameter(description = "Filter by ASCCP types. Example: `Default,Extension`.", required = false)
            @RequestParam(name = "asccpTypes", required = false) String asccpTypes,

            @Parameter(description = "Exclude certain components.", required = false)
            @RequestParam(name = "excludes", required = false) String excludes,

            @Parameter(description = "Filter for BIE usability (true/false).", required = false)
            @RequestParam(name = "isBIEUsable", required = false) String isBIEUsable,

            @Parameter(description = "Filter by commonly used DT components (true/false).", required = false)
            @RequestParam(name = "commonlyUsed", required = false) String commonlyUsed,

            @Parameter(description = "Comma-separated list of owner login IDs to filter results.", required = false)
            @RequestParam(name = "ownerLoginIdList", required = false) String ownerLoginIdList,

            @Parameter(description = "Comma-separated list of updater login IDs to filter results.", required = false)
            @RequestParam(name = "updaterLoginIdList", required = false) String updaterLoginIdList,

            @Parameter(deprecated = true,
                    description = "Deprecated. Replaced by 'lastUpdatedOn'. Filter for updates after this timestamp (epoch ms).")
            @RequestParam(name = "updateStart", required = false) String updateStart,

            @Parameter(deprecated = true,
                    description = "Deprecated. Replaced by 'lastUpdatedOn'. Filter for updates before this timestamp (epoch ms).")
            @RequestParam(name = "updateEnd", required = false) String updateEnd,

            @Parameter(description = "Filter by last update timestamp range in epoch milliseconds. "
                    + "Format: `[after~before]`. Example: `1700000000000~1705000000000`.", required = false)
            @RequestParam(name = "lastUpdatedOn", required = false) String lastUpdatedOn,

            @Parameter(deprecated = true,
                    description = "Deprecated. Replaced by 'orderBy'. Previously used to specify the active sorting property.")
            @RequestParam(name = "sortActive", required = false) String sortActive,

            @Parameter(deprecated = true,
                    description = "Deprecated. Replaced by 'orderBy'. Previously used to specify sorting direction (ASC/DESC).")
            @RequestParam(name = "sortDirection", required = false) String sortDirection,

            @Parameter(description = "Sorting criteria for the results. Example: `-releaseNum,+lastUpdateTimestamp,state`.", required = false)
            @RequestParam(name = "orderBy", required = false) String orderBy,

            @Parameter(description = "Index of the page to retrieve (zero-based). "
                    + "If negative, pagination is ignored and all results are returned.", required = false)
            @RequestParam(name = "pageIndex", required = false) Integer pageIndex,

            @Parameter(description = "Number of records per page. "
                    + "If negative, pagination is ignored and all results are returned.", required = false)
            @RequestParam(name = "pageSize", required = false) Integer pageSize) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        ScoreUser requester = sessionService.asScoreUser(user);
        CcListFilterCriteria filterCriteria =
                CcListFilterCriteria.builder(releaseId)
                        .den(den)
                        .definition(definition)
                        .module(module)
                        .types(hasLength(types) ? CcListTypes.fromString(types) : CcListTypes.fromString("ACC,ASCCP,BCCP"))
                        .states(separate(states).map(e -> CcState.valueOf(e)).collect(Collectors.toSet()))
                        .tags(separate(tags).collect(toSet()))
                        .namespaceIds(separate(namespaces).map(e -> NamespaceId.from(e)).collect(Collectors.toSet()))
                        .componentTypes(separate(componentTypes).map(e -> OagisComponentType.valueOf(e)).collect(toSet()))
                        .asccpTypes(separate(asccpTypes).map(e -> AsccpType.valueOf(e)).collect(toSet()))
                        .asccpManifestIds(
                                (hasLength(den) && den.startsWith("AI:")) ?
                                        interpreter.interpret(requester, releaseId, den.substring(3).trim()) : Collections.emptyList()
                        )
                        .excludes(separate(excludes).map(e -> new BigInteger(e)).collect(toSet()))
                        .deprecated(hasLength(deprecated) ? ("true".equalsIgnoreCase(deprecated) ? true : false) : null)
                        .reusable(hasLength(reusable) ? ("true".equalsIgnoreCase(reusable) ? true : false) : null)
                        .commonlyUsed(hasLength(commonlyUsed) ? ("true".equalsIgnoreCase(commonlyUsed) ? true : false) : null)
                        .newComponent(hasLength(newComponent) ? ("true".equalsIgnoreCase(newComponent) ? true : false) : null)
                        .isBIEUsable(hasLength(isBIEUsable) ? ("true".equalsIgnoreCase(isBIEUsable) ? true : false) : null)
                        .ownerLoginIdList(separate(ownerLoginIdList).collect(toSet()))
                        .updaterLoginIdList(separate(updaterLoginIdList).collect(toSet()))
                        .lastUpdatedTimestampRange(
                                (hasLength(lastUpdatedOn)) ?
                                        DateRangeCriteria.create(lastUpdatedOn) :
                                        (hasLength(updateStart) || hasLength(updateEnd)) ?
                                                DateRangeCriteria.create(
                                                        hasLength(updateStart) ? Long.valueOf(updateStart) : null,
                                                        hasLength(updateEnd) ? Long.valueOf(updateEnd) : null) : null
                        )
                        .build();

        PageRequest pageRequest =
                (hasLength(orderBy)) ? pageRequest(pageIndex, pageSize, orderBy) :
                        pageRequest(pageIndex, pageSize, sortActive, sortDirection);

        var resultAndCount = ccQueryService.getCcList(sessionService.asScoreUser(user), filterCriteria, pageRequest);

        PageResponse<CcListEntryRecord> response = new PageResponse<>();
        response.setList(resultAndCount.result());
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(resultAndCount.count());
        return response;
    }

    @GetMapping(value = "/acc/{accManifestId:[\\d]+}")
    public AccDetailsRecord getAccDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("accManifestId") AccManifestId accManifestId) {
        return ccQueryService.getAccDetails(sessionService.asScoreUser(user), accManifestId);
    }

    @GetMapping(value = "/acc/{accManifestId:[\\d]+}/prev")
    public AccDetailsRecord getPrevAccDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("accManifestId") AccManifestId accManifestId) {
        return ccQueryService.getPrevAccDetails(sessionService.asScoreUser(user), accManifestId);
    }

    @GetMapping(value = "/extension/{accManifestId:[\\d]+}")
    public AccDetailsRecord getCcNode(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                      @PathVariable("accManifestId") AccManifestId accManifestId) {
        return getAccDetails(user, accManifestId);
//        return ccQueryService.getExtensionNode(sessionService.asScoreUser(user), accManifestId);
    }

    @GetMapping(value = "/ascc/{asccManifestId:[\\d]+}")
    public AsccDetailsRecord getAsccDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("asccManifestId") AsccManifestId asccManifestId) {
        return ccQueryService.getAsccDetails(sessionService.asScoreUser(user), asccManifestId);
    }

    @GetMapping(value = "/bcc/{bccManifestId:[\\d]+}")
    public BccDetailsRecord getBccDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("bccManifestId") BccManifestId bccManifestId) {
        return ccQueryService.getBccDetails(sessionService.asScoreUser(user), bccManifestId);
    }

    @GetMapping(value = "/acc/{accManifestId:[\\d]+}/base-acc-list")
    public List<CcListEntryRecord> getBaseAccList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                  @PathVariable("accManifestId") AccManifestId accManifestId) {

        return ccQueryService.getBaseAccList(sessionService.asScoreUser(user), accManifestId);
    }

    @GetMapping(value = "/ascc/refactor")
    public CcRefactorValidationResponse refactorValidation(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam("targetManifestId") AsccManifestId targetManifestId,
            @RequestParam("destinationManifestId") AccManifestId destinationManifestId) {

        return ccQueryService.validateAsccRefactoring(
                sessionService.asScoreUser(user),
                targetManifestId, destinationManifestId);
    }

    @GetMapping(value = "/bcc/refactor")
    public CcRefactorValidationResponse refactorValidation(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam("targetManifestId") BccManifestId targetManifestId,
            @RequestParam("destinationManifestId") AccManifestId destinationManifestId) {

        return ccQueryService.validateBccRefactoring(
                sessionService.asScoreUser(user),
                targetManifestId, destinationManifestId);
    }

    @GetMapping(value = "/acc/{accManifestId:[\\d]+}/verify")
    public CcVerifyAppendResponse verifyAppendNode(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("accManifestId") AccManifestId accManifestId,
            @RequestParam(value = "basedAccManifestId", required = false) AccManifestId basedAccManifestId,
            @RequestParam(value = "propertyTerm", required = false) String propertyTerm,
            @RequestParam(value = "asccpManifestId", required = false) AsccpManifestId asccpManifestId,
            @RequestParam(value = "bccpManifestId", required = false) BccpManifestId bccpManifestId) {

        ScoreUser requester = sessionService.asScoreUser(user);

        try {
            if (StringUtils.hasLength(propertyTerm)) {
                ccQueryService.assertSamePropertyTerm(requester, accManifestId, propertyTerm);
            } else if (basedAccManifestId != null) {
                ccQueryService.verifySetBasedAcc(requester, accManifestId, basedAccManifestId);
            } else if (asccpManifestId != null) {
                ccQueryService.verifyAppendAsccp(requester, accManifestId, asccpManifestId);
            } else if (bccpManifestId != null) {
                ccQueryService.verifyAppendBccp(requester, accManifestId, bccpManifestId);
            }
        } catch (AssertionError e) {
            return new CcVerifyAppendResponse(true, e.getMessage());
        }

        return new CcVerifyAppendResponse(false, null);
    }

    @GetMapping(value = "/asccp/{asccpManifestId:[\\d]+}")
    public AsccpDetailsRecord getAsccpDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("asccpManifestId") AsccpManifestId asccpManifestId) {
        return ccQueryService.getAsccpDetails(sessionService.asScoreUser(user), asccpManifestId);
    }

    @GetMapping(value = "/asccp/{asccpManifestId:[\\d]+}/prev")
    public AsccpDetailsRecord getPrevAsccpDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("asccpManifestId") AsccpManifestId asccpManifestId) {
        return ccQueryService.getPrevAsccpDetails(sessionService.asScoreUser(user), asccpManifestId);
    }

    @GetMapping(value = "/bccp/{bccpManifestId:[\\d]+}")
    public BccpDetailsRecord getBccpDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("bccpManifestId") BccpManifestId bccpManifestId) {
        return ccQueryService.getBccpDetails(sessionService.asScoreUser(user), bccpManifestId);
    }

    @GetMapping(value = "/bccp/{bccpManifestId:[\\d]+}/prev")
    public BccpDetailsRecord getPrevBccpDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("bccpManifestId") BccpManifestId bccpManifestId) {
        return ccQueryService.getPrevBccpDetails(sessionService.asScoreUser(user), bccpManifestId);
    }

    @GetMapping(value = "/dt/{dtManifestId:[\\d]+}")
    public DtDetailsRecord getDtDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("dtManifestId") DtManifestId dtManifestId) {
        return ccQueryService.getDtDetails(sessionService.asScoreUser(user), dtManifestId);
    }

    @GetMapping(value = "/dt/{dtManifestId:[\\d]+}/prev")
    public DtDetailsRecord getPrevDtDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("dtManifestId") DtManifestId dtManifestId) {
        return ccQueryService.getPrevDtDetails(sessionService.asScoreUser(user), dtManifestId);
    }

    @GetMapping(value = "/dt/{representationTerm}/primitive-values")
    public List<DtScAwdPriDetailsRecord> getDefaultPrimitiveValues(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("representationTerm") String representationTerm,
            @RequestParam(name = "dtScManifestId") DtScManifestId dtScManifestId) {
        return ccQueryService.getDefaultPrimitiveValues(sessionService.asScoreUser(user), representationTerm, dtScManifestId);
    }

    @GetMapping(value = "/dt-sc/{dtScManifestId:[\\d]+}")
    public DtScDetailsRecord getDtScDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("dtScManifestId") DtScManifestId dtScManifestId) {
        return ccQueryService.getDtScDetails(sessionService.asScoreUser(user), dtScManifestId);
    }

    @GetMapping(value = "/changes-in-release/{releaseId:[\\d]+}")
    public CcChangesResponse getCcChanges(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("releaseId") ReleaseId releaseId) {

        return ccQueryService.getCcChanges(sessionService.asScoreUser(user), releaseId);
    }

    @GetMapping(value = "/export/standalone")
    public ResponseEntity<DeleteOnCloseFileSystemResource> exportStandaloneSchema(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam(name = "asccpManifestIdList") String asccpManifestIdList) throws Exception {

        ExportStandaloneSchemaResponse response =
                releaseQueryService.exportStandaloneSchema(sessionService.asScoreUser(user),
                        separate(asccpManifestIdList).map(e -> AsccpManifestId.from(e)).collect(toSet()));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.filename() + "\"")
                .contentType(MediaType.parseMediaType(
                        (response.filename().endsWith(".zip") ? "application/zip" : "application/xml")
                ))
                .contentLength(response.file().length())
                .body(new DeleteOnCloseFileSystemResource(response.file()));
    }

    @GetMapping(value = "/asccp/{asccpManifestId:[\\d]+}/plantuml")
    public Map<String, String> generatePlantUml(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("asccpManifestId") AsccpManifestId asccpManifestId,
            @RequestParam(value = "asccpLinkTemplate", defaultValue = "/core_component/asccp/{manifestId}") String asccpLinkTemplate,
            @RequestParam(value = "bccpLinkTemplate", defaultValue = "/core_component/bccp/{manifestId}") String bccpLinkTemplate) throws IOException {

        String text = ccQueryService.generatePlantUmlText(sessionService.asScoreUser(user),
                asccpManifestId, asccpLinkTemplate, bccpLinkTemplate);

        return Map.of("text", text,
                "encodedText", plantUmlService.getEncodedText(text));
    }

    // BIE Support

    @RequestMapping(value = "/dt/{dtManifestId:[\\d]+}/primitives",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DtAwdPriSummaryRecord> availableDtAwdPriListByBccpManifestId(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("dtManifestId") DtManifestId dtManifestId) {

        return ccQueryService.availableDtAwdPriListByDtManifestId(sessionService.asScoreUser(user), dtManifestId);
    }

    @RequestMapping(value = "/dt/{dtManifestId:[\\d]+}/code-lists",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CodeListSummaryRecord> availableCodeListListByBccpManifestId(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("dtManifestId") DtManifestId dtManifestId) {

        return codeListQueryService.availableCodeListListByDtManifestId(sessionService.asScoreUser(user), dtManifestId);
    }

    @RequestMapping(value = "/dt/{dtManifestId:[\\d]+}/agency-id-lists",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AgencyIdListSummaryRecord> availableAgencyIdListListByBccpManifestId(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("dtManifestId") DtManifestId dtManifestId) {

        return agencyIdListQueryService.availableAgencyIdListListByDtManifestId(sessionService.asScoreUser(user), dtManifestId);
    }

    @RequestMapping(value = "/dt-sc/{dtScManifestId:[\\d]+}/primitives",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DtScAwdPriSummaryRecord> availableDtScAwdPriListByDtScManifestId(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("dtScManifestId") DtScManifestId dtScManifestId) {

        return ccQueryService.availableDtScAwdPriListByDtScManifestId(sessionService.asScoreUser(user), dtScManifestId);
    }

    @RequestMapping(value = "/dt-sc/{dtScManifestId:[\\d]+}/code-lists",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CodeListSummaryRecord> availableCodeListListByBdtScManifestId(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("dtScManifestId") DtScManifestId dtScManifestId) {

        return codeListQueryService.availableCodeListListByDtScManifestId(sessionService.asScoreUser(user), dtScManifestId);
    }

    @RequestMapping(value = "/dt-sc/{dtScManifestId:[\\d]+}/agency-id-lists",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AgencyIdListSummaryRecord> availableAgencyIdListListByBdtScManifestId(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("dtScManifestId") DtScManifestId dtScManifestId) {

        return agencyIdListQueryService.availableAgencyIdListListByDtScManifestId(sessionService.asScoreUser(user), dtScManifestId);
    }

}
