package org.oagi.score.gateway.http.api.context_management.controller;

import org.oagi.score.repo.api.businesscontext.model.*;
import org.oagi.score.service.authentication.AuthenticationService;
import org.oagi.score.service.businesscontext.ContextSchemeService;
import org.oagi.score.service.common.data.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
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

import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.base.SortDirection.DESC;

@RestController
public class ContextSchemeController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ContextSchemeService contextSchemeService;

    @RequestMapping(value = "/context_schemes", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<ContextScheme> getContextSchemeList(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "updaterUsernameList", required = false) String updaterUsernameList,
            @RequestParam(name = "updateStart", required = false) String updateStart,
            @RequestParam(name = "updateEnd", required = false) String updateEnd,
            @RequestParam(name = "sortActive") String sortActive,
            @RequestParam(name = "sortDirection") String sortDirection,
            @RequestParam(name = "pageIndex") int pageIndex,
            @RequestParam(name = "pageSize") int pageSize) {

        GetContextSchemeListRequest request = new GetContextSchemeListRequest(
                authenticationService.asScoreUser(requester));

        request.setSchemeName(name);
        request.setUpdaterUsernameList(!StringUtils.hasLength(updaterUsernameList) ? Collections.emptyList() :
                Arrays.asList(updaterUsernameList.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList()));
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

        GetContextSchemeListResponse response = contextSchemeService.getContextSchemeList(request);

        PageResponse<ContextScheme> pageResponse = new PageResponse<>();
        pageResponse.setList(response.getResults());
        pageResponse.setPage(response.getPage());
        pageResponse.setSize(response.getSize());
        pageResponse.setLength(response.getLength());
        return pageResponse;
    }

    @RequestMapping(value = "/context_scheme_values", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<ContextSchemeValue> getContextSchemeValueList(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestParam(name = "value", required = false) String value,
            @RequestParam(name = "sortActive") String sortActive,
            @RequestParam(name = "sortDirection") String sortDirection,
            @RequestParam(name = "pageIndex") int pageIndex,
            @RequestParam(name = "pageSize") int pageSize) {

        GetContextSchemeValueListRequest request = new GetContextSchemeValueListRequest(
                authenticationService.asScoreUser(requester));

        request.setValue(value);

        request.setPageIndex(pageIndex);
        request.setPageSize(pageSize);
        request.setSortActive(sortActive);
        request.setSortDirection("asc".equalsIgnoreCase(sortDirection) ? ASC : DESC);

        GetContextSchemeValueListResponse response = contextSchemeService.getContextSchemeValueList(request);

        PageResponse<ContextSchemeValue> pageResponse = new PageResponse<>();
        pageResponse.setList(response.getResults());
        pageResponse.setPage(response.getPage());
        pageResponse.setSize(response.getSize());
        pageResponse.setLength(response.getLength());
        return pageResponse;
    }

    @RequestMapping(value = "/context_scheme/{id:[\\d]+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ContextScheme getContextScheme(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger contextSchemeId) {

        GetContextSchemeRequest request = new GetContextSchemeRequest(
                authenticationService.asScoreUser(requester));
        request.setContextSchemeId(contextSchemeId);

        GetContextSchemeResponse response =
                contextSchemeService.getContextScheme(request);
        return response.getContextScheme();
    }

    @RequestMapping(value = "/simple_context_schemes", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ContextScheme> getSimpleContextSchemeList(
            @AuthenticationPrincipal AuthenticatedPrincipal requester) {

        GetContextSchemeListRequest request = new GetContextSchemeListRequest(
                authenticationService.asScoreUser(requester));
        request.setPageIndex(-1);
        request.setPageSize(-1);

        GetContextSchemeListResponse response = contextSchemeService.getContextSchemeList(request);
        return response.getResults();
    }

    @RequestMapping(value = "/context_category/{id:[\\d]+}/simple_context_schemes", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ContextScheme> getSimpleContextSchemeList(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger contextCategoryId) {

        GetContextSchemeListRequest request = new GetContextSchemeListRequest(
                authenticationService.asScoreUser(requester));
        request.setContextCategoryIdList(Arrays.asList(contextCategoryId));
        request.setPageIndex(-1);
        request.setPageSize(-1);

        GetContextSchemeListResponse response = contextSchemeService.getContextSchemeList(request);
        return response.getResults();
    }

    @RequestMapping(value = "/context_scheme", method = RequestMethod.PUT)
    public ResponseEntity create(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody ContextScheme contextScheme) {

        CreateContextSchemeRequest request =
                new CreateContextSchemeRequest(authenticationService.asScoreUser(requester));
        request.setContextCategoryId(contextScheme.getContextCategoryId());
        request.setSchemeId(contextScheme.getSchemeId());
        request.setSchemeName(contextScheme.getSchemeName());
        request.setSchemeAgencyId(contextScheme.getSchemeAgencyId());
        request.setSchemeVersionId(contextScheme.getSchemeVersionId());
        request.setDescription(contextScheme.getDescription());
        request.setContextSchemeValueList(contextScheme.getContextSchemeValueList());

        CreateContextSchemeResponse response =
                contextSchemeService.createContextScheme(request);

        if (response.getContextSchemeId() != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/context_scheme/{id:[\\d]+}", method = RequestMethod.POST)
    public ResponseEntity update(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger contextSchemeId,
            @RequestBody ContextScheme contextScheme) {

        UpdateContextSchemeRequest request =
                new UpdateContextSchemeRequest(authenticationService.asScoreUser(requester))
                        .withContextSchemeId(contextSchemeId)
                        .withContextCategoryId(contextScheme.getContextCategoryId())
                        .withCodeListId(contextScheme.getCodeListId())
                        .withSchemeId(contextScheme.getSchemeId())
                        .withSchemeName(contextScheme.getSchemeName())
                        .withSchemeAgencyId(contextScheme.getSchemeAgencyId())
                        .withSchemeVersionId(contextScheme.getSchemeVersionId())
                        .withDescription(contextScheme.getDescription())
                        .withContextSchemeValueList(contextScheme.getContextSchemeValueList());

        UpdateContextSchemeResponse response =
                contextSchemeService.updateContextScheme(request);

        if (response.getContextSchemeId() != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/context_scheme/{id:[\\d]+}", method = RequestMethod.DELETE)
    public ResponseEntity delete(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger contextSchemeId) {

        DeleteContextSchemeRequest request =
                new DeleteContextSchemeRequest(authenticationService.asScoreUser(requester))
                        .withContextSchemeId(contextSchemeId);

        DeleteContextSchemeResponse response =
                contextSchemeService.deleteContextScheme(request);

        if (response.contains(contextSchemeId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    public static class DeleteContextSchemeRequestData {
        private List<BigInteger> contextSchemeIdList = Collections.emptyList();

        public List<BigInteger> getContextSchemeIdList() {
            return contextSchemeIdList;
        }

        public void setContextSchemeIdList(List<BigInteger> contextSchemeIdList) {
            this.contextSchemeIdList = contextSchemeIdList;
        }
    }

    @RequestMapping(value = "/context_scheme/delete", method = RequestMethod.POST)
    public ResponseEntity deletes(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody DeleteContextSchemeRequestData requestData) {
        DeleteContextSchemeRequest request =
                new DeleteContextSchemeRequest(authenticationService.asScoreUser(requester))
                        .withContextSchemeIdList(requestData.getContextSchemeIdList());

        DeleteContextSchemeResponse response =
                contextSchemeService.deleteContextScheme(request);

        if (response.containsAll(requestData.getContextSchemeIdList())) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/context_scheme/check_uniqueness", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean checkUniqueness(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody ContextScheme contextScheme) {
        return contextSchemeService.hasSameCtxScheme(contextScheme);
    }

    @RequestMapping(value = "/context_scheme/check_name_uniqueness", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean checkNameUniqueness(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody ContextScheme contextScheme) {
        return contextSchemeService.hasSameCtxSchemeName(contextScheme);
    }

    @RequestMapping(value = "/context_scheme/{id:[\\d]+}/simple_context_scheme_values", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<ContextSchemeValue> getSimpleContextSchemeValueList(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger contextSchemeId) {

        GetContextSchemeRequest request = new GetContextSchemeRequest(
                authenticationService.asScoreUser(requester));
        request.setContextSchemeId(contextSchemeId);

        GetContextSchemeResponse response =
                contextSchemeService.getContextScheme(request);
        return response.getContextScheme().getContextSchemeValueList();
    }
}
