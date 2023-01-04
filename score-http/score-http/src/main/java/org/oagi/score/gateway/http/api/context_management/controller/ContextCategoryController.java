package org.oagi.score.gateway.http.api.context_management.controller;

import org.oagi.score.repo.api.businesscontext.model.*;
import org.oagi.score.service.authentication.AuthenticationService;
import org.oagi.score.service.businesscontext.ContextCategoryService;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.base.SortDirection.DESC;

@RestController
public class ContextCategoryController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ContextCategoryService contextCategoryService;

    @RequestMapping(value = "/context_categories", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<ContextCategory> getContextCategoryList(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "description", required = false) String description,
            @RequestParam(name = "updaterUsernameList", required = false) String updaterUsernameList,
            @RequestParam(name = "updateStart", required = false) String updateStart,
            @RequestParam(name = "updateEnd", required = false) String updateEnd,
            @RequestParam(name = "sortActive") String sortActive,
            @RequestParam(name = "sortDirection") String sortDirection,
            @RequestParam(name = "pageIndex") int pageIndex,
            @RequestParam(name = "pageSize") int pageSize) {

        GetContextCategoryListRequest request = new GetContextCategoryListRequest(
                authenticationService.asScoreUser(requester));
        request.setName(name);
        request.setDescription(description);
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

        GetContextCategoryListResponse response = contextCategoryService.getContextCategoryList(request);

        PageResponse<ContextCategory> pageResponse = new PageResponse<>();
        pageResponse.setList(response.getResults());
        pageResponse.setPage(response.getPage());
        pageResponse.setSize(response.getSize());
        pageResponse.setLength(response.getLength());
        return pageResponse;
    }

    @RequestMapping(value = "/simple_context_categories", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ContextCategory> getSimpleContextCategoryList(
            @AuthenticationPrincipal AuthenticatedPrincipal requester) {
        GetContextCategoryListRequest request = new GetContextCategoryListRequest(
                authenticationService.asScoreUser(requester));
        request.setPageIndex(-1);
        request.setPageSize(-1);

        GetContextCategoryListResponse response =
                contextCategoryService.getContextCategoryList(request);
        return response.getResults();
    }

    @RequestMapping(value = "/context_category/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ContextCategory getContextCategory(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger contextCategoryId) {

        GetContextCategoryRequest request =
                new GetContextCategoryRequest(authenticationService.asScoreUser(requester))
                        .withContextCategoryId(contextCategoryId);

        GetContextCategoryResponse response =
                contextCategoryService.getContextCategory(request);

        return response.getContextCategory();
    }

    @RequestMapping(value = "/context_schemes_from_ctg/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ContextScheme> getContextSchemeListFromCtxCategory(@PathVariable("id") BigInteger id) {
        return contextCategoryService.getContextSchemeByCategoryId(id);
    }

    @RequestMapping(value = "/context_category", method = RequestMethod.PUT)
    public ResponseEntity create(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody ContextCategory contextCategory) {

        CreateContextCategoryRequest request =
                new CreateContextCategoryRequest(authenticationService.asScoreUser(requester))
                        .withName(contextCategory.getName())
                        .withDescription(contextCategory.getDescription());

        CreateContextCategoryResponse response =
                contextCategoryService.createContextCategory(request);

        if (response.getContextCategoryId() != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/context_category/{id}", method = RequestMethod.POST)
    public ResponseEntity update(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger contextCategoryId,
            @RequestBody ContextCategory contextCategory) {

        UpdateContextCategoryRequest request =
                new UpdateContextCategoryRequest(authenticationService.asScoreUser(requester))
                        .withContextCategoryId(contextCategoryId)
                        .withName(contextCategory.getName())
                        .withDescription(contextCategory.getDescription());

        UpdateContextCategoryResponse response =
                contextCategoryService.updateContextCategory(request);

        if (response.getContextCategoryId() != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/context_category/{id}", method = RequestMethod.DELETE)
    public ResponseEntity delete(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger contextCategoryId) {

        DeleteContextCategoryRequest request =
                new DeleteContextCategoryRequest(authenticationService.asScoreUser(requester))
                        .withContextCategoryId(contextCategoryId);

        DeleteContextCategoryResponse response =
                contextCategoryService.deleteContextCategory(request);

        if (response.contains(contextCategoryId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    public static class DeleteContextCategoryRequestData {
        private List<BigInteger> contextCategoryIdList = Collections.emptyList();

        public List<BigInteger> getContextCategoryIdList() {
            return contextCategoryIdList;
        }

        public void setContextCategoryIdList(List<BigInteger> contextCategoryIdList) {
            this.contextCategoryIdList = contextCategoryIdList;
        }
    }

    @RequestMapping(value = "/context_category/delete", method = RequestMethod.POST)
    public ResponseEntity deletes(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody DeleteContextCategoryRequestData requestData) {

        DeleteContextCategoryRequest request =
                new DeleteContextCategoryRequest(authenticationService.asScoreUser(requester))
                        .withContextCategoryIdList(requestData.getContextCategoryIdList());

        DeleteContextCategoryResponse response =
                contextCategoryService.deleteContextCategory(request);

        if (response.containsAll(requestData.getContextCategoryIdList())) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

}
