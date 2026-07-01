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

    @PostMapping()
    public BusinessTermCreateResponse create(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody BusinessTermCreateRequest request) {

        var businessTermId = businessTermCommandService.create(sessionService.asScoreUser(user), request);
        return new BusinessTermCreateResponse(businessTermId);
    }

    /**
     * Parses an uploaded CSV/TSV/XLSX file into headers + rows for the import dialog's column-mapping
     * and preview steps. Nothing is persisted here — the user confirms the selected rows separately
     * via {@link #importBatch}.
     */
    @PostMapping(value = "/parse")
    public BusinessTermParseResult parse(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "sheet", required = false) String sheet) throws IOException {

        return businessTermCommandService.parse(
                sessionService.asScoreUser(user), file.getOriginalFilename(), file.getInputStream(), sheet);
    }

    /**
     * Imports the rows the user selected (and possibly inline-edited) in the import dialog. This is a
     * best-effort upsert keyed by external reference URI: good rows are committed and bad rows are
     * reported per row, so the response always carries a created/updated/failed breakdown rather than
     * aborting the whole import on the first bad row.
     */
    @PostMapping(value = "/batch")
    public BusinessTermBatchImportResult importBatch(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody BusinessTermBatchImportRequest request) {

        return businessTermCommandService.createBatch(sessionService.asScoreUser(user), request.rows());
    }

    @PutMapping(value = "/{businessTermId:[\\d]+}")
    public ResponseEntity update(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("businessTermId") BusinessTermId businessTermId,
            @RequestBody BusinessTermUpdateRequest request) {

        // #1753 - L2: the path id is authoritative; reject a body that targets a different id.
        if (request.businessTermId() == null || !businessTermId.equals(request.businessTermId())) {
            throw new IllegalArgumentException(
                    "The business term id in the path does not match the request body.");
        }

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

        // #1752 - M9: discard the whole batch in a single transaction so a mid-batch failure
        // (e.g. a term in use) rolls back rather than leaving partial deletions.
        businessTermCommandService.discard(sessionService.asScoreUser(user), request.businessTermIdList());

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
