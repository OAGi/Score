package org.oagi.score.gateway.http.api.oas_management.controller;

import io.swagger.v3.oas.annotations.Parameter;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.context_management.business_context.service.BusinessContextQueryService;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.*;
import org.oagi.score.gateway.http.api.oas_management.model.*;
import org.oagi.score.gateway.http.api.oas_management.service.OpenAPIDocService;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.*;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

@RestController
public class OpenAPIDocController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OpenAPIDocService oasDocService;

    @Autowired
    private BusinessContextQueryService businessContextQueryService;

    @Autowired
    private ApplicationConfigurationService applicationConfigurationService;

    @Autowired
    private SessionService sessionService;

    @RequestMapping(value = "/oas_docs", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<OasDoc> getOasDocList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam(name = "openAPIVersion", required = false) String openAPIVersion,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "description", required = false) String description,
            @RequestParam(name = "version", required = false) String version,
            @RequestParam(name = "updaterUsernameList", required = false) String updaterUsernameList,
            @RequestParam(name = "updateStart", required = false) String updateStart,
            @RequestParam(name = "updateEnd", required = false) String updateEnd,
            @RequestParam(name = "licenseName", required = false) String licenseName,
            @RequestParam(name = "sortActives") String sortActives,
            @RequestParam(name = "sortDirections") String sortDirections,
            @RequestParam(name = "pageIndex") int pageIndex,
            @RequestParam(name = "pageSize") int pageSize) {
        
        GetOasDocListRequest request = new GetOasDocListRequest(sessionService.asScoreUser(user));

        request.setOpenAPIVersion(openAPIVersion);
        request.setTitle(title);
        request.setLicenseName(licenseName);
        request.setVersion(version);
        request.setDescription(description);

        request.setUpdaterUsernameList(!hasLength(updaterUsernameList) ? Collections.emptyList() :
                Arrays.asList(updaterUsernameList.split(",")).stream().map(e -> e.trim())
                        .filter(e -> hasLength(e)).collect(Collectors.toList()));
        if (hasLength(updateStart)) {
            request.setUpdateStartDate(new Timestamp(Long.valueOf(updateStart)).toLocalDateTime());
        }
        if (hasLength(updateEnd)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf(updateEnd));
            calendar.add(Calendar.DATE, 1);
            request.setUpdateEndDate(new Timestamp(calendar.getTimeInMillis()).toLocalDateTime());
        }

        request.setPageIndex(pageIndex);
        request.setPageSize(pageSize);
        request.setSortActives(!hasLength(sortActives) ? Collections.emptyList() :
                Arrays.asList(sortActives.split(",")).stream().map(e -> e.trim()).filter(e -> hasLength(e)).collect(Collectors.toList()));
        request.setSortDirections(!hasLength(sortDirections) ? Collections.emptyList() :
                Arrays.asList(sortDirections.split(",")).stream().map(e -> e.trim()).filter(e -> hasLength(e)).map(e -> SortDirection.valueOf(e.toUpperCase())).collect(Collectors.toList()));

        GetOasDocListResponse response = oasDocService.getOasDocList(sessionService.asScoreUser(user), request);
        PageResponse<OasDoc> pageResponse = new PageResponse<>();
        pageResponse.setList(response.getResults());
        pageResponse.setPage(response.getPage());
        pageResponse.setSize(response.getSize());
        pageResponse.setLength(response.getLength());

        return pageResponse;
    }

    @RequestMapping(value = "/oas_docs/check_uniqueness", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean checkUniqueness(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody OasDoc oasDoc) {
        return oasDocService.checkOasDocUniqueness(sessionService.asScoreUser(user), oasDoc);
    }

    @RequestMapping(value = "/oas_docs/check_title_uniqueness", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean checkTitleUniqueness(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody OasDoc oasDoc) {
        return oasDocService.checkOasDocTitleUniqueness(sessionService.asScoreUser(user), oasDoc);
    }

    @RequestMapping(value = "/oas_doc", method = RequestMethod.POST)
    public ResponseEntity create(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody OasDoc oasDoc) {
        CreateOasDocRequest request = new CreateOasDocRequest(sessionService.asScoreUser(user));
        request.setOwnerUserId(user.getName());
        request.setOpenAPIVersion(oasDoc.getOpenAPIVersion());
        request.setTitle(oasDoc.getTitle());
        request.setDescription(oasDoc.getDescription());
        request.setVersion(oasDoc.getVersion());
        request.setTermsOfService(oasDoc.getTermsOfService());
        request.setContactEmail(oasDoc.getContactEmail());
        request.setContactName(oasDoc.getContactName());
        request.setContactUrl(oasDoc.getContactUrl());
        request.setLicenseName(oasDoc.getLicenseName());
        request.setLicenseUrl(oasDoc.getLicenseUrl());

        CreateOasDocResponse response = oasDocService.createOasDoc(sessionService.asScoreUser(user), request);

        if (response.getOasDocId() != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/oas_doc/{id:[\\d]+}/select_bie", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<BieForOasDoc> selectBieForOasDoc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("id") BigInteger oasDocId,
            @RequestParam(name = "den", required = false) String den,
            @RequestParam(name = "propertyTerm", required = false) String propertyTerm,
            @RequestParam(name = "businessContext", required = false) String businessContext,
            @RequestParam(name = "version", required = false) String version,
            @RequestParam(name = "remark", required = false) String remark,
            @RequestParam(name = "asccpManifestId", required = false) AsccpManifestId asccpManifestId,
            @RequestParam(name = "access", required = false) String access,
            @RequestParam(name = "states", required = false) String states,
            @RequestParam(name = "excludePropertyTerms", required = false) String excludePropertyTerms,
            @RequestParam(name = "excludeTopLevelAsbiepIds", required = false) String excludeTopLevelAsbiepIds,
            @RequestParam(name = "ownerLoginIdList", required = false) String ownerLoginIdList,
            @RequestParam(name = "updaterLoginIdList", required = false) String updaterLoginIdList,
            @RequestParam(name = "updateStart", required = false) String updateStart,
            @RequestParam(name = "updateEnd", required = false) String updateEnd,
            @RequestParam(name = "ownedByDeveloper", required = false) Boolean ownedByDeveloper,
            @RequestParam(name = "libraryId", required = false) LibraryId libraryId,
            @RequestParam(name = "releaseId", required = false) ReleaseId releaseId,
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

        BieForOasDocListRequest request = new BieForOasDocListRequest();
        request.setDen(den);
        request.setPropertyTerm(propertyTerm);
        request.setBusinessContext(businessContext);
        request.setVersion(version);
        request.setRemark(remark);
        request.setAsccpManifestId(asccpManifestId);
        request.setAccess(hasLength(access) ? AccessPrivilege.valueOf(access) : null);
        request.setStates(hasLength(states) ?
                Arrays.asList(states.split(",")).stream()
                        .map(e -> BieState.valueOf(e)).collect(Collectors.toList()) : Collections.emptyList());
        request.setExcludePropertyTerms(!hasLength(excludePropertyTerms) ? Collections.emptyList() :
                Arrays.asList(excludePropertyTerms.split(",")).stream().map(e -> e.trim()).filter(e -> hasLength(e)).collect(Collectors.toList()));
        request.setExcludeTopLevelAsbiepIds(!hasLength(excludeTopLevelAsbiepIds) ? Collections.emptyList() :
                Arrays.asList(excludeTopLevelAsbiepIds.split(",")).stream().map(e -> e.trim()).filter(e -> hasLength(e)).map(e -> TopLevelAsbiepId.from(e)).collect(Collectors.toList()));
        request.setOwnerLoginIdList(!hasLength(ownerLoginIdList) ? Collections.emptyList() :
                Arrays.asList(ownerLoginIdList.split(",")).stream().map(e -> e.trim()).filter(e -> hasLength(e)).collect(Collectors.toList()));
        request.setUpdaterLoginIdList(!hasLength(updaterLoginIdList) ? Collections.emptyList() :
                Arrays.asList(updaterLoginIdList.split(",")).stream().map(e -> e.trim()).filter(e -> hasLength(e)).collect(Collectors.toList()));

        request.setOwnedByDeveloper(ownedByDeveloper);

        if (libraryId != null && libraryId.value().longValue() > 0) {
            request.setLibraryId(libraryId);
        }
        if (releaseId != null && releaseId.value().longValue() > 0) {
            request.setReleaseId(releaseId);
        }

        if (hasLength(updateStart)) {
            request.setUpdateStartDate(new Date(Long.valueOf(updateStart)));
        }
        if (hasLength(updateEnd)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf(updateEnd));
            calendar.add(Calendar.DATE, 1);
            request.setUpdateEndDate(calendar.getTime());
        }

        PageRequest pageRequest =
                (StringUtils.hasLength(orderBy)) ? pageRequest(pageIndex, pageSize, orderBy) :
                        pageRequest(pageIndex, pageSize, sortActive, sortDirection);

        request.setPageRequest(pageRequest);
        return oasDocService.selectBieForOasDoc(sessionService.asScoreUser(user), request);
    }

    @RequestMapping(value = "/oas_doc/{oasDocId:[\\d]+}/bie_list/delete", method = RequestMethod.POST)
    public ResponseEntity deletes(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody DeleteBieForOasDocRequestData requestData) {

        DeleteBieForOasDocRequest request = new DeleteBieForOasDocRequest(sessionService.asScoreUser(user))
                .withBieForOasDocList(requestData.getBieForOasDocList());
        request.setOasDocId(requestData.getOasDocId());
        DeleteBieForOasDocResponse response = oasDocService.deleteBieForOasDoc(sessionService.asScoreUser(user), request);

        if (response.containsAll(requestData.getBieForOasDocList())) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    public static class DeleteBieForOasDocRequestData {
        private BigInteger oasDocId;
        private List<BieForOasDoc> bieForOasDocList = Collections.emptyList();

        public BigInteger getOasDocId() {
            return oasDocId;
        }

        public void setOasDocId(BigInteger oasDocId) {
            this.oasDocId = oasDocId;
        }

        public List<BieForOasDoc> getBieForOasDocList() {
            return bieForOasDocList;
        }

        public void setBieForOasDocList(List<BieForOasDoc> bieForOasDocList) {
            this.bieForOasDocList = bieForOasDocList;
        }
    }

    @RequestMapping(value = "/oas_doc/{id:[\\d]+}/bie_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<BieForOasDoc> getBieListForOasDoc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("id") BigInteger oasDocId,
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
            @RequestParam(name = "sortActives") String sortActives,
            @RequestParam(name = "sortDirections") String sortDirections,
            @RequestParam(name = "pageIndex") int pageIndex,
            @RequestParam(name = "pageSize") int pageSize) {

        ScoreUser requester = sessionService.asScoreUser(user);
        GetBieForOasDocRequest request = new GetBieForOasDocRequest(requester);

        request.setOasDocId(oasDocId);
        request.setSortActives(!hasLength(sortActives) ? Collections.emptyList() :
                Arrays.asList(sortActives.split(",")).stream().map(e -> e.trim()).filter(e -> hasLength(e)).collect(Collectors.toList()));
        request.setSortDirections(!hasLength(sortDirections) ? Collections.emptyList() :
                Arrays.asList(sortDirections.split(",")).stream().map(e -> e.trim()).filter(e -> hasLength(e)).map(e -> SortDirection.valueOf(e.toUpperCase())).collect(Collectors.toList()));
        request.setPageIndex(pageIndex);
        request.setPageSize(pageSize);

        GetBieForOasDocResponse bieForOasDocList = oasDocService.getBieForOasDoc(sessionService.asScoreUser(user), request);

        bieForOasDocList.getResults().forEach(bieList -> {
//            bieList.setBusinessContexts(
//                    businessContextQueryService.getBusinessContextSummaryList(
//                            requester, bieList.getTopLevelAsbiepId(), request.getBusinessContext())
//            );
            bieList.setAccess(AccessPrivilege.toAccessPrivilege(requester, bieList.getOwner().userId(), bieList.getState()).toString());
        });

        PageResponse<BieForOasDoc> pageResponse = new PageResponse<>();
        pageResponse.setList(bieForOasDocList.getResults());
        pageResponse.setPage(bieForOasDocList.getPage());
        pageResponse.setSize(bieForOasDocList.getSize());
        pageResponse.setLength(bieForOasDocList.getLength());

        return pageResponse;

    }

    @RequestMapping(value = "/oas_doc/{id:[\\d]+}/bie_list/{topLevelAsbiepId:[\\d]+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BieForOasDoc getBieForOasDoc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("oasDocId") BigInteger oasDocId,
            @PathVariable("topLevelAsbiepId") TopLevelAsbiepId selectedTopLevelAsbiepId) {

        BieForOasDoc bieForOasDoc = new BieForOasDoc();

        GetBieForOasDocRequest request = new GetBieForOasDocRequest(sessionService.asScoreUser(user));

        request.setOasDocId(oasDocId);

        GetBieForOasDocResponse bieForOasDocList = oasDocService.getBieForOasDoc(sessionService.asScoreUser(user), request);

        return bieForOasDocList.getResults().stream()
                .filter(c -> c.getTopLevelAsbiepId().equals(selectedTopLevelAsbiepId))
                .findAny().get();
    }

    @RequestMapping(value = "/oas_doc/{id:[\\d]+}/bie_list", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addBieForOasDoc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody AssignBieForOasDoc assignBieForOasDoc) {
        ScoreUser requester = sessionService.asScoreUser(user);
        AddBieForOasDocRequest request = new AddBieForOasDocRequest(requester);
        request.setOasRequest(assignBieForOasDoc.isOasRequest());
        request.setTopLevelAsbiepId(assignBieForOasDoc.getTopLevelAsbiepId());
        request.setOasDocId(assignBieForOasDoc.getOasDocId());
        request.setMakeArrayIndicator(assignBieForOasDoc.isArrayIndicator());
        //issue#1492 Comments by Scott without displaying, default Suppress Root = true when Adding BIE
        request.setSuppressRootIndicator(true);
        request.setTagName(assignBieForOasDoc.getTagName());
        String verbOption = assignBieForOasDoc.getVerb();
        String resoureName = null;
        GetOasDocRequest oasDocRequest = new GetOasDocRequest(requester);
        oasDocRequest.setOasDocId(assignBieForOasDoc.getOasDocId());
        GetOasDocResponse oasDocResponse = oasDocService.getOasDoc(requester, oasDocRequest);
        String oasDocVersion = null;
        if (oasDocResponse != null) {
            oasDocVersion = oasDocResponse.getOasDoc().getVersion();
        }
        String bieForOasDocPropertyTermWithDash = assignBieForOasDoc.getPropertyTerm().replaceAll("\\s", "-");
        //get BusinessContext for the assigned BieForOasDoc
        assignBieForOasDoc.setBusinessContexts(
                businessContextQueryService.getBusinessContextSummaryList(
                        sessionService.asScoreUser(user),
                        assignBieForOasDoc.getTopLevelAsbiepId(), request.getBusinessContext())
        );

        String businessContextName = assignBieForOasDoc.getBusinessContexts().get(0).name().toLowerCase();
        businessContextName = businessContextName.replace(' ', '-');

        boolean isArray = request.isMakeArrayIndicator();
        if (oasDocVersion != null) {
            resoureName = "/" + businessContextName + "/" + oasDocVersion + "/" + ((isArray) ? bieForOasDocPropertyTermWithDash.toLowerCase() + "-list" :
                    bieForOasDocPropertyTermWithDash.toLowerCase());

        } else {
            resoureName = "/" + businessContextName + "/" + ((isArray) ? bieForOasDocPropertyTermWithDash.toLowerCase() + "-list" :
                    bieForOasDocPropertyTermWithDash.toLowerCase());
        }
        request.setPath(resoureName);
        request.setVerb(verbOption);
        SetOperationIdWithVerb setOperationIdWithVerb = new SetOperationIdWithVerb(verbOption, businessContextName, assignBieForOasDoc.getPropertyTerm(),
                isArray);
        String operationId = setOperationIdWithVerb.verbToOperationId();
        request.setOperationId(operationId);
        request.setMakeArrayIndicator(assignBieForOasDoc.isArrayIndicator());
        //issue#1492 Comments by Scott without displaying, default Suppress Root = true when Adding BIE
        request.setSuppressRootIndicator(true);
        request.setRequiredForRequestBody(assignBieForOasDoc.isRequired());
        request.setDeprecatedForOperation(false);
        AddBieForOasDocResponse response = oasDocService.addBieForOasDoc(sessionService.asScoreUser(user), request);
        if (response.getOasResponseId() != null || response.getOasRequestId() != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/oas_doc/{id:[\\d]+}/check_bie_reused_across_operations", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ReusedBIEViolationCheckResponse checkBIEReusedAcrossOperations(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody BieForOasDoc selectedBieForOasDoc,
            @PathVariable("id") BigInteger oasDocId) {
        // Retrieve all current assigned bie list for this given oasDocId
        ReusedBIEViolationCheck reusedBIEViolationCheck = new ReusedBIEViolationCheck(oasDocId);
        ReusedBIEViolationCheckResponse response = new ReusedBIEViolationCheckResponse();
        List<String> errorMessages = new ArrayList<>();
        GetBieForOasDocRequest getBieForOasDocRequest = new GetBieForOasDocRequest(sessionService.asScoreUser(user));
        getBieForOasDocRequest.setOasDocId(oasDocId);
        GetBieForOasDocResponse bieForOasDocList = oasDocService.getBieForOasDoc(sessionService.asScoreUser(user), getBieForOasDocRequest);
        if (bieForOasDocList != null && bieForOasDocList.getResults() != null) {
            for (BieForOasDoc bieForOasDoc : bieForOasDocList.getResults()) {
                TopLevelAsbiepId selectedTopLevelAsbiepId = bieForOasDoc.getTopLevelAsbiepId();

                reusedBIEViolationCheck.getReusedBIE(selectedTopLevelAsbiepId)
                        .putOperation(bieForOasDoc.getVerb(), Pair.of(bieForOasDoc.getMessageBody(), bieForOasDoc.getOperationId()));
                reusedBIEViolationCheck.getReusedBIE(selectedTopLevelAsbiepId)
                        .putResourcePath(bieForOasDoc.getVerb(), bieForOasDoc.getResourceName());
            }
        }

        // Check reusedBIE across multiple operations
        // use the table in issue #1519 for violation check
        ReusedBIERecord reusedBIERecord = reusedBIEViolationCheck.getReusedBIE(selectedBieForOasDoc.getTopLevelAsbiepId());
        String selectedVerb = selectedBieForOasDoc.getVerb();
        if (selectedVerb.equals("GET") && "Request".equals(selectedBieForOasDoc.getMessageBody())) {
            errorMessages.add("A request body rarely used for GET operation.");
        } else {
            if (reusedBIERecord.hasReusedOperations(selectedVerb,
                    Pair.of(selectedBieForOasDoc.getMessageBody(), selectedBieForOasDoc.getOperationId()))) {
                errorMessages.add("There is an existing " + selectedBieForOasDoc.getMessageBody().toLowerCase() +
                        " message body for the operation " + selectedVerb + " '" + selectedBieForOasDoc.getOperationId() + "' for the same BIE.");
            }
        }

        response.setErrorMessages(errorMessages);
        return response;
    }

    @RequestMapping(value = "/oas_doc/{id:[\\d]+}/check_bie_reused_across_operations_after_update", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ReusedBIEViolationCheckResponse checkBIEReusedAcrossOperationsAfterUpdate(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("id") BigInteger oasDocId) {
        // Retrieve all current assigned bie list for this given oasDocId
        ReusedBIEViolationCheck reusedBIEViolationCheck = new ReusedBIEViolationCheck(oasDocId);
        ReusedBIEViolationCheckResponse response = new ReusedBIEViolationCheckResponse();
        List<String> errorMessages = new ArrayList<>();
        GetBieForOasDocRequest getBieForOasDocRequest = new GetBieForOasDocRequest(sessionService.asScoreUser(user));
        getBieForOasDocRequest.setOasDocId(oasDocId);
        GetBieForOasDocResponse bieForOasDocList = oasDocService.getBieForOasDoc(
                sessionService.asScoreUser(user), getBieForOasDocRequest);
        if (bieForOasDocList != null && bieForOasDocList.getResults() != null) {
            for (BieForOasDoc bieForOasDoc : bieForOasDocList.getResults()) {
                TopLevelAsbiepId selectedTopLevelAsbiepId = bieForOasDoc.getTopLevelAsbiepId();

                reusedBIEViolationCheck.getReusedBIE(selectedTopLevelAsbiepId)
                        .putOperation(bieForOasDoc.getVerb(), Pair.of(bieForOasDoc.getMessageBody(), bieForOasDoc.getOperationId()));
                reusedBIEViolationCheck.getReusedBIE(selectedTopLevelAsbiepId)
                        .putResourcePath(bieForOasDoc.getVerb(), bieForOasDoc.getResourceName());
            }
        }

        response.setErrorMessages(errorMessages);
        return response;
    }

    private class ReusedBIEViolationCheck {

        private BigInteger oasDocId;
        private Map<TopLevelAsbiepId, ReusedBIERecord> reusedBIEMap = new HashMap<>();

        public ReusedBIEViolationCheck(BigInteger oasDocId) {
            this.oasDocId = oasDocId;
        }

        public Map<TopLevelAsbiepId, ReusedBIERecord> getReusedBIEMap() {
            return reusedBIEMap;
        }

        public void setReusedBIEMap(Map<TopLevelAsbiepId, ReusedBIERecord> reusedBIEMap) {
            this.reusedBIEMap = reusedBIEMap;
        }

        public ReusedBIERecord getReusedBIE(TopLevelAsbiepId topLevelAsbiepId) {
            if (!reusedBIEMap.containsKey(topLevelAsbiepId)) {
                ReusedBIERecord reusedBIERecord = new ReusedBIERecord(topLevelAsbiepId);
                reusedBIEMap.put(topLevelAsbiepId, reusedBIERecord);
            }
            return reusedBIEMap.get(topLevelAsbiepId);
        }

        public BigInteger getOasDocId() {
            return oasDocId;
        }

        public void setOasDocId(BigInteger oasDocId) {
            this.oasDocId = oasDocId;
        }
    }

    private class ReusedBIERecord {
        private TopLevelAsbiepId topLevelAsbiepId;
        private Map<String, Set<Pair<String, String>>> reusedOperations = new HashMap<>();
        private Map<String, Set<String>> reusedResourcePath = new HashMap<>();

        public ReusedBIERecord(TopLevelAsbiepId topLevelAsbiepId) {
            this.topLevelAsbiepId = topLevelAsbiepId;
        }

        public TopLevelAsbiepId getTopLevelAsbiepId() {
            return topLevelAsbiepId;
        }

        public void setTopLevelAsbiepId(TopLevelAsbiepId topLevelAsbiepId) {
            this.topLevelAsbiepId = topLevelAsbiepId;
        }

        public Map<String, Set<Pair<String, String>>> getReusedOperations() {
            return reusedOperations;
        }

        public void setReusedOperations(Map<String, Set<Pair<String, String>>> reusedOperations) {
            this.reusedOperations = reusedOperations;
        }

        /**
         * Adds the specified operation to this set.
         * More formally, adds the specified verb {@code verb} to this set
         * if the set contains no element {@code operation}.
         *
         * @param verb verb to be added to this set
         * @param operation resource path
         * @return {@code true} if this set did not already contain the specified operation
         */
        public boolean putOperation(String verb, Pair<String, String> operation) {
            if (!this.reusedOperations.containsKey(verb)) {
                this.reusedOperations.put(verb, new HashSet<>());
            }
            return this.reusedOperations.get(verb).add(operation);
        }

        public boolean hasReusedOperations(String verb, Pair<String, String> operation) {
            if (!this.reusedOperations.containsKey(verb)) {
                return false;
            }
            return this.reusedOperations.get(verb).contains(operation);
        }

        public Map<String, Set<String>> getReusedResourcePath() {
            return reusedResourcePath;
        }

        public void setReusedResourcePath(Map<String, Set<String>> reusedResourcePath) {
            this.reusedResourcePath = reusedResourcePath;
        }

        /**
         * Adds the specified resource path to this set.
         * More formally, adds the specified verb {@code verb} to this set
         * if the set contains no element {@code resourcePath}.
         *
         * @param verb verb to be added to this set
         * @param resourcePath resource path
         * @return {@code true} if this set did not already contain the specified resource path
         */
        public boolean putResourcePath(String verb, String resourcePath) {
            if (!this.reusedResourcePath.containsKey(verb)) {
                this.reusedResourcePath.put(verb, new HashSet<>());
            }
            return this.reusedResourcePath.get(verb).add(resourcePath);
        }
    }

    @RequestMapping(value = "/oas_doc/{id:[\\d]+}/bie_list/detail", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateBieForOasDoc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody BieForOasDocUpdateRequest request) {

        UpdateBieForOasDocRequest updateBieForOasDocRequest = new UpdateBieForOasDocRequest(sessionService.asScoreUser(user));
        updateBieForOasDocRequest.setOasDocId(request.getOasDocId());
        updateBieForOasDocRequest.setBieForOasDocList(request.getBieForOasDocList());

        ScoreUser requester = sessionService.asScoreUser(user);

        for (BieForOasDoc bieForOasDoc : updateBieForOasDocRequest.getBieForOasDocList()) {
            if (bieForOasDoc.getOasResourceId() != null) {
                GetOasOperationRequest getOasOperationRequest = new GetOasOperationRequest(requester)
                        .withOasResourceId(bieForOasDoc.getOasResourceId());
                GetOasOperationResponse oasOperationResponse = oasDocService.getOasOperation(requester, getOasOperationRequest);
                if (!bieForOasDoc.getVerb().equals(oasOperationResponse.getOasOperation().getVerb())) {

                    UpdateOperationIdWhenVerbChanged updateOperationIdWhenVerbChanged = new UpdateOperationIdWhenVerbChanged(
                            bieForOasDoc.getVerb(), bieForOasDoc.getOperationId(), bieForOasDoc.isArrayIndicator());
                    String updatedOperationId = updateOperationIdWhenVerbChanged.verbToOperationId();
                    bieForOasDoc.setOperationId(updatedOperationId);
                }
            }

            if (bieForOasDoc.getOasOperationId() != null) {
                if (bieForOasDoc.getMessageBody().equals("Request")) {
                    GetOasRequestTableRequest getOasRequestTableRequest = new GetOasRequestTableRequest(requester)
                            .withOasOperationId(bieForOasDoc.getOasOperationId());
                    GetOasRequestTableResponse oasRequestTableResponse = oasDocService.getOasRequestTable(requester, getOasRequestTableRequest);
                    if (oasRequestTableResponse != null &&
                            oasRequestTableResponse.getOasRequestTable() != null
                            && bieForOasDoc.isArrayIndicator() != oasRequestTableResponse.getOasRequestTable().isMakeArrayIndicator()) {
                        String newResourceName = null;
                        String newOperationId = null;
                        String oldResourceName = bieForOasDoc.getResourceName();
                        String oldOperationId = bieForOasDoc.getOperationId();
                        if (bieForOasDoc.isArrayIndicator()) {
                            if (!oldResourceName.endsWith("-list")) {
                                newResourceName = oldResourceName + "-list";
                            }
                            if (!oldOperationId.endsWith("List")) {
                                newOperationId = oldOperationId + "List";
                            }
                        } else {
                            if (oldResourceName.endsWith("-list")) {
                                newResourceName = oldResourceName.substring(0, oldResourceName.length() - 5);
                            }
                            if (oldOperationId.endsWith("List")) {
                                newOperationId = oldOperationId.substring(0, oldOperationId.length() - 4);
                            }
                        }
                        if (newResourceName != null) {
                            bieForOasDoc.setResourceName(newResourceName);
                        }
                        if (newOperationId != null) {
                            bieForOasDoc.setOperationId(newOperationId);
                        }
                    }
                }

                if (bieForOasDoc.getMessageBody().equals("Response")) {
                    GetOasResponseTableRequest getOasResponseTableRequest = new GetOasResponseTableRequest(requester)
                            .withOasOperationId(bieForOasDoc.getOasOperationId());
                    GetOasResponseTableResponse oasResponseTableResponse = oasDocService.getOasResponseTable(requester, getOasResponseTableRequest);
                    if (oasResponseTableResponse != null &&
                            oasResponseTableResponse.getOasResponseTable() != null
                            && bieForOasDoc.isArrayIndicator() != oasResponseTableResponse.getOasResponseTable().isMakeArrayIndicator()) {
                        String newResourceName = null;
                        String newOperationId = null;
                        String oldResourceName = bieForOasDoc.getResourceName();
                        String oldOperationId = bieForOasDoc.getOperationId();
                        if (bieForOasDoc.isArrayIndicator()) {
                            if (!oldResourceName.endsWith("-list")) {
                                newResourceName = oldResourceName + "-list";
                            }
                            if (!oldOperationId.endsWith("List")) {
                                newOperationId = oldOperationId + "List";
                            }
                        } else {
                            if (oldResourceName.endsWith("-list")) {
                                newResourceName = oldResourceName.substring(0, oldResourceName.length() - 5);
                            }
                            if (oldOperationId.endsWith("List")) {
                                newOperationId = oldOperationId.substring(0, oldOperationId.length() - 4);
                            }
                        }
                        if (newResourceName != null) {
                            bieForOasDoc.setResourceName(newResourceName);
                        }
                        if (newOperationId != null) {
                            bieForOasDoc.setOperationId(newOperationId);
                        }
                    }
                }
            }
        }

        UpdateBieForOasDocResponse response = oasDocService.updateDetails(requester, updateBieForOasDocRequest);

        if (response.getOasDocId() != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/oas_doc/{id:[\\d]+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public OasDoc getOasDoc(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("id") BigInteger oasDocId) {

        GetOasDocRequest request = new GetOasDocRequest(
                sessionService.asScoreUser(user));
        request.setOasDocId(oasDocId);

        GetOasDocResponse response = oasDocService.getOasDoc(sessionService.asScoreUser(user), request);
        return response.getOasDoc();
    }

    @RequestMapping(value = "/oas_doc/{id:[\\d]+}", method = RequestMethod.POST)
    public ResponseEntity update(
            @PathVariable("id") String oasDocId,
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody OasDoc oasDoc) {

        UpdateOasDocRequest request = new UpdateOasDocRequest(sessionService.asScoreUser(user))
                .withOasDocId(oasDocId);
        request.setOwnerUserId(user.getName());
        request.setOpenAPIVersion(oasDoc.getOpenAPIVersion());
        request.setTitle(oasDoc.getTitle());
        request.setTermsOfService(oasDoc.getTermsOfService());
        request.setVersion(oasDoc.getVersion());
        request.setDescription(oasDoc.getDescription());
        request.setContactEmail(oasDoc.getContactEmail());
        request.setContactName(oasDoc.getContactName());
        request.setContactUrl(oasDoc.getContactUrl());
        request.setLicenseName(oasDoc.getLicenseName());
        request.setLicenseUrl(oasDoc.getLicenseUrl());

        UpdateOasDocResponse response = oasDocService.updateOasDoc(sessionService.asScoreUser(user), request);

        if (response.getOasDocId() != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/oas_doc/{id:[\\d]+}", method = RequestMethod.DELETE)
    public ResponseEntity delete(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("id") BigInteger oasDocId) throws ScoreDataAccessException {

        ScoreUser requester = sessionService.asScoreUser(user);

        GetBieForOasDocRequest getBieForOasDocRequest = new GetBieForOasDocRequest(requester);

        getBieForOasDocRequest.setOasDocId(oasDocId);

        GetBieForOasDocResponse bieForOasDocList = oasDocService.getBieForOasDoc(requester, getBieForOasDocRequest);

        DeleteBieForOasDocRequest deleteBieForOasDocRequest = new DeleteBieForOasDocRequest(requester)
                .withBieForOasDocList(bieForOasDocList.getResults());
        deleteBieForOasDocRequest.setOasDocId(oasDocId);
        DeleteBieForOasDocResponse deleteBieForOasDocResponse = oasDocService.deleteBieForOasDoc(requester, deleteBieForOasDocRequest);

        if (deleteBieForOasDocResponse.containsAll(bieForOasDocList.getResults())) {
            DeleteOasDocRequest deleteOasDocRequest = new DeleteOasDocRequest(requester)
                    .withOasDocIdList(Arrays.asList(oasDocId));
            DeleteOasDocResponse response = oasDocService.deleteOasDoc(requester, deleteOasDocRequest);

            if (response.contains(oasDocId)) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    public static class DeleteOasDocRequestData {
        private List<BigInteger> oasDocIdList = Collections.emptyList();

        public List<BigInteger> getOasDocIdList() {
            return oasDocIdList;
        }

        public void setOasDocIdList(List<BigInteger> oasDocIdList) {
            this.oasDocIdList = oasDocIdList;
        }
    }

    @RequestMapping(value = "/oas_doc/delete", method = RequestMethod.POST)
    public ResponseEntity deletes(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody DeleteOasDocRequestData requestData) {
        DeleteOasDocRequest request =
                new DeleteOasDocRequest(sessionService.asScoreUser(user))
                        .withOasDocIdList(requestData.getOasDocIdList());
        DeleteOasDocResponse response =
                oasDocService.deleteOasDoc(sessionService.asScoreUser(user), request);

        if (response.containsAll(requestData.getOasDocIdList())) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}


