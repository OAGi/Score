package org.oagi.score.gateway.http.api.agency_id_management.controller;

import org.oagi.score.gateway.http.api.agency_id_management.data.*;
import org.oagi.score.gateway.http.api.agency_id_management.service.AgencyIdService;
import org.oagi.score.gateway.http.api.cc_management.data.CcCreateResponse;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.agency.model.AgencyIdList;
import org.oagi.score.repo.api.agency.model.GetAgencyIdListListRequest;
import org.oagi.score.repo.api.agency.model.GetAgencyIdListListResponse;
import org.oagi.score.repo.api.base.SortDirection;
import org.oagi.score.repo.api.corecomponent.model.CcState;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class AgencyIdController {

    @Autowired
    private AgencyIdService service;

    @Autowired
    private SessionService sessionService;

    @RequestMapping(value = "/simple_agency_id_list_values/{releaseId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public GetSimpleAgencyIdListValuesResponse getSimpleAgencyIdListValues(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("releaseId") BigInteger releaseId) {
        return service.getSimpleAgencyIdListValues(sessionService.asScoreUser(user), releaseId);
    }

    @RequestMapping(value = "/agency_id_list/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AgencyIdList getAgencyIdListDetail(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                              @PathVariable("id") BigInteger manifestId) {
        return service.getAgencyIdListDetail(sessionService.asScoreUser(user), manifestId);
    }

    @RequestMapping(value = "/agency_id_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public GetAgencyIdListListResponse getAgencyIdListList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                           @RequestParam(name = "states", required = false) String states,
                                                           @RequestParam(name = "name", required = false) String name,
                                                           @RequestParam(name = "definition", required = false) String definition,
                                                           @RequestParam(name = "module", required = false) String module,
                                                           @RequestParam(name = "deprecated", required = false) String deprecated,
                                                           @RequestParam(name = "ownerLoginIds", required = false) String ownerLoginIds,
                                                           @RequestParam(name = "updaterLoginIds", required = false) String updaterLoginIds,
                                                           @RequestParam(name = "updateStart", required = false) String updateStart,
                                                           @RequestParam(name = "updateEnd", required = false) String updateEnd,
                                                           @RequestParam(name = "releaseId", required = true) BigInteger releaseId,
                                                           @RequestParam(name = "sortActive") String sortActive,
                                                           @RequestParam(name = "sortDirection") String sortDirection,
                                                           @RequestParam(name = "pageIndex") int pageIndex,
                                                           @RequestParam(name = "pageSize") int pageSize) {
        GetAgencyIdListListRequest request = new GetAgencyIdListListRequest(sessionService.asScoreUser(user));
        request.setReleaseId(releaseId);
        request.setName(name);
        request.setDefinition(definition);
        request.setModule(module);
        if (deprecated != null) {
            request.setDeprecated(deprecated.equals("true"));
        }

        request.setStates(StringUtils.hasLength(states) ?
                Arrays.asList(states.split(",")).stream()
                        .map(e -> CcState.valueOf(e)).collect(Collectors.toList()) : Collections.emptyList());
        request.setOwnerLoginIds(!StringUtils.hasLength(ownerLoginIds) ? Collections.emptyList() :
                Arrays.asList(ownerLoginIds.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList()));
        request.setUpdaterLoginIds(!StringUtils.hasLength(updaterLoginIds) ? Collections.emptyList() :
                Arrays.asList(updaterLoginIds.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList()));

        if (StringUtils.hasLength(updateStart)) {
            request.setUpdateStartDate(new Timestamp(Long.valueOf(updateStart)).toLocalDateTime());
        }
        if (StringUtils.hasLength(updateEnd)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf(updateEnd));
            calendar.add(Calendar.DATE, 1);
            request.setUpdateEndDate(new Timestamp(calendar.getTimeInMillis()).toLocalDateTime());
        }

        request.setSortActive(sortActive);
        request.setSortDirection(SortDirection.valueOf(sortDirection.toUpperCase()));
        request.setPageIndex(pageIndex);
        request.setPageSize(pageSize);
        return service.getAgencyIdListList(request);
    }

    @RequestMapping(value = "/agency_id_list", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcCreateResponse createAgencyIdList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                               @RequestBody CreateAgencyIdListRequest request) {
        CcCreateResponse ccCreateResponse = new CcCreateResponse();
        ccCreateResponse.setManifestId(
                service.createAgencyIdList(sessionService.asScoreUser(user), request));
        return  ccCreateResponse;
    }

    @RequestMapping(value = "/agency_id_list/delete", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteAgencyIdLists(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                               @RequestBody UpdateAgencyIdListListRequest request) {
        ScoreUser requester = sessionService.asScoreUser(user);
        for (BigInteger agencyIdListManifestId : request.getAgencyIdListManifestIds()) {
            service.updateAgencyIdListState(requester, agencyIdListManifestId, CcState.Deleted);
        }
    }

    @RequestMapping(value = "/agency_id_list/restore", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void restoreAgencyIdLists(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                    @RequestBody UpdateAgencyIdListListRequest request) {
        ScoreUser requester = sessionService.asScoreUser(user);
        for (BigInteger agencyIdListManifestId : request.getAgencyIdListManifestIds()) {
            service.updateAgencyIdListState(requester, agencyIdListManifestId, CcState.WIP);
        }
    }

    @RequestMapping(value = "/agency_id_list/{manifestId}/transfer_ownership",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity transferOwnership(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                            @PathVariable("manifestId") BigInteger manifestId,
                                            @RequestBody Map<String, String> request) {
        String targetLoginId = request.get("targetLoginId");
        ScoreUser requester = sessionService.asScoreUser(user);
        service.transferOwnership(requester, manifestId, targetLoginId);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/agency_id_list/{manifestId}",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AgencyIdList updateAgencyIdList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                           @PathVariable("manifestId") BigInteger manifestId,
                                           @RequestBody AgencyIdList agencyIdList) {
        ScoreUser requester = sessionService.asScoreUser(user);
        return service.updateAgencyIdListProperty(requester, agencyIdList);
    }

    @RequestMapping(value = "/agency_id_list/{manifestId}/state",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateAgencyIdListState(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                           @PathVariable("manifestId") BigInteger manifestId,
                                           @RequestBody Map<String, String> request) {
        ScoreUser requester = sessionService.asScoreUser(user);
        String toState = request.get("toState");
        service.updateAgencyIdListState(requester, manifestId, CcState.valueOf(toState));
    }

    @RequestMapping(value = "/agency_id_list/{manifestId}/revision",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void reviseAgencyIdList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                        @PathVariable("manifestId") BigInteger manifestId) {
        ScoreUser requester = sessionService.asScoreUser(user);
        service.reviseAgencyIdList(requester, manifestId);
    }

    @RequestMapping(value = "/agency_id_list/{manifestId}/cancel",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void cancelAgencyIdList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                   @PathVariable("manifestId") BigInteger manifestId) {
        ScoreUser requester = sessionService.asScoreUser(user);
        service.cancelAgencyIdList(requester, manifestId);
    }

    @RequestMapping(value = "/agency_id_list/check_uniqueness", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean checkUniqueness(
            @RequestParam(name = "releaseId") long releaseId,
            @RequestParam(name = "agencyIdListManifestId", required = false) Long agencyIdListManifestId,
            @RequestParam(name = "listId") String listId,
            @RequestParam(name = "agencyIdListValueManifestId", required = false) Long agencyIdListValueManifestId,
            @RequestParam(name = "versionId") String versionId) {

        SameAgencyIdListParams params = new SameAgencyIdListParams();
        params.setReleaseId(releaseId);
        params.setAgencyIdListManifestId(agencyIdListManifestId);
        params.setAgencyIdListValueManifestId(agencyIdListValueManifestId);
        params.setListId(listId);
        params.setVersionId(versionId);

        return service.hasSameAgencyIdList(params);
    }

    @RequestMapping(value = "/agency_id_list/check_name_uniqueness", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean checkNameUniqueness(
            @RequestParam(name = "releaseId") long releaseId,
            @RequestParam(name = "agencyIdListManifestId", required = false) Long agencyIdListManifestId,
            @RequestParam(name = "agencyIdListName") String agencyIdListName) {

        SameNameAgencyIdListParams params = new SameNameAgencyIdListParams();
        params.setReleaseId(releaseId);
        params.setAgencyIdListManifestId(agencyIdListManifestId);
        params.setAgencyIdListName(agencyIdListName);

        return service.hasSameNameAgencyIdList(params);
    }
}
