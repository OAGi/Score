package org.oagi.score.gateway.http.api.release_management.controller;

import org.oagi.score.gateway.http.api.release_management.data.*;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.helper.DeleteOnCloseFileSystemResource;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ReleaseController {

    @Autowired
    private ReleaseService service;

    @Autowired
    private SessionService sessionService;

    @RequestMapping(value = "/simple_releases", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SimpleRelease> getSimpleReleases(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                 @RequestParam(name = "states", required = false) String states) {
        SimpleReleasesRequest request = new SimpleReleasesRequest(user);

        request.setStates(StringUtils.hasLength(states) ?
                Arrays.asList(states.split(",")).stream()
                        .map(e -> ReleaseState.valueOf(e)).collect(Collectors.toList()) : Collections.emptyList());

        return service.getSimpleReleases(request);
    }

    @RequestMapping(value = "/simple_release/{id:[\\d]+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public SimpleRelease getSimpleRelease(@PathVariable("id") BigInteger releaseId) {
        return service.getSimpleReleaseByReleaseId(releaseId);
    }

    @RequestMapping(value = "/release_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ReleaseList> getReleaseList(@AuthenticationPrincipal AuthenticatedPrincipal user) {
        return service.getReleaseList(user);
    }

    @RequestMapping(value = "/releases",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<ReleaseList> getReleases(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                 @RequestParam(name = "releaseNum", required = false) String releaseNum,
                                                 @RequestParam(name = "states", required = false) String states,
                                                 @RequestParam(name = "excludes", required = false) String excludes,
                                                 @RequestParam(name = "namespaces", required = false) String namespaces,
                                                 @RequestParam(name = "creatorLoginIds", required = false) String creatorLoginIds,
                                                 @RequestParam(name = "createStart", required = false) String createStart,
                                                 @RequestParam(name = "createEnd", required = false) String createEnd,
                                                 @RequestParam(name = "updaterLoginIds", required = false) String updaterLoginIds,
                                                 @RequestParam(name = "updateStart", required = false) String updateStart,
                                                 @RequestParam(name = "updateEnd", required = false) String updateEnd,
                                                 @RequestParam(name = "sortActive") String sortActive,
                                                 @RequestParam(name = "sortDirection") String sortDirection,
                                                 @RequestParam(name = "pageIndex") int pageIndex,
                                                 @RequestParam(name = "pageSize") int pageSize) {

        ReleaseListRequest request = new ReleaseListRequest();

        request.setReleaseNum(releaseNum);
        request.setStates(StringUtils.hasLength(states) ?
                Arrays.asList(states.split(",")).stream()
                        .map(e -> ReleaseState.valueOf(e)).collect(Collectors.toList()) : Collections.emptyList());
        request.setExcludes(!StringUtils.hasLength(excludes) ? Collections.emptyList() :
                Arrays.asList(excludes.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList()));
        request.setNamespaces(!StringUtils.hasLength(namespaces) ? Collections.emptyList() :
                Arrays.asList(namespaces.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e))
                        .map(e -> new BigInteger(e)).collect(Collectors.toList()));
        request.setCreatorLoginIds(!StringUtils.hasLength(creatorLoginIds) ? Collections.emptyList() :
                Arrays.asList(creatorLoginIds.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList()));

        if (StringUtils.hasLength(createStart)) {
            request.setCreateStartDate(new Date(Long.valueOf(createStart)));
        }
        if (StringUtils.hasLength(createEnd)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf(createEnd));
            calendar.add(Calendar.DATE, 1);
            request.setCreateEndDate(calendar.getTime());
        }
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

        PageRequest pageRequest = new PageRequest();
        pageRequest.setSortActive(sortActive);
        pageRequest.setSortDirection(sortDirection);
        pageRequest.setPageIndex(pageIndex);
        pageRequest.setPageSize(pageSize);
        request.setPageRequest(pageRequest);
        return service.getReleases(user, request);
    }

    @RequestMapping(value = "/release/create", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ReleaseResponse createRelease(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                         @RequestBody ReleaseDetail releaseDetail) {
        return service.createRelease(user, releaseDetail);
    }

    @RequestMapping(value = "/release/{id:[\\d]+}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateRelease(@AuthenticationPrincipal AuthenticatedPrincipal user,
                              @PathVariable("id") BigInteger releaseId,
                              @RequestBody ReleaseDetail releaseDetail) {
        releaseDetail.setReleaseId(releaseId);
        service.updateRelease(user, releaseDetail);
    }

    @RequestMapping(value = "/release/{id:[\\d]+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ReleaseDetail getReleaseDetail(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                          @PathVariable("id") BigInteger releaseId) {
        return service.getReleaseDetail(user, releaseId);
    }

    @RequestMapping(value = "/release/{id:[\\d]+}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void discard(@AuthenticationPrincipal AuthenticatedPrincipal user,
                        @PathVariable("id") BigInteger releaseId) {
        service.discard(user, Arrays.asList(releaseId));
    }

    @RequestMapping(value = "/release", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void discard(@AuthenticationPrincipal AuthenticatedPrincipal user,
                        @RequestParam(name = "releaseIds") String releaseIds) {
        service.discard(user, Arrays.asList(releaseIds.split(",")).stream()
                .map(e -> new BigInteger(e)).collect(Collectors.toList()));
    }

    @RequestMapping(value = "/release/{id:[\\d]+}/assignable", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AssignComponents assignComponents(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                             @PathVariable("id") BigInteger releaseId) {
        return service.getAssignComponents(releaseId);
    }

    @RequestMapping(value = "/release/{id:[\\d]+}/state", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void transitState(@AuthenticationPrincipal AuthenticatedPrincipal user,
                             @PathVariable("id") BigInteger releaseId,
                             @RequestBody TransitStateRequest request) {
        request.setReleaseId(releaseId);
        service.transitState(user, request);
    }

    @RequestMapping(value = "/release/validate", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ReleaseValidationResponse validate(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                              @RequestBody ReleaseValidationRequest request) {
        return service.validate(user, request);
    }

    @RequestMapping(value = "/release/{id:[\\d]+}/draft", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ReleaseValidationResponse createDraft(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                 @PathVariable("id") BigInteger releaseId,
                                                 @RequestBody ReleaseValidationRequest request) {
        return service.createDraft(user, releaseId, request);
    }

    @RequestMapping(value = "/release/{id:[\\d]+}/generate_migration_script", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<DeleteOnCloseFileSystemResource> generateMigrationScript(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("id") BigInteger releaseId) throws Exception {

        GenerateMigrationScriptResponse response =
                service.generateMigrationScript(sessionService.asScoreUser(user), releaseId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.getFilename() + "\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .contentLength(response.getFile().length())
                .body(new DeleteOnCloseFileSystemResource(response.getFile()));
    }


}
