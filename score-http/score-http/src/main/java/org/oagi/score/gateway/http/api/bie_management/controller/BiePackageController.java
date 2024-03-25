package org.oagi.score.gateway.http.api.bie_management.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.oagi.score.gateway.http.api.bie_management.data.*;
import org.oagi.score.gateway.http.api.bie_management.service.BiePackageService;
import org.oagi.score.gateway.http.api.mail.data.SendMailRequest;
import org.oagi.score.gateway.http.api.mail.service.MailService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.base.SortDirection;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.service.common.data.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.impl.utils.StringUtils.hasLength;

@RestController
public class BiePackageController {

    @Autowired
    private BiePackageService service;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private MailService mailService;

    @RequestMapping(value = "/bie_packages",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<BiePackage> getBiePackageList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                      @RequestParam(name = "versionId", required = false) String versionId,
                                                      @RequestParam(name = "versionName", required = false) String versionName,
                                                      @RequestParam(name = "description", required = false) String description,
                                                      @RequestParam(name = "den", required = false) String den,
                                                      @RequestParam(name = "businessTerm", required = false) String businessTerm,
                                                      @RequestParam(name = "version", required = false) String version,
                                                      @RequestParam(name = "remark", required = false) String remark,
                                                      @RequestParam(name = "states", required = false) String states,
                                                      @RequestParam(name = "biePackageIds", required = false) String biePackageIds,
                                                      @RequestParam(name = "releaseIds", required = false) String releaseIds,
                                                      @RequestParam(name = "ownerLoginIds", required = false) String ownerLoginIds,
                                                      @RequestParam(name = "updaterLoginIds", required = false) String updaterLoginIds,
                                                      @RequestParam(name = "updateStart", required = false) String updateStart,
                                                      @RequestParam(name = "updateEnd", required = false) String updateEnd,
                                                      @RequestParam(name = "sortActives") String sortActives,
                                                      @RequestParam(name = "sortDirections") String sortDirections,
                                                      @RequestParam(name = "pageIndex") int pageIndex,
                                                      @RequestParam(name = "pageSize") int pageSize) {

        BiePackageListRequest request = new BiePackageListRequest(sessionService.asScoreUser(user));

        request.setVersionId(versionId);
        request.setVersionName(versionName);
        request.setDescription(description);
        request.setDen(den);
        request.setBusinessTerm(businessTerm);
        request.setVersion(version);
        request.setRemark(remark);
        request.setStates(StringUtils.hasLength(states) ?
                Arrays.asList(states.split(",")).stream()
                        .map(e -> BieState.valueOf(e)).collect(Collectors.toList()) : Collections.emptyList());
        request.setBiePackageIds(!StringUtils.hasLength(biePackageIds) ? Collections.emptyList() :
                Arrays.asList(biePackageIds.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).map(e -> new BigInteger(e)).collect(Collectors.toList()));
        request.setReleaseIds(!StringUtils.hasLength(releaseIds) ? Collections.emptyList() :
                Arrays.asList(releaseIds.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).map(e -> new BigInteger(e)).collect(Collectors.toList()));
        request.setOwnerLoginIds(!StringUtils.hasLength(ownerLoginIds) ? Collections.emptyList() :
                Arrays.asList(ownerLoginIds.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList()));
        request.setUpdaterLoginIds(!StringUtils.hasLength(updaterLoginIds) ? Collections.emptyList() :
                Arrays.asList(updaterLoginIds.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList()));

        if (StringUtils.hasLength(updateStart)) {
            request.setUpdateStartDate(new Date(Long.valueOf(updateStart)));
        }
        if (StringUtils.hasLength(updateEnd)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf(updateEnd));
            calendar.add(Calendar.DATE, 1);
            request.setUpdateEndDate(calendar.getTime());
        }

        request.setPageIndex(pageIndex);
        request.setPageSize(pageSize);
        request.setSortActives(!hasLength(sortActives) ? Collections.emptyList() :
                Arrays.asList(sortActives.split(",")).stream().map(e -> e.trim()).filter(e -> hasLength(e)).collect(Collectors.toList()));
        request.setSortDirections(!hasLength(sortDirections) ? Collections.emptyList() :
                Arrays.asList(sortDirections.split(",")).stream().map(e -> e.trim()).filter(e -> hasLength(e)).map(e -> SortDirection.valueOf(e.toUpperCase())).collect(Collectors.toList()));

        return service.getBiePackageList(request);
    }

    @RequestMapping(value = "/bie_packages", method = RequestMethod.POST)
    public CreateBiePackageResponse createBiePackage(@AuthenticationPrincipal AuthenticatedPrincipal user)
            throws ScoreDataAccessException {

        CreateBiePackageRequest request = new CreateBiePackageRequest(sessionService.asScoreUser(user));
        return service.createBiePackage(request);
    }

    @RequestMapping(value = "/bie_packages/{id:\\d+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BiePackage getBiePackage(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                    @PathVariable("id") BigInteger biePackageId) throws ScoreDataAccessException {

        return service.getBiePackageById(sessionService.asScoreUser(user), biePackageId);
    }

    @RequestMapping(value = "/bie_packages/{id:\\d+}", method = RequestMethod.POST)
    public ResponseEntity updateBiePackage(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                           @PathVariable("id") BigInteger biePackageId,
                                           @RequestBody UpdateBiePackageRequest request) throws ScoreDataAccessException {

        request.setRequester(sessionService.asScoreUser(user));
        request.setBiePackageId(biePackageId);

        if (request.getState() != null) {
            service.updateBiePackageState(request);
        } else {
            service.updateBiePackage(request);
        }

        return ResponseEntity.accepted().build();
    }

    @RequestMapping(value = "/bie_packages/{id:\\d+}", method = RequestMethod.DELETE)
    public ResponseEntity deleteBiePackage(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                           @PathVariable("id") BigInteger biePackageId) throws ScoreDataAccessException {

        DeleteBiePackageRequest request = new DeleteBiePackageRequest(sessionService.asScoreUser(user));
        request.setBiePackageId(biePackageId);

        service.deleteBiePackage(request);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/bie_packages", method = RequestMethod.DELETE)
    public ResponseEntity deleteBiePackage(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                           @RequestBody DeleteBiePackageRequest request) throws ScoreDataAccessException {

        request.setRequester(sessionService.asScoreUser(user));

        service.deleteBiePackage(request);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/bie_packages/{id:[\\d]+}/transfer_ownership", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity transferOwnership(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                            @PathVariable("id") BigInteger biePackageId,
                                            @RequestParam(value = "sendNotification", required = false) Boolean sendNotification,
                                            @RequestBody Map<String, Object> requestBody) {
        String targetLoginId = (String) requestBody.get("targetLoginId");

        BieOwnershipTransferRequest request = new BieOwnershipTransferRequest();
        request.setRequester(sessionService.asScoreUser(user));
        request.setTargetUser(sessionService.getScoreUserByUsername(targetLoginId));
        request.setBiePackageId(biePackageId);

        service.transferOwnership(request);

        if (sendNotification != null && sendNotification) {
            SendMailRequest sendMailRequest = new SendMailRequest();
            sendMailRequest.setRecipient(sessionService.getScoreUserByUsername(targetLoginId));
            sendMailRequest.setTemplateName("bie-package-ownership-transfer-acceptance");
            sendMailRequest.setParameters((Map<String, Object>) requestBody.get("parameters"));
            mailService.sendMail(sessionService.asScoreUser(user), sendMailRequest);
        }

        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/bie_packages/{id:\\d+}/bie_list",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<BieList> getBieListInBiePackage(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                        @PathVariable("id") BigInteger biePackageId,
                                                        @RequestParam(name = "sortActives") String sortActives,
                                                        @RequestParam(name = "sortDirections") String sortDirections,
                                                        @RequestParam(name = "pageIndex") int pageIndex,
                                                        @RequestParam(name = "pageSize") int pageSize) {

        BieListInBiePackageRequest request = new BieListInBiePackageRequest(sessionService.asScoreUser(user));
        request.setBiePackageId(biePackageId);

        request.setPageIndex(pageIndex);
        request.setPageSize(pageSize);
        request.setSortActives(!hasLength(sortActives) ? Collections.emptyList() :
                Arrays.asList(sortActives.split(",")).stream().map(e -> e.trim()).filter(e -> hasLength(e)).collect(Collectors.toList()));
        request.setSortDirections(!hasLength(sortDirections) ? Collections.emptyList() :
                Arrays.asList(sortDirections.split(",")).stream().map(e -> e.trim()).filter(e -> hasLength(e)).map(e -> SortDirection.valueOf(e.toUpperCase())).collect(Collectors.toList()));

        return service.getBieListInBiePackage(request);
    }

    @RequestMapping(value = "/bie_packages/{id:\\d+}/bie", method = RequestMethod.POST)
    public ResponseEntity addBieToBiePackage(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                             @PathVariable("id") BigInteger biePackageId,
                                             @RequestBody AddBieToBiePackageRequest request) throws ScoreDataAccessException {

        request.setRequester(sessionService.asScoreUser(user));
        request.setBiePackageId(biePackageId);

        service.addBieToBiePackage(request);

        return ResponseEntity.accepted().build();
    }

    @RequestMapping(value = "/bie_packages/{id:\\d+}/bie/{topLevelAsbiepId:\\d+}", method = RequestMethod.DELETE)
    public ResponseEntity deleteBieInBiePackage(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                @PathVariable("id") BigInteger biePackageId,
                                                @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId) throws ScoreDataAccessException {

        DeleteBieInBiePackageRequest request = new DeleteBieInBiePackageRequest(sessionService.asScoreUser(user));
        request.setBiePackageId(biePackageId);
        request.setTopLevelAsbiepIdList(Arrays.asList(topLevelAsbiepId));

        service.deleteBieInBiePackage(request);
        return ResponseEntity.noContent().build();

    }

    @RequestMapping(value = "/bie_packages/{id:\\d+}/bie", method = RequestMethod.DELETE)
    public ResponseEntity deleteBieInBiePackage(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                @PathVariable("id") BigInteger biePackageId,
                                                @RequestBody DeleteBieInBiePackageRequest request) throws ScoreDataAccessException {
        request.setRequester(sessionService.asScoreUser(user));
        request.setBiePackageId(biePackageId);

        service.deleteBieInBiePackage(request);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/bie_packages/{id:[\\d]+}/generate", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> generate(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                        @PathVariable("id") BigInteger biePackageId,
                                                        @RequestParam(name = "topLevelAsbiepIdList") String topLevelAsbiepIdList,
                                                        @RequestParam(name = "schemaExpression") String schemaExpression,
                                                        HttpServletRequest httpServletRequest) throws IOException {

        GenerateBiePackageRequest request = new GenerateBiePackageRequest(sessionService.asScoreUser(user));
        request.setBiePackageId(biePackageId);
        request.setTopLevelAsbiepIdList(!StringUtils.hasLength(topLevelAsbiepIdList) ? Collections.emptyList() :
                Arrays.asList(topLevelAsbiepIdList.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).map(e -> new BigInteger(e)).collect(Collectors.toList()));
        request.setSchemaExpression(schemaExpression);

        GenerateBiePackageResponse response = service.generate(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(response.getContentType()))
                .contentLength(response.getFile().length())
                .body(new InputStreamResource(new FileInputStream(response.getFile())));
    }
}
