package org.oagi.score.gateway.http.api.oas_management.controller;

import org.jooq.impl.QOM;
import org.oagi.score.data.TopLevelAsbiep;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.oas_management.data.*;
import org.oagi.score.gateway.http.api.oas_management.service.OpenAPIDocService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.api.businesscontext.model.GetBusinessContextListRequest;
import org.oagi.score.repo.api.businesscontext.model.GetBusinessContextListResponse;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.openapidoc.model.*;
import org.oagi.score.repo.api.openapidoc.model.OasDoc;
import org.oagi.score.service.authentication.AuthenticationService;
import org.oagi.score.service.businesscontext.BusinessContextService;
import org.oagi.score.service.common.data.AccessPrivilege;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.redisson.transaction.operation.set.SetOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression.Helper.camelCase;
import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.base.SortDirection.DESC;

@RestController
public class OpenAPIDocController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private OpenAPIDocService oasDocService;
    @Autowired
    private BusinessContextService businessContextService;
    @Autowired
    private ApplicationConfigurationService applicationConfigurationService;
    @Autowired
    private SessionService sessionService;

    @RequestMapping(value = "/oas_docs", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<OasDoc> getOasDocList(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestParam(name = "openAPIVersion", required = false) String openAPIVersion,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "description", required = false) String description,
            @RequestParam(name = "version", required = false) String version,
            @RequestParam(name = "updaterUsernameList", required = false) String updaterUsernameList,
            @RequestParam(name = "updateStart", required = false) String updateStart,
            @RequestParam(name = "updateEnd", required = false) String updateEnd,
            @RequestParam(name = "licenseName", required = false) String licenseName,
            @RequestParam(name = "sortActive") String sortActive,
            @RequestParam(name = "sortDirection") String sortDirection,
            @RequestParam(name = "pageIndex") int pageIndex,
            @RequestParam(name = "pageSize") int pageSize
    ) {
        GetOasDocListRequest request = new GetOasDocListRequest(authenticationService.asScoreUser(requester));

        request.setOpenAPIVersion(openAPIVersion);
        request.setTitle(title);
        request.setLicenseName(licenseName);
        request.setVersion(version);

        request.setUpdaterUsernameList(!StringUtils.hasLength(updaterUsernameList) ? Collections.emptyList() :
                Arrays.asList(updaterUsernameList.split(",")).stream().map(e -> e.trim())
                        .filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList()));
        if (StringUtils.hasLength(updateStart)) {
            request.setUpdateStartDate(new Timestamp(Long.valueOf(updateStart)).toLocalDateTime());
        }
        if (StringUtils.hasLength(updateEnd)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf(updateEnd));
            calendar.add(Calendar.DATE, 1);
            request.setUpdateEndDate(new Timestamp(calendar.getTimeInMillis()).toLocalDateTime());
        }

        request.setPageIndex(pageIndex);
        request.setPageSize(pageSize);
        request.setSortActive(sortActive);
        request.setSortDirection("asc".equalsIgnoreCase(sortDirection) ? ASC : DESC);

        GetOasDocListResponse response = oasDocService.getOasDocList(request);
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
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody OasDoc oasDoc) {
        return oasDocService.checkOasDocUniqueness(oasDoc);
    }


    @RequestMapping(value = "/oas_docs/check_title_uniqueness", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean checkTitleUniqueness(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody OasDoc oasDoc) {
        return oasDocService.checkOasDocTitleUniqueness(oasDoc);
    }

    @RequestMapping(value = "/oas_doc", method = RequestMethod.POST)
    public ResponseEntity create(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody OasDoc oasDoc) {
        CreateOasDocRequest request = new CreateOasDocRequest(authenticationService.asScoreUser(requester));
        request.setOwnerUserId(requester.getName());
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

        CreateOasDocResponse response = oasDocService.createOasDoc(request);

        if (response.getOasDocId() != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/oas_doc/{id:[\\d]+}/select_bie", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<BieForOasDoc> selectBieForOasDoc(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger oasDocId,
            @RequestParam(name = "den", required = false) String den,
            @RequestParam(name = "propertyTerm", required = false) String propertyTerm,
            @RequestParam(name = "businessContext", required = false) String businessContext,
            @RequestParam(name = "asccpManifestId", required = false) BigInteger asccpManifestId,
            @RequestParam(name = "access", required = false) String access,
            @RequestParam(name = "states", required = false) String states,
            @RequestParam(name = "excludePropertyTerms", required = false) String excludePropertyTerms,
            @RequestParam(name = "excludeTopLevelAsbiepIds", required = false) String excludeTopLevelAsbiepIds,
            @RequestParam(name = "ownerLoginIds", required = false) String ownerLoginIds,
            @RequestParam(name = "updaterLoginIds", required = false) String updaterLoginIds,
            @RequestParam(name = "updateStart", required = false) String updateStart,
            @RequestParam(name = "updateEnd", required = false) String updateEnd,
            @RequestParam(name = "ownedByDeveloper", required = false) Boolean ownedByDeveloper,
            @RequestParam(name = "releaseId", required = false) BigInteger releaseId,
            @RequestParam(name = "sortActive") String sortActive,
            @RequestParam(name = "sortDirection") String sortDirection,
            @RequestParam(name = "pageIndex") int pageIndex,
            @RequestParam(name = "pageSize") int pageSize) {

        BieForOasDocListRequest request = new BieForOasDocListRequest();
        request.setDen(den);
        request.setPropertyTerm(propertyTerm);
        request.setBusinessContext(businessContext);
        request.setAsccpManifestId(asccpManifestId);
        request.setAccess(org.springframework.util.StringUtils.hasLength(access) ? AccessPrivilege.valueOf(access) : null);
        request.setStates(org.springframework.util.StringUtils.hasLength(states) ?
                Arrays.asList(states.split(",")).stream()
                        .map(e -> BieState.valueOf(e)).collect(Collectors.toList()) : Collections.emptyList());
        request.setExcludePropertyTerms(!org.springframework.util.StringUtils.hasLength(excludePropertyTerms) ? Collections.emptyList() :
                Arrays.asList(excludePropertyTerms.split(",")).stream().map(e -> e.trim()).filter(e -> org.springframework.util.StringUtils.hasLength(e)).collect(Collectors.toList()));
        request.setExcludeTopLevelAsbiepIds(!org.springframework.util.StringUtils.hasLength(excludeTopLevelAsbiepIds) ? Collections.emptyList() :
                Arrays.asList(excludeTopLevelAsbiepIds.split(",")).stream().map(e -> e.trim()).filter(e -> org.springframework.util.StringUtils.hasLength(e)).map(e -> new BigInteger(e)).collect(Collectors.toList()));
        request.setOwnerLoginIds(!org.springframework.util.StringUtils.hasLength(ownerLoginIds) ? Collections.emptyList() :
                Arrays.asList(ownerLoginIds.split(",")).stream().map(e -> e.trim()).filter(e -> org.springframework.util.StringUtils.hasLength(e)).collect(Collectors.toList()));
        request.setUpdaterLoginIds(!org.springframework.util.StringUtils.hasLength(updaterLoginIds) ? Collections.emptyList() :
                Arrays.asList(updaterLoginIds.split(",")).stream().map(e -> e.trim()).filter(e -> org.springframework.util.StringUtils.hasLength(e)).collect(Collectors.toList()));

        request.setOwnedByDeveloper(ownedByDeveloper);

        if (releaseId != null && releaseId.compareTo(BigInteger.ZERO) > 0) {
            request.setReleaseId(releaseId);
        }

        if (org.springframework.util.StringUtils.hasLength(updateStart)) {
            request.setUpdateStartDate(new Date(Long.valueOf(updateStart)));
        }
        if (org.springframework.util.StringUtils.hasLength(updateEnd)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf(updateEnd));
            calendar.add(Calendar.DATE, 1);
            request.setUpdateEndDate(calendar.getTime());
        }

        PageRequest pageRequest = new PageRequest();
        pageRequest.setSortActive(sortActive);
        pageRequest.setSortDirection(sortDirection);
        pageRequest.setPageIndex(pageIndex);
        pageRequest.setPageSize(pageSize);
        request.setPageRequest(pageRequest);
        return oasDocService.selectBieForOasDoc(requester, request);
    }

    @RequestMapping(value = "/oas_doc/{oasDocId:[\\d]+}/bie_list/delete", method = RequestMethod.POST)
    public ResponseEntity deletes(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody DeleteBieForOasDocRequestData requestData) {

        DeleteBieForOasDocRequest request = new DeleteBieForOasDocRequest(authenticationService.asScoreUser(requester))
                .withBieForOasDocList(requestData.getBieForOasDocList());
        request.setOasDocId(requestData.getOasDocId());
        DeleteBieForOasDocResponse response = oasDocService.deleteBieForOasDoc(request);

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
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger oasDocId) {

        GetBieForOasDocRequest request = new GetBieForOasDocRequest(authenticationService.asScoreUser(requester));

        AppUser appUser = sessionService.getAppUserByUsername(requester);

        request.setOasDocId(oasDocId);

        GetBieForOasDocResponse bieForOasDocList = oasDocService.getBieForOasDoc(request);

        bieForOasDocList.getResults().forEach(bieList -> {

            GetBusinessContextListRequest getBusinessContextListRequest =
                    new GetBusinessContextListRequest(authenticationService.asScoreUser(requester))
                            .withTopLevelAsbiepIdList(Arrays.asList(bieList.getTopLevelAsbiepId()))
                            .withName(request.getBusinessContext());

            getBusinessContextListRequest.setPageIndex(-1);
            getBusinessContextListRequest.setPageSize(-1);

            GetBusinessContextListResponse getBusinessContextListResponse = businessContextService
                    .getBusinessContextList(getBusinessContextListRequest, applicationConfigurationService.isTenantEnabled());

            bieList.setBusinessContexts(getBusinessContextListResponse.getResults());
            bieList.setAccess(AccessPrivilege.toAccessPrivilege(appUser, bieList.getOwnerUserId(), bieList.getState()).toString()
            );
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
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("oasDocId") BigInteger oasDocId,
            @PathVariable("topLevelAsbiepId") BigInteger selectedTopLevelAsbiepId) {

        BieForOasDoc bieForOasDoc = new BieForOasDoc();

        GetBieForOasDocRequest request = new GetBieForOasDocRequest(authenticationService.asScoreUser(requester));

        request.setOasDocId(oasDocId);

        GetBieForOasDocResponse bieForOasDocList = oasDocService.getBieForOasDoc(request);

        return bieForOasDocList.getResults().stream()
                .filter(c -> c.getTopLevelAsbiepId() == selectedTopLevelAsbiepId)
                .findAny().get();
    }

    @RequestMapping(value = "/oas_doc/{id:[\\d]+}/bie_list", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addBieForOasDoc(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody AssignBieForOasDoc assignBieForOasDoc) {
        AddBieForOasDocRequest request = new AddBieForOasDocRequest(authenticationService.asScoreUser(requester));
        request.setOasRequest(assignBieForOasDoc.isOasRequest());
        request.setTopLevelAsbiepId(assignBieForOasDoc.getTopLevelAsbiepId());
        request.setOasDocId(assignBieForOasDoc.getOasDocId());
        request.setMakeArrayIndicator(assignBieForOasDoc.isArrayIndicator());
        request.setSuppressRootIndicator(assignBieForOasDoc.isSuppressRootIndicator());
        request.setTagName(assignBieForOasDoc.getTagName());
        String verbOption = assignBieForOasDoc.getVerb();
        String resoureName = null;
        GetOasDocRequest oasDocRequest = new GetOasDocRequest(authenticationService.asScoreUser(requester));
        oasDocRequest.setOasDocId(assignBieForOasDoc.getOasDocId());
        GetOasDocResponse oasDocResponse = oasDocService.getOasDoc(oasDocRequest);
        String oasDocVersion = null;
        if (oasDocResponse != null) {
            oasDocVersion = oasDocResponse.getOasDoc().getVersion();
        }
        String bieForOasDocPropertyTermWithDash = assignBieForOasDoc.getPropertyTerm().replaceAll("\\s", "-");
        boolean isArray = request.isMakeArrayIndicator();
        if (oasDocVersion != null) {
            resoureName = "/" + oasDocVersion + "/" + ((isArray) ? bieForOasDocPropertyTermWithDash.toLowerCase() + "-list" :
                    bieForOasDocPropertyTermWithDash.toLowerCase());

        } else {
            resoureName = "/" + ((isArray) ? bieForOasDocPropertyTermWithDash.toLowerCase() + "-list" :
                    bieForOasDocPropertyTermWithDash.toLowerCase());
        }
        request.setPath(resoureName);
        request.setVerb(verbOption);
        SetOperationIdWithVerb setOperationIdWithVerb = new SetOperationIdWithVerb(verbOption, assignBieForOasDoc.getPropertyTerm(),
                isArray);
        String operationId = setOperationIdWithVerb.verbToOperationId();
        request.setOperationId(operationId);
        request.setMakeArrayIndicator(assignBieForOasDoc.isArrayIndicator());
        request.setSuppressRootIndicator(assignBieForOasDoc.isSuppressRootIndicator());
        request.setRequiredForRequestBody(assignBieForOasDoc.isRequired());
        request.setDeprecatedForOperation(false);
        AddBieForOasDocResponse response = oasDocService.addBieForOasDoc(requester, request);
        if (response.getOasResponseId() != null || response.getOasRequestId() != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/oas_doc/check_bie_reused_across_operations", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity checkBIEReusedAcrossOperations(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody BieForOasDoc selectedBieForOasDoc) {
        //Retrieve all current assigned bie list for this given oasDocId
        ReusedBIEViolationCheck reusedBIEViolationCheck = new ReusedBIEViolationCheck(selectedBieForOasDoc.getOasDocId());
        GetBieForOasDocRequest getBieForOasDocRequest = new GetBieForOasDocRequest(authenticationService.asScoreUser(requester));
        getBieForOasDocRequest.setOasDocId(selectedBieForOasDoc.getOasDocId());
        GetBieForOasDocResponse bieForOasDocList = oasDocService.getBieForOasDoc(getBieForOasDocRequest);
        if (bieForOasDocList != null && bieForOasDocList.getResults() != null){
            for (BieForOasDoc bieForOasDoc : bieForOasDocList.getResults()){
                BigInteger selectedTopLevelAsbiepId = bieForOasDoc.getTopLevelAsbiepId();
                if (!reusedBIEViolationCheck.getReusedBIEMap().containsKey(selectedTopLevelAsbiepId)){
                    ReusedBIERecord reusedBIERecord = new ReusedBIERecord(selectedTopLevelAsbiepId);
                    reusedBIERecord.getReusedOperations().put(bieForOasDoc.getVerb(), bieForOasDoc.getMessageBody());
                    reusedBIERecord.getReusedResourcePath().put(bieForOasDoc.getVerb(), bieForOasDoc.getResourceName());
                    reusedBIEViolationCheck.getReusedBIEMap().put(selectedTopLevelAsbiepId, reusedBIERecord);
                }
                else{
                    ReusedBIERecord reusedBIERecord = reusedBIEViolationCheck.getReusedBIEMap().get(selectedTopLevelAsbiepId);
                    String verb = bieForOasDoc.getVerb();
                    if (!reusedBIERecord.getReusedOperations().containsKey(verb)){
                        reusedBIERecord.getReusedOperations().put(verb, bieForOasDoc.getMessageBody());
                    } else if (!reusedBIERecord.getReusedOperations().get(verb).contains(bieForOasDoc.getMessageBody())){
                        reusedBIERecord.getReusedOperations().put(verb, bieForOasDoc.getMessageBody());
                    }

                    if (!reusedBIERecord.getReusedResourcePath().containsKey(verb)){
                        reusedBIERecord.getReusedResourcePath().put(verb, bieForOasDoc.getResourceName());
                    } else if (!reusedBIERecord.getReusedResourcePath().get(verb).contains(bieForOasDoc.getResourceName())){
                        reusedBIERecord.getReusedResourcePath().put(verb, bieForOasDoc.getResourceName());
                    }
                }
            }
        }

        // Check reusedBIE across multiple operations
        // use the table in issue #1519 for violation check
        ReusedBIERecord reusedBIERecord = reusedBIEViolationCheck.getReusedBIEMap().get(selectedBieForOasDoc.getTopLevelAsbiepId());
        if (reusedBIERecord != null){
            String selectedVerb = selectedBieForOasDoc.getVerb();
            String existingMessageBody = reusedBIERecord.getReusedOperations().get(selectedVerb);
            if (StringUtils.hasLength(existingMessageBody)){
                if (selectedBieForOasDoc.getMessageBody().equals("Request")){
                    if (selectedVerb.equals("GET")){
                        return ResponseEntity.status(415).body("requestBody is not allowed for: " + selectedVerb);
                    }
                }
            }

        }
        return ResponseEntity.noContent().build();
    }
    private class ReusedBIEViolationCheck{

        private BigInteger oasDocId;
        private HashMap<BigInteger, ReusedBIERecord> reusedBIEMap = new HashMap<>();

        public ReusedBIEViolationCheck(BigInteger oasDocId){
            this.oasDocId = oasDocId;
        }

        public HashMap<BigInteger, ReusedBIERecord> getReusedBIEMap() {
            return reusedBIEMap;
        }

        public void setReusedBIEMap(HashMap<BigInteger, ReusedBIERecord> reusedBIEMap) {
            this.reusedBIEMap = reusedBIEMap;
        }

        public BigInteger getOasDocId() {
            return oasDocId;
        }

        public void setOasDocId(BigInteger oasDocId) {
            this.oasDocId = oasDocId;
        }
    }

    private class ReusedBIERecord {
        private BigInteger topLevelAsbiepId;
        private HashMap<String, String> reusedOperations = new HashMap<>();
        private HashMap<String, String> reusedResourcePath = new HashMap<>();

        public ReusedBIERecord(BigInteger topLevelAsbiepId){
            this.topLevelAsbiepId =  topLevelAsbiepId;
        }

        public BigInteger getTopLevelAsbiepId() {
            return topLevelAsbiepId;
        }

        public void setTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
            this.topLevelAsbiepId = topLevelAsbiepId;
        }

        public HashMap<String, String> getReusedOperations() {
            return reusedOperations;
        }

        public void setReusedOperations(HashMap<String, String> reusedOperations) {
            this.reusedOperations = reusedOperations;
        }

        public HashMap<String, String> getReusedResourcePath() {
            return reusedResourcePath;
        }

        public void setReusedResourcePath(HashMap<String, String> reusedResourcePath) {
            this.reusedResourcePath = reusedResourcePath;
        }
    }

    @RequestMapping(value = "/oas_doc/{id:[\\d]+}/bie_list/detail", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateBieForOasDoc(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody BieForOasDocUpdateRequest request) {

        UpdateBieForOasDocRequest updateBieForOasDocRequest = new UpdateBieForOasDocRequest(authenticationService.asScoreUser(requester));

        updateBieForOasDocRequest.setOasDocId(request.getOasDocId());

        updateBieForOasDocRequest.setBieForOasDocList(request.getBieForOasDocList());

        for (BieForOasDoc bieForOasDoc : updateBieForOasDocRequest.getBieForOasDocList()){
            if (bieForOasDoc.getOasResourceId() != null){
                GetOasOperationRequest getOasOperationRequest = new GetOasOperationRequest(authenticationService.asScoreUser(requester))
                        .withOasResourceId(bieForOasDoc.getOasResourceId());
                GetOasOperationResponse  oasOperationResponse = oasDocService.getOasOperation(getOasOperationRequest);
                if (!bieForOasDoc.getVerb().equals(oasOperationResponse.getOasOperation().getVerb())){

                    UpdateOperationIdWhenVerbChanged updateOperationIdWhenVerbChanged = new UpdateOperationIdWhenVerbChanged(
                            bieForOasDoc.getVerb(), bieForOasDoc.getOperationId(), bieForOasDoc.isArrayIndicator());
                    String updatedOperationId = updateOperationIdWhenVerbChanged.verbToOperationId();
                    bieForOasDoc.setOperationId(updatedOperationId);
                }
            }

            if (bieForOasDoc.getOasOperationId() != null){
                if (bieForOasDoc.getMessageBody().equals("Request")){
                    GetOasRequestTableRequest getOasRequestTableRequest = new GetOasRequestTableRequest(authenticationService.asScoreUser(requester))
                            .withOasOperationId(bieForOasDoc.getOasOperationId());
                    GetOasRequestTableResponse oasRequestTableResponse = oasDocService.getOasRequestTable(getOasRequestTableRequest);
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

                if (bieForOasDoc.getMessageBody().equals("Response")){
                    GetOasResponseTableRequest getOasResponseTableRequest = new GetOasResponseTableRequest(authenticationService.asScoreUser(requester))
                            .withOasOperationId(bieForOasDoc.getOasOperationId());
                    GetOasResponseTableResponse oasResponseTableResponse = oasDocService.getOasResponseTable(getOasResponseTableRequest);
                    if (oasResponseTableResponse != null &&
                            oasResponseTableResponse.getOasResponseTable() != null
                            && bieForOasDoc.isArrayIndicator() != oasResponseTableResponse.getOasResponseTable().isMakeArrayIndicator()){
                        String newResourceName = null;
                        String newOperationId = null;
                        String oldResourceName = bieForOasDoc.getResourceName();
                        String oldOperationId = bieForOasDoc.getOperationId();
                        if (bieForOasDoc.isArrayIndicator()){
                            if (!oldResourceName.endsWith("-list")){
                                newResourceName = oldResourceName + "-list";
                            }
                            if (!oldOperationId.endsWith("List")){
                                newOperationId = oldOperationId + "List";
                            }
                        }
                       else{
                            if (oldResourceName.endsWith("-list")){
                                newResourceName = oldResourceName.substring(0, oldResourceName.length() -5);
                            }
                            if (oldOperationId.endsWith("List")){
                                newOperationId = oldOperationId.substring(0, oldOperationId.length() -4);
                            }
                        }
                       if(newResourceName != null){
                           bieForOasDoc.setResourceName(newResourceName);
                       }
                       if(newOperationId != null){
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
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger oasDocId) {

        GetOasDocRequest request = new GetOasDocRequest(
                authenticationService.asScoreUser(requester));
        request.setOasDocId(oasDocId);

        GetOasDocResponse response = oasDocService.getOasDoc(request);
        return response.getOasDoc();
    }

    @RequestMapping(value = "/oas_doc/{id:[\\d]+}", method = RequestMethod.POST)
    public ResponseEntity update(
            @PathVariable("id") String oasDocId,
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody OasDoc oasDoc) {

        UpdateOasDocRequest request = new UpdateOasDocRequest(authenticationService.asScoreUser(requester))
                .withOasDocId(oasDocId);
        request.setOwnerUserId(requester.getName());
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

        UpdateOasDocResponse response = oasDocService.updateOasDoc(request);

        if (response.getOasDocId() != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/oas_doc/{id:[\\d]+}", method = RequestMethod.DELETE)
    public ResponseEntity delete(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger oasDocId) throws ScoreDataAccessException {

        DeleteOasDocRequest request = new DeleteOasDocRequest(authenticationService.asScoreUser(requester))
                .withOasDocIdList(Arrays.asList(oasDocId));

        DeleteOasDocResponse response = oasDocService.DeleteOasDoc(request);

        if (response.contains(oasDocId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }


}
