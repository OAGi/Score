package org.oagi.score.gateway.http.api.oas_management.controller;

import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.oas_management.data.*;
import org.oagi.score.gateway.http.api.oas_management.service.OpenAPIDocService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.base.SortDirection;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.api.businesscontext.model.GetBusinessContextListRequest;
import org.oagi.score.repo.api.businesscontext.model.GetBusinessContextListResponse;
import org.oagi.score.repo.api.openapidoc.model.*;
import org.oagi.score.service.authentication.AuthenticationService;
import org.oagi.score.service.businesscontext.BusinessContextService;
import org.oagi.score.service.common.data.AccessPrivilege;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.impl.utils.StringUtils.hasLength;

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
            @RequestParam(name = "sortActives") String sortActives,
            @RequestParam(name = "sortDirections") String sortDirections,
            @RequestParam(name = "pageIndex") int pageIndex,
            @RequestParam(name = "pageSize") int pageSize) {
        GetOasDocListRequest request = new GetOasDocListRequest(authenticationService.asScoreUser(requester));

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
            @RequestParam(name = "version", required = false) String version,
            @RequestParam(name = "remark", required = false) String remark,
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
            @RequestParam(name = "libraryId", required = false) BigInteger libraryId,
            @RequestParam(name = "releaseId", required = false) BigInteger releaseId,
            @RequestParam(name = "sortActive") String sortActive,
            @RequestParam(name = "sortDirection") String sortDirection,
            @RequestParam(name = "pageIndex") int pageIndex,
            @RequestParam(name = "pageSize") int pageSize) {

        BieForOasDocListRequest request = new BieForOasDocListRequest();
        request.setDen(den);
        request.setPropertyTerm(propertyTerm);
        request.setBusinessContext(businessContext);
        request.setVersion(version);
        request.setRemark(remark);
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

        if (libraryId != null && libraryId.compareTo(BigInteger.ZERO) > 0) {
            request.setLibraryId(libraryId);
        }
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
            @PathVariable("id") BigInteger oasDocId,
            @RequestParam(name = "den", required = false) String den,
            @RequestParam(name = "propertyTerm", required = false) String propertyTerm,
            @RequestParam(name = "businessContext", required = false) String businessContext,
            @RequestParam(name = "asccpManifestId", required = false) BigInteger asccpManifestId,
            @RequestParam(name = "access", required = false) String access,
            @RequestParam(name = "states", required = false) String states,
            @RequestParam(name = "excludePropertyTerms", required = false) String excludePropertyTerms,
            @RequestParam(name = "topLevelAsbiepIds", required = false) String topLevelAsbiepIds,
            @RequestParam(name = "excludeTopLevelAsbiepIds", required = false) String excludeTopLevelAsbiepIds,
            @RequestParam(name = "ownerLoginIds", required = false) String ownerLoginIds,
            @RequestParam(name = "updaterLoginIds", required = false) String updaterLoginIds,
            @RequestParam(name = "updateStart", required = false) String updateStart,
            @RequestParam(name = "updateEnd", required = false) String updateEnd,
            @RequestParam(name = "ownedByDeveloper", required = false) Boolean ownedByDeveloper,
            @RequestParam(name = "releaseIds", required = false) String releaseIds,
            @RequestParam(name = "sortActives") String sortActives,
            @RequestParam(name = "sortDirections") String sortDirections,
            @RequestParam(name = "pageIndex") int pageIndex,
            @RequestParam(name = "pageSize") int pageSize) {

        GetBieForOasDocRequest request = new GetBieForOasDocRequest(authenticationService.asScoreUser(requester));

        AppUser appUser = sessionService.getAppUserByUsername(requester);

        request.setOasDocId(oasDocId);
        request.setSortActives(!hasLength(sortActives) ? Collections.emptyList() :
                Arrays.asList(sortActives.split(",")).stream().map(e -> e.trim()).filter(e -> hasLength(e)).collect(Collectors.toList()));
        request.setSortDirections(!hasLength(sortDirections) ? Collections.emptyList() :
                Arrays.asList(sortDirections.split(",")).stream().map(e -> e.trim()).filter(e -> hasLength(e)).map(e -> SortDirection.valueOf(e.toUpperCase())).collect(Collectors.toList()));
        request.setPageIndex(pageIndex);
        request.setPageSize(pageSize);

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
        //issue#1492 Comments by Scott without displaying, default Suppress Root = true when Adding BIE
        request.setSuppressRootIndicator(true);
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
        //get BusinessContext for the assigned BieForOasDoc
        GetBusinessContextListRequest getBusinessContextListRequest =
                new GetBusinessContextListRequest(authenticationService.asScoreUser(requester))
                        .withTopLevelAsbiepIdList(Arrays.asList(assignBieForOasDoc.getTopLevelAsbiepId()))
                        .withName(request.getBusinessContext());

        getBusinessContextListRequest.setPageIndex(-1);
        getBusinessContextListRequest.setPageSize(-1);

        GetBusinessContextListResponse getBusinessContextListResponse = businessContextService
                .getBusinessContextList(getBusinessContextListRequest, applicationConfigurationService.isTenantEnabled());
        assignBieForOasDoc.setBusinessContexts(getBusinessContextListResponse.getResults());
        //

        String businessContextName = assignBieForOasDoc.getBusinessContexts().get(0).getName().toLowerCase();
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
        AddBieForOasDocResponse response = oasDocService.addBieForOasDoc(requester, request);
        if (response.getOasResponseId() != null || response.getOasRequestId() != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/oas_doc/{id:[\\d]+}/check_bie_reused_across_operations", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ReusedBIEViolationCheckResponse checkBIEReusedAcrossOperations(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody BieForOasDoc selectedBieForOasDoc,
            @PathVariable("id") BigInteger oasDocId) {
        // Retrieve all current assigned bie list for this given oasDocId
        ReusedBIEViolationCheck reusedBIEViolationCheck = new ReusedBIEViolationCheck(oasDocId);
        ReusedBIEViolationCheckResponse response = new ReusedBIEViolationCheckResponse();
        List<String> errorMessages = new ArrayList<>();
        GetBieForOasDocRequest getBieForOasDocRequest = new GetBieForOasDocRequest(authenticationService.asScoreUser(requester));
        getBieForOasDocRequest.setOasDocId(oasDocId);
        GetBieForOasDocResponse bieForOasDocList = oasDocService.getBieForOasDoc(getBieForOasDocRequest);
        if (bieForOasDocList != null && bieForOasDocList.getResults() != null) {
            for (BieForOasDoc bieForOasDoc : bieForOasDocList.getResults()) {
                BigInteger selectedTopLevelAsbiepId = bieForOasDoc.getTopLevelAsbiepId();

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
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger oasDocId) {
        // Retrieve all current assigned bie list for this given oasDocId
        ReusedBIEViolationCheck reusedBIEViolationCheck = new ReusedBIEViolationCheck(oasDocId);
        ReusedBIEViolationCheckResponse response = new ReusedBIEViolationCheckResponse();
        List<String> errorMessages = new ArrayList<>();
        GetBieForOasDocRequest getBieForOasDocRequest = new GetBieForOasDocRequest(authenticationService.asScoreUser(requester));
        getBieForOasDocRequest.setOasDocId(oasDocId);
        GetBieForOasDocResponse bieForOasDocList = oasDocService.getBieForOasDoc(getBieForOasDocRequest);
        if (bieForOasDocList != null && bieForOasDocList.getResults() != null) {
            for (BieForOasDoc bieForOasDoc : bieForOasDocList.getResults()) {
                BigInteger selectedTopLevelAsbiepId = bieForOasDoc.getTopLevelAsbiepId();

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
        private Map<BigInteger, ReusedBIERecord> reusedBIEMap = new HashMap<>();

        public ReusedBIEViolationCheck(BigInteger oasDocId) {
            this.oasDocId = oasDocId;
        }

        public Map<BigInteger, ReusedBIERecord> getReusedBIEMap() {
            return reusedBIEMap;
        }

        public void setReusedBIEMap(Map<BigInteger, ReusedBIERecord> reusedBIEMap) {
            this.reusedBIEMap = reusedBIEMap;
        }

        public ReusedBIERecord getReusedBIE(BigInteger topLevelAsbiepId) {
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
        private BigInteger topLevelAsbiepId;
        private Map<String, Set<Pair<String, String>>> reusedOperations = new HashMap<>();
        private Map<String, Set<String>> reusedResourcePath = new HashMap<>();

        public ReusedBIERecord(BigInteger topLevelAsbiepId) {
            this.topLevelAsbiepId = topLevelAsbiepId;
        }

        public BigInteger getTopLevelAsbiepId() {
            return topLevelAsbiepId;
        }

        public void setTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
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
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody BieForOasDocUpdateRequest request) {

        UpdateBieForOasDocRequest updateBieForOasDocRequest = new UpdateBieForOasDocRequest(authenticationService.asScoreUser(requester));

        updateBieForOasDocRequest.setOasDocId(request.getOasDocId());

        updateBieForOasDocRequest.setBieForOasDocList(request.getBieForOasDocList());

        for (BieForOasDoc bieForOasDoc : updateBieForOasDocRequest.getBieForOasDocList()) {
            if (bieForOasDoc.getOasResourceId() != null) {
                GetOasOperationRequest getOasOperationRequest = new GetOasOperationRequest(authenticationService.asScoreUser(requester))
                        .withOasResourceId(bieForOasDoc.getOasResourceId());
                GetOasOperationResponse oasOperationResponse = oasDocService.getOasOperation(getOasOperationRequest);
                if (!bieForOasDoc.getVerb().equals(oasOperationResponse.getOasOperation().getVerb())) {

                    UpdateOperationIdWhenVerbChanged updateOperationIdWhenVerbChanged = new UpdateOperationIdWhenVerbChanged(
                            bieForOasDoc.getVerb(), bieForOasDoc.getOperationId(), bieForOasDoc.isArrayIndicator());
                    String updatedOperationId = updateOperationIdWhenVerbChanged.verbToOperationId();
                    bieForOasDoc.setOperationId(updatedOperationId);
                }
            }

            if (bieForOasDoc.getOasOperationId() != null) {
                if (bieForOasDoc.getMessageBody().equals("Request")) {
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

                if (bieForOasDoc.getMessageBody().equals("Response")) {
                    GetOasResponseTableRequest getOasResponseTableRequest = new GetOasResponseTableRequest(authenticationService.asScoreUser(requester))
                            .withOasOperationId(bieForOasDoc.getOasOperationId());
                    GetOasResponseTableResponse oasResponseTableResponse = oasDocService.getOasResponseTable(getOasResponseTableRequest);
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

        GetBieForOasDocRequest getBieForOasDocRequest = new GetBieForOasDocRequest(authenticationService.asScoreUser(requester));

        AppUser appUser = sessionService.getAppUserByUsername(requester);

        getBieForOasDocRequest.setOasDocId(oasDocId);

        GetBieForOasDocResponse bieForOasDocList = oasDocService.getBieForOasDoc(getBieForOasDocRequest);

        DeleteBieForOasDocRequest deleteBieForOasDocRequest = new DeleteBieForOasDocRequest(authenticationService.asScoreUser(requester))
                .withBieForOasDocList(bieForOasDocList.getResults());
        deleteBieForOasDocRequest.setOasDocId(oasDocId);
        DeleteBieForOasDocResponse deleteBieForOasDocResponse = oasDocService.deleteBieForOasDoc(deleteBieForOasDocRequest);

        if (deleteBieForOasDocResponse.containsAll(bieForOasDocList.getResults())) {
            DeleteOasDocRequest deleteOasDocRequest = new DeleteOasDocRequest(authenticationService.asScoreUser(requester))
                    .withOasDocIdList(Arrays.asList(oasDocId));
            DeleteOasDocResponse response = oasDocService.deleteOasDoc(deleteOasDocRequest);

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
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody DeleteOasDocRequestData requestData) {
        DeleteOasDocRequest request =
                new DeleteOasDocRequest(authenticationService.asScoreUser(requester))
                        .withOasDocIdList(requestData.getOasDocIdList());
        DeleteOasDocResponse response =
                oasDocService.deleteOasDoc(request);

        if (response.containsAll(requestData.getOasDocIdList())) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}


