package org.oagi.score.gateway.http.api.business_term_management.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.business_term_management.controller.payload.*;
import org.oagi.score.gateway.http.api.business_term_management.model.AsbieBusinessTermId;
import org.oagi.score.gateway.http.api.business_term_management.model.BbieBusinessTermId;
import org.oagi.score.gateway.http.api.business_term_management.model.BusinessTermId;
import org.oagi.score.gateway.http.api.business_term_management.service.BusinessTermCommandService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
@Tag(name = "Business Term - Commands", description = "API for creating, updating, and deleting business terms")
@RequestMapping("/business-terms")
public class BusinessTermCommandController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private BusinessTermCommandService businessTermCommandService;

    @Autowired
    private SessionService sessionService;

    private static String DEFAULT_ALLOWED_CONTENT_TYPE = "text/csv";

    @PostMapping()
    public BusinessTermCreateResponse create(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody BusinessTermCreateRequest request) {

        var businessTermId = businessTermCommandService.create(sessionService.asScoreUser(user), request);
        return new BusinessTermCreateResponse(businessTermId);
    }

    @PostMapping(value = "/csv")
    public ResponseEntity createFromFile(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam("file") MultipartFile file) throws IOException {
        if (DEFAULT_ALLOWED_CONTENT_TYPE.equals(file.getContentType())) {

            List<BusinessTermId> businessTermIdList =
                    businessTermCommandService.create(sessionService.asScoreUser(user), file.getInputStream());
            if (businessTermIdList != null && !businessTermIdList.isEmpty()) {
                logger.debug("Uploaded the file successfully: " + file.getOriginalFilename() + " with created record IDs " + businessTermIdList);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(200).build();
            }
        } else {
            return ResponseEntity.status(415).body("Unsupported content type: " + file.getContentType());
        }
    }

    @PutMapping(value = "/{businessTermId:[\\d]+}")
    public ResponseEntity update(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody BusinessTermUpdateRequest request) {

        businessTermCommandService.update(sessionService.asScoreUser(user), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = "/{businessTermId:[\\d]+}")
    public ResponseEntity discard(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("businessTermId") BusinessTermId businessTermId) {

        businessTermCommandService.discard(sessionService.asScoreUser(user), businessTermId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping()
    public ResponseEntity discard(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody BusinessTermDiscardRequest request) {

        for (BusinessTermId businessTermId : request.businessTermIdList()) {
            businessTermCommandService.discard(sessionService.asScoreUser(user), businessTermId);
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{businessTermId:[\\d]+}/assign")
    public ResponseEntity assignBusinessTerm(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("businessTermId") BusinessTermId businessTermId,
            @RequestBody AssignBusinessTermRequest request) {

        List<BigInteger> assignedBusinessTermIdList =
                businessTermCommandService.assignBusinessTerm(sessionService.asScoreUser(user), businessTermId, request);
        if (assignedBusinessTermIdList != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping(value = "/assign/{type}/{id:[\\d]+}")
    public ResponseEntity updateAssignment(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("type") String bieType,
            @PathVariable("id") BigInteger assignedBizTermId,
            @RequestBody AssignedBusinessTermUpdateRequest request) {

        bieType = bieType.toUpperCase();

        boolean result;
        if ("ASBIE".equals(bieType)) {
            result = businessTermCommandService.updateAssignment(
                    sessionService.asScoreUser(user),
                    new AsbieBusinessTermId(assignedBizTermId), request);
        } else if ("BBIE".equals(bieType)) {
            result = businessTermCommandService.updateAssignment(
                    sessionService.asScoreUser(user),
                    new BbieBusinessTermId(assignedBizTermId), request);
        } else {
            throw new IllegalArgumentException("Unsupported assignment type: " + bieType);
        }

        if (result) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping(value = "/assign/{type}/{id:[\\d]+}")
    public ResponseEntity deleteAssignment(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("id") BigInteger assignedBizTermId,
            @PathVariable("type") String bieType) {

        bieType = bieType.toUpperCase();

        businessTermCommandService.deleteBusinessTermAssignment(sessionService.asScoreUser(user),
                new AssignedBusinessTermDeleteRequest(
                        ("ASBIE".equals(bieType)) ? Arrays.asList(new AsbieBusinessTermId(assignedBizTermId)) : Collections.emptyList(),
                        ("BBIE".equals(bieType)) ? Arrays.asList(new BbieBusinessTermId(assignedBizTermId)) : Collections.emptyList()
                ));

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = "/assign")
    public ResponseEntity deleteAssignments(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody AssignedBusinessTermDeleteRequest request) {

        businessTermCommandService.deleteBusinessTermAssignment(sessionService.asScoreUser(user), request);

        return ResponseEntity.noContent().build();
    }

}
