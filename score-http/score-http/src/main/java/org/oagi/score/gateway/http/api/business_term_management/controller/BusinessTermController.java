package org.oagi.score.gateway.http.api.business_term_management.controller;

import org.oagi.score.gateway.http.api.business_term_management.data.AssignedBusinessTermListRecord;
import org.oagi.score.gateway.http.api.business_term_management.data.AssignedBusinessTermListRequest;
import org.oagi.score.gateway.http.api.business_term_management.service.BusinessTermService;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.businessterm.model.*;
import org.oagi.score.service.authentication.AuthenticationService;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.base.SortDirection.DESC;

@RestController
public class BusinessTermController {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private BusinessTermService businessTermService;

    private static String TYPE = "text/csv";

    @RequestMapping(value = "/business_terms", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<BusinessTerm> getBusinessTermList(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestParam(name = "businessTerm", required = false) String term,
            @RequestParam(name = "externalReferenceUri", required = false) String externalReferenceUri,
            @RequestParam(name = "externalReferenceId", required = false) String externalReferenceId,
            @RequestParam(name = "definition", required = false) String definition,
            @RequestParam(name = "comment", required = false) String comment,
            @RequestParam(name = "updaterUsernameList", required = false) String updaterUsernameList,
            @RequestParam(name = "updateStart", required = false) String updateStart,
            @RequestParam(name = "updateEnd", required = false) String updateEnd,
            @RequestParam(name = "searchByCC", required = false) boolean searchByCC,
            @RequestParam(name = "byAssignedBieIds", required = false) String byAssignedBieIds,
            @RequestParam(name = "byAssignedBieTypes", required = false) String byAssignedBieTypes,
            @RequestParam(name = "sortActive") String sortActive,
            @RequestParam(name = "sortDirection") String sortDirection,
            @RequestParam(name = "pageIndex") int pageIndex,
            @RequestParam(name = "pageSize") int pageSize
    ) {

        GetBusinessTermListRequest request = new GetBusinessTermListRequest(
                authenticationService.asScoreUser(requester));

        request.setBusinessTerm(term);
        request.setExternalRefUri(externalReferenceUri);
        request.setExternalRefId(externalReferenceId);
        request.setDefinition(definition);
        request.setComment(comment);
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
        if (searchByCC && byAssignedBieIds != null && byAssignedBieTypes != null) {
            List<BigInteger> byAssignedBieIdList = Arrays.stream(byAssignedBieIds.split(",")).map(e -> new BigInteger(e)).collect(Collectors.toList());
            List<String> byAssignedBieTypeList = Arrays.asList(byAssignedBieTypes.split(","));
            if (byAssignedBieIdList.size() == byAssignedBieTypeList.size()) {
                List<BieToAssign> byAssignedBies = IntStream
                        .range(0, byAssignedBieIdList.size())
                        .mapToObj(index -> new BieToAssign(byAssignedBieIdList.get(index), byAssignedBieTypeList.get(index)))
                        .collect(Collectors.toList());
                request.setAssignedBies(byAssignedBies);
            } else {
                System.out.println("ERROR: Assigned bie lists of id and types are different size: "
                        + byAssignedBieIdList.size() + " " + byAssignedBieTypeList.size());
            }
        }

        request.setPageIndex(pageIndex);
        request.setPageSize(pageSize);
        request.setSortActive(sortActive);
        request.setSortDirection("asc".equalsIgnoreCase(sortDirection) ? ASC : DESC);

        GetBusinessTermListResponse response = businessTermService.getBusinessTermList(request);

        PageResponse<BusinessTerm> pageResponse = new PageResponse<>();
        pageResponse.setList(response.getResults());
        pageResponse.setPage(response.getPage());
        pageResponse.setSize(response.getSize());
        pageResponse.setLength(response.getLength());
        return pageResponse;
    }

    @RequestMapping(value = "/business_terms/check_uniqueness", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean checkUniqueness(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody BusinessTerm businessTerm) {
        return businessTermService.checkBusinessTermUniqueness(businessTerm);
    }


    @RequestMapping(value = "/business_terms/check_name_uniqueness", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean checkNameUniqueness(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody BusinessTerm businessTerm) {
        return businessTermService.checkBusinessTermNameUniqueness(businessTerm);
    }

    @RequestMapping(value = "/business_terms/assign", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<AssignedBusinessTermListRecord> getAssignedBusinessTermList(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestParam(name = "bieId", required = false) Optional<BigInteger> bieId,
            @RequestParam(name = "bieDen", required = false) String bieDen,
            @RequestParam(name = "bieTypes", required = false) String bieTypes,
            @RequestParam(name = "businessTerm", required = false) String term,
            @RequestParam(name = "externalReferenceUri", required = false) String externalReferenceUri,
            @RequestParam(name = "typeCode", required = false) String typeCode,
            @RequestParam(name = "primary", required = false) Boolean primary,
            @RequestParam(name = "ownerUsernameList", required = false) String ownerUsernameList,
            @RequestParam(name = "updaterUsernameList", required = false) String updaterUsernameList,
            @RequestParam(name = "updateStart", required = false) String updateStart,
            @RequestParam(name = "updateEnd", required = false) String updateEnd,
            @RequestParam(name = "sortActive") String sortActive,
            @RequestParam(name = "sortDirection") String sortDirection,
            @RequestParam(name = "pageIndex") int pageIndex,
            @RequestParam(name = "pageSize") int pageSize) {

        AssignedBusinessTermListRequest request = new AssignedBusinessTermListRequest();

        if (bieId.isPresent()) {
            request.setBieId(bieId.get());
        }
        request.setBieDen(bieDen);
        request.setBusinessTerm(term);
        request.setExternalReferenceUri(externalReferenceUri);
        request.setBieTypes(Arrays.asList(bieTypes.split(",")));
        request.setPrimary((primary == null) ? false : primary);
        request.setTypeCode(typeCode);

        request.setOwnerLoginIds(!StringUtils.hasLength(ownerUsernameList) ? Collections.emptyList() :
                Arrays.asList(ownerUsernameList.split(",")).stream().map(e -> e.trim())
                        .filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList()));

        request.setUpdaterLoginIds(!StringUtils.hasLength(updaterUsernameList) ? Collections.emptyList() :
                Arrays.asList(updaterUsernameList.split(",")).stream().map(e -> e.trim())
                        .filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList()));
        if (StringUtils.hasLength(updateStart)) {
            request.setUpdateStartDate(new Date(Long.valueOf(updateStart)));
        }
        if (StringUtils.hasLength(updateEnd)) {
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
        return businessTermService.getBusinessTermAssignmentList(requester, request);
    }

    @RequestMapping(value = "/business_term", method = RequestMethod.PUT)
    public ResponseEntity create(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody BusinessTerm businessTerm) {

        CreateBusinessTermRequest request =
                new CreateBusinessTermRequest(authenticationService.asScoreUser(requester));
        request.setBusinessTerm(businessTerm.getBusinessTerm());
        request.setDefinition(businessTerm.getDefinition());
        request.setComment(businessTerm.getComment());
        request.setExternalReferenceId(businessTerm.getExternalReferenceId());
        request.setExternalReferenceUri(businessTerm.getExternalReferenceUri());

        CreateBusinessTermResponse response =
                businessTermService.createBusinessTerm(request);

        if (response.getBusinessTermId() != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/csv/business_terms", method = RequestMethod.POST)
    public ResponseEntity uploadFile(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestParam("file") MultipartFile file) {
        if (TYPE.equals(file.getContentType())) {
            try {
                CreateBulkBusinessTermRequest request = new CreateBulkBusinessTermRequest(authenticationService.asScoreUser(requester));
                request.setInputStream(file.getInputStream());
                CreateBulkBusinessTermResponse response = businessTermService.createBusinessTermsFromFile(request);
                if (response.getBusinessTermIds() != null && !response.getBusinessTermIds().isEmpty()) {
                    System.out.println("Uploaded the file successfully: " + file.getOriginalFilename()
                            + "Created record ids: ");
                    response.getBusinessTermIds().stream().forEach(System.out::println);
                    return ResponseEntity.noContent().build();
                } else {
                    return ResponseEntity.status(200).build();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.badRequest().body(
                        "Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage());
            }
        } else {
            return ResponseEntity.badRequest().body("Please upload a csv file!");
        }
    }

    @RequestMapping(value = "/csv/business_terms/template", method = RequestMethod.GET)
    public ResponseEntity getTemplateFile(
            @AuthenticationPrincipal AuthenticatedPrincipal requester) {

        String templateName = "businessTermTemplateWithExample.csv";
        Resource resource = resourceLoader.getResource("classpath:" + templateName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @RequestMapping(value = "/business_term/{id:[\\d]+}", method = RequestMethod.POST)
    public ResponseEntity update(
            @PathVariable("id") String businessTermId,
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody BusinessTerm businessTerm) {

        UpdateBusinessTermRequest request =
                new UpdateBusinessTermRequest(authenticationService.asScoreUser(requester))
                        .withBusinessTermId(businessTermId);
        request.setBusinessTerm(businessTerm.getBusinessTerm());
        request.setExternalReferenceId(businessTerm.getExternalReferenceId());
        request.setExternalReferenceUri(businessTerm.getExternalReferenceUri());
        request.setComment(businessTerm.getComment());

        UpdateBusinessTermResponse response =
                businessTermService.updateBusinessTerm(request);

        if (response.getBusinessTermId() != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/business_term/{id:[\\d]+}", method = RequestMethod.DELETE)
    public ResponseEntity delete(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger businessTermId) throws ScoreDataAccessException {

        DeleteBusinessTermRequest request =
                new DeleteBusinessTermRequest(authenticationService.asScoreUser(requester))
                        .withBusinessTermIdList(Arrays.asList(businessTermId));

        DeleteBusinessTermResponse response =
                businessTermService.deleteBusinessTerm(request);

        if (response.contains(businessTermId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    public static class DeleteBusinessTermRequestData {
        private List<BigInteger> businessTermIdList = Collections.emptyList();

        public List<BigInteger> getBusinessTermIdList() {
            return businessTermIdList;
        }

        public void setBusinessTermIdList(List<BigInteger> businessTermIdList) {
            this.businessTermIdList = businessTermIdList;
        }
    }

    @RequestMapping(value = "/business_term/delete", method = RequestMethod.POST)
    public ResponseEntity deletes(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody DeleteBusinessTermRequestData requestData) {
        DeleteBusinessTermRequest request =
                new DeleteBusinessTermRequest(authenticationService.asScoreUser(requester))
                        .withBusinessTermIdList(requestData.getBusinessTermIdList());

        DeleteBusinessTermResponse response =
                businessTermService.deleteBusinessTerm(request);

        if (response.containsAll(requestData.getBusinessTermIdList())) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/business_term/{id:[\\d]+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BusinessTerm getBusinessTerm(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger businessTermId) {

        GetBusinessTermRequest request = new GetBusinessTermRequest(
                authenticationService.asScoreUser(requester));
        request.setBusinessTermId(businessTermId);

        GetBusinessTermResponse response =
                businessTermService.getBusinessTerm(request);
        return response.getBusinessTerm();
    }

    @RequestMapping(value = "/business_terms/assign", method = RequestMethod.PUT)
    public ResponseEntity create(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody AssignBusinessTermRequest request) {
        request.setRequester(authenticationService.asScoreUser(requester));
        AssignBusinessTermResponse response =
                businessTermService.assignBusinessTerm(request);

        if (response.getAssignedBusinessTermIdList() != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/business_terms/assign/check_uniqueness", method = RequestMethod.POST)
    public boolean checkAssignmentUniqueness(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody AssignBusinessTermRequest request) {
        request.setRequester(authenticationService.asScoreUser(requester));
        return businessTermService.checkAssignmentUniqueness(request);
    }

    @RequestMapping(value = "/business_terms/assign/{type}/{id:[\\d]+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AssignedBusinessTerm getAssignedBusinessTerm(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("type") String bieType,
            @PathVariable("id") BigInteger assignedBizTermId) {
        GetAssignedBusinessTermRequest request = new GetAssignedBusinessTermRequest(
                authenticationService.asScoreUser(requester))
                .withAssignedBizTermId(assignedBizTermId)
                .withBieType(bieType);

        return businessTermService.getBusinessTermAssignment(request);
    }

    @RequestMapping(value = "/business_terms/assign/{type}/{id:[\\d]+}", method = RequestMethod.POST)
    public ResponseEntity updateAssignment(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger assignedBizTermId,
            @PathVariable("type") String bieType,
            @RequestBody AssignedBusinessTerm assignedBusinessTerm) {

        UpdateBusinessTermAssignmentRequest request =
                new UpdateBusinessTermAssignmentRequest(authenticationService.asScoreUser(requester))
                        .withAssignedBizTermId(assignedBizTermId);
        request.setBieType(bieType);
        request.setBieId(assignedBusinessTerm.getBieId());
        request.setTypeCode(assignedBusinessTerm.getTypeCode());
        request.setPrimary(assignedBusinessTerm.isPrimary());

        UpdateBusinessTermAssignmentResponse response =
                businessTermService.updateBusinessTermAssignment(request);

        if (response.getAssignedBizTermId() != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/business_terms/assign/{type}/{id:[\\d]+}", method = RequestMethod.DELETE)
    public ResponseEntity deleteAssignment(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger assignedBizTermId,
            @PathVariable("type") String bieType) {

        BieToAssign assToDelete = new BieToAssign(assignedBizTermId, bieType);
        DeleteAssignedBusinessTermRequest request =
                new DeleteAssignedBusinessTermRequest(authenticationService.asScoreUser(requester))
                        .withAssignedBtList(Arrays.asList(assToDelete));

        DeleteAssignedBusinessTermResponse response =
                businessTermService.deleteBusinessTermAssignment(request);

        if (response.contains(assToDelete)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    public static class DeleteBusinessTermAssignmentRequestData {
        private List<BieToAssign> assignedBtList = Collections.emptyList();

        public List<BieToAssign> getAssignedBtList() {
            return assignedBtList;
        }

        public void setAssignedBtList(List<BieToAssign> assignedBtList) {
            this.assignedBtList = assignedBtList;
        }
    }

    @RequestMapping(value = "/business_terms/assign/delete", method = RequestMethod.POST)
    public ResponseEntity deleteAssignments(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody DeleteBusinessTermAssignmentRequestData requestData) {
        DeleteAssignedBusinessTermRequest request =
                new DeleteAssignedBusinessTermRequest(authenticationService.asScoreUser(requester))
                        .withAssignedBtList(requestData.getAssignedBtList());

        DeleteAssignedBusinessTermResponse response = businessTermService.deleteBusinessTermAssignment(request);

        if (response.containsAll(requestData.getAssignedBtList())) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

}
