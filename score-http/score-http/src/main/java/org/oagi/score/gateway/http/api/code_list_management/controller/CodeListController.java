package org.oagi.score.gateway.http.api.code_list_management.controller;

import org.oagi.score.gateway.http.api.cc_management.data.CcCreateResponse;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.gateway.http.api.code_list_management.data.*;
import org.oagi.score.gateway.http.api.code_list_management.service.CodeListService;
import org.oagi.score.service.common.data.AccessPrivilege;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.service.log.LogRepository;
import org.oagi.score.service.codelist.CodeListUpliftingService;
import org.oagi.score.service.codelist.model.CodeListUpliftingRequest;
import org.oagi.score.service.codelist.model.CodeListUpliftingResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CodeListController {

    @Autowired
    private CodeListService service;

    @Autowired
    private CodeListUpliftingService upliftingService;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private SessionService sessionService;

    @RequestMapping(value = "/code_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<CodeListForList> getCodeLists(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam(name = "releaseId") long releaseId,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "definition", required = false) String definition,
            @RequestParam(name = "module", required = false) String module,
            @RequestParam(name = "access", required = false) String access,
            @RequestParam(name = "states", required = false) String states,
            @RequestParam(name = "deprecated", required = false) String deprecated,
            @RequestParam(name = "extensible", required = false) Boolean extensible,
            @RequestParam(name = "ownedByDeveloper", required = false) Boolean ownedByDeveloper,
            @RequestParam(name = "ownerLoginIds", required = false) String ownerLoginIds,
            @RequestParam(name = "updaterLoginIds", required = false) String updaterLoginIds,
            @RequestParam(name = "updateStart", required = false) String updateStart,
            @RequestParam(name = "updateEnd", required = false) String updateEnd,
            @RequestParam(name = "sortActive") String sortActive,
            @RequestParam(name = "sortDirection") String sortDirection,
            @RequestParam(name = "pageIndex") int pageIndex,
            @RequestParam(name = "pageSize") int pageSize) {

        CodeListForListRequest request = new CodeListForListRequest();

        request.setReleaseId(releaseId);
        request.setName(name);
        request.setDefinition(definition);
        request.setModule(module);
        request.setAccess(StringUtils.hasLength(access) ? AccessPrivilege.valueOf(access) : null);
        if (StringUtils.hasLength(states)) {
            List<String> stateStrings = Arrays.asList(states.split(",")).stream().collect(Collectors.toList());
            request.setStates(stateStrings.stream()
                    .map(e -> CcState.valueOf(e.trim())).collect(Collectors.toList()));
        }
        if (StringUtils.hasLength(deprecated)) {
            if ("true".equalsIgnoreCase(deprecated.toLowerCase())) {
                request.setDeprecated(true);
            } else if ("false".equalsIgnoreCase(deprecated.toLowerCase())) {
                request.setDeprecated(false);
            }
        }
        request.setExtensible(extensible);
        request.setOwnedByDeveloper(ownedByDeveloper);

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

        PageRequest pageRequest = new PageRequest();
        pageRequest.setSortActive(sortActive);
        pageRequest.setSortDirection(sortDirection);
        pageRequest.setPageIndex(pageIndex);
        pageRequest.setPageSize(pageSize);
        request.setPageRequest(pageRequest);

        return service.getCodeLists(user, request);
    }

    @RequestMapping(value = "/code_list/{manifestId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CodeList getCodeList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                @PathVariable("manifestId") BigInteger manifestId) {
        return service.getCodeList(user, manifestId);
    }

    @RequestMapping(value = "/code_list", method = RequestMethod.PUT)
    public CcCreateResponse create(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody CodeList codeList) {
        BigInteger manifestId = service.createCodeList(user, codeList);

        CcCreateResponse resp = new CcCreateResponse();
        resp.setManifestId(manifestId);
        return resp;
    }

    @RequestMapping(value = "/code_list/{manifestId}", method = RequestMethod.POST)
    public ResponseEntity update(
            @PathVariable("manifestId") BigInteger manifestId,
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody CodeList codeList) {
        codeList.setCodeListManifestId(manifestId);
        service.update(user, codeList);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/code_list/{manifestId}/revision", method = RequestMethod.POST)
    public ResponseEntity makeNewRevision(
            @PathVariable("manifestId") BigInteger manifestId,
            @AuthenticationPrincipal AuthenticatedPrincipal user) {

        service.makeNewRevision(user, manifestId);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/code_list/{manifestId}/revision", method = RequestMethod.GET)
    public CodeList getCodeListRevision(
            @PathVariable("manifestId") BigInteger manifestId,
            @AuthenticationPrincipal AuthenticatedPrincipal user) {
        return service.getCodeListRevision(user, manifestId);
    }

    @RequestMapping(value = "/code_list/{manifestId}/revision/cancel", method = RequestMethod.POST)
    public ResponseEntity cancelRevision(
            @PathVariable("manifestId") BigInteger manifestId,
            @AuthenticationPrincipal AuthenticatedPrincipal user) {

        service.cancelRevision(user, manifestId);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/code_list/delete", method = RequestMethod.POST)
    public ResponseEntity deleteCodeList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                  @RequestBody DeleteCodeListRequest request) {
        service.deleteCodeList(user, request.getCodeListManifestIds());
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/code_list/restore", method = RequestMethod.POST)
    public ResponseEntity restoreCodeList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                  @RequestBody DeleteCodeListRequest request) {
        service.restoreCodeList(user, request.getCodeListManifestIds());
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/code_list/check_uniqueness", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean checkUniqueness(
            @RequestParam(name = "releaseId") long releaseId,
            @RequestParam(name = "codeListManifestId", required = false) Long codeListManifestId,
            @RequestParam(name = "listId") String listId,
            @RequestParam(name = "agencyId") Long agencyId,
            @RequestParam(name = "versionId") String versionId) {

        SameCodeListParams params = new SameCodeListParams();
        params.setReleaseId(releaseId);
        params.setCodeListManifestId(codeListManifestId);
        params.setListId(listId);
        params.setAgencyId(agencyId);
        params.setVersionId(versionId);

        return service.hasSameCodeList(params);
    }

    @RequestMapping(value = "/code_list/check_name_uniqueness", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean checkNameUniqueness(
            @RequestParam(name = "releaseId") long releaseId,
            @RequestParam(name = "codeListManifestId", required = false) Long codeListManifestId,
            @RequestParam(name = "codeListName") String codeListName) {

        SameNameCodeListParams params = new SameNameCodeListParams();
        params.setReleaseId(releaseId);
        params.setCodeListManifestId(codeListManifestId);
        params.setCodeListName(codeListName);

        return service.hasSameNameCodeList(params);
    }

    @RequestMapping(value = "/code_list/{manifestId}/transfer_ownership",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity transferOwnership(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                            @PathVariable("manifestId") BigInteger manifestId,
                                            @RequestBody Map<String, String> request) {
        String targetLoginId = request.get("targetLoginId");
        service.transferOwnership(user, manifestId, targetLoginId);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/code_list/{manifestId}/uplift",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CodeListUpliftingResponse upliftCodeList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                    @PathVariable("manifestId") BigInteger manifestId,
                                                    @RequestBody CodeListUpliftingRequest request) {
        request.setRequester(sessionService.asScoreUser(user));
        request.setCodeListManifestId(manifestId);


        return upliftingService.upliftCodeList(request);
    }
}
