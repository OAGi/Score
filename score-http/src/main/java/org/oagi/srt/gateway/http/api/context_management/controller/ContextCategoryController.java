package org.oagi.srt.gateway.http.api.context_management.controller;

import org.oagi.srt.gateway.http.api.common.data.PageRequest;
import org.oagi.srt.gateway.http.api.common.data.PageResponse;
import org.oagi.srt.gateway.http.api.context_management.data.*;
import org.oagi.srt.gateway.http.api.context_management.service.ContextCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ContextCategoryController {

    @Autowired
    private ContextCategoryService service;

    @RequestMapping(value = "/context_categories", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<ContextCategory> getContextCategoryList(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "description", required = false) String description,
            @RequestParam(name = "sortActive") String sortActive,
            @RequestParam(name = "sortDirection") String sortDirection,
            @RequestParam(name = "pageIndex") int pageIndex,
            @RequestParam(name = "pageSize") int pageSize) {

        ContextCategoryListRequest request = new ContextCategoryListRequest();

        request.setName(name);
        request.setDescription(description);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setSortActive(sortActive);
        pageRequest.setSortDirection(sortDirection);
        pageRequest.setPageIndex(pageIndex);
        pageRequest.setPageSize(pageSize);
        request.setPageRequest(pageRequest);

        return service.getContextCategoryList(request);
    }

    @RequestMapping(value = "/simple_context_categories", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SimpleContextCategory> getSimpleContextCategoryList() {
        return service.getSimpleContextCategoryList();
    }


    @RequestMapping(value = "/context_category/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ContextCategory getContextCategory(@PathVariable("id") long id) {
        return service.getContextCategory(id);
    }

    @RequestMapping(value = "/context_schemes_from_ctg/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ContextScheme> getContextSchemeListFromCtxCategory(@PathVariable("id") long id) {
        return service.getContextSchemeByCategoryId(id);
    }

    @RequestMapping(value = "/context_category", method = RequestMethod.PUT)
    public ResponseEntity create(
            @RequestBody ContextCategory contextCategory) {
        service.insert(contextCategory);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/context_category/{id}", method = RequestMethod.POST)
    public ResponseEntity update(
            @PathVariable("id") long id,
            @RequestBody ContextCategory contextCategory) {
        contextCategory.setCtxCategoryId(id);
        service.update(contextCategory);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/context_category/{id}", method = RequestMethod.DELETE)
    public ResponseEntity delete(
            @PathVariable("id") long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/context_category/delete", method = RequestMethod.POST)
    public ResponseEntity deletes(@RequestBody DeleteContextCategoryRequest request) {
        service.delete(request.getCtxCategoryIds());
        return ResponseEntity.noContent().build();
    }

}
