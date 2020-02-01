package org.oagi.srt.gateway.http.api.context_management.controller;

import org.oagi.srt.gateway.http.api.common.data.PageRequest;
import org.oagi.srt.gateway.http.api.common.data.PageResponse;
import org.oagi.srt.gateway.http.api.context_management.data.*;
import org.oagi.srt.gateway.http.api.context_management.service.ContextSchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ContextSchemeController {

    @Autowired
    private ContextSchemeService service;

    @RequestMapping(value = "/context_schemes", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<ContextScheme> getContextSchemeList(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "updaterLoginIds", required = false) String updaterLoginIds,
            @RequestParam(name = "updateStart", required = false) String updateStart,
            @RequestParam(name = "updateEnd", required = false) String updateEnd,
            @RequestParam(name = "sortActive") String sortActive,
            @RequestParam(name = "sortDirection") String sortDirection,
            @RequestParam(name = "pageIndex") int pageIndex,
            @RequestParam(name = "pageSize") int pageSize) {

        ContextSchemeListRequest request = new ContextSchemeListRequest();

        request.setName(name);
        request.setUpdaterLoginIds(StringUtils.isEmpty(updaterLoginIds) ? Collections.emptyList() :
                Arrays.asList(updaterLoginIds.split(",")).stream().map(e -> e.trim()).filter(e -> !StringUtils.isEmpty(e)).collect(Collectors.toList()));

        if (!StringUtils.isEmpty(updateStart)) {
            request.setUpdateStartDate(new Date(Long.valueOf(updateStart)));
        }
        if (!StringUtils.isEmpty(updateEnd)) {
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

        return service.getContextSchemeList(request);
    }

    @RequestMapping(value = "/context_scheme/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ContextScheme getContextScheme(@PathVariable("id") long id) {
        return service.getContextScheme(id);
    }

    @RequestMapping(value = "/simple_context_schemes", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SimpleContextScheme> getSimpleContextSchemeList() {
        return service.getSimpleContextSchemeList();
    }

    @RequestMapping(value = "/context_category/{id}/simple_context_schemes", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SimpleContextScheme> getSimpleContextSchemeList(@PathVariable("id") long ctxCategoryId) {
        return service.getSimpleContextSchemeList(ctxCategoryId);
    }

    @RequestMapping(value = "/simple_context_scheme_value_from_ctx_values/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ContextSchemeValue getSimpleContextSchemeValueByCtxSchemeValuesId(@PathVariable("id") long ctxSchemeValuesId) {
        return service.getSimpleContextSchemeValueByCtxSchemeValuesId(ctxSchemeValuesId);
    }

    @RequestMapping(value = "/biz_ctx_values_from_ctx_values/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BusinessContextValue> getBizCtxValueFromCtxSchemeValueId(@PathVariable("id") long ctxSchemeValueId) {
        return service.getBizCtxValueFromCtxSchemeValueId(ctxSchemeValueId);
    }

    @RequestMapping(value = "/biz_ctx/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BusinessContext getBusinessContext(@PathVariable("id") long bizCtxId) {
        return service.getBusinessContext(bizCtxId);
    }

    @RequestMapping(value = "/biz_ctx_values", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BusinessContextValue> getBizCtxValues() {
        return service.getBizCtxValues();
    }

    @RequestMapping(value = "/context_scheme", method = RequestMethod.PUT)
    public ResponseEntity create(
            @AuthenticationPrincipal User user,
            @RequestBody ContextScheme contextScheme) {
        service.insert(user, contextScheme);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/context_scheme/{id}", method = RequestMethod.POST)
    public ResponseEntity update(
            @PathVariable("id") long id,
            @AuthenticationPrincipal User user,
            @RequestBody ContextScheme contextScheme) {
        contextScheme.setCtxSchemeId(id);
        service.update(user, contextScheme);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/context_scheme/{id}", method = RequestMethod.DELETE)
    public ResponseEntity delete(
            @PathVariable("id") long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/context_scheme/delete", method = RequestMethod.POST)
    public ResponseEntity deletes(@RequestBody DeleteContextSchemeRequest request) {
        service.delete(request.getCtxSchemeIds());
        return ResponseEntity.noContent().build();
    }
}
