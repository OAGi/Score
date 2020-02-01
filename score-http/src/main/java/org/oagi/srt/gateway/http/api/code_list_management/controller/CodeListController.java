package org.oagi.srt.gateway.http.api.code_list_management.controller;

import org.oagi.srt.gateway.http.api.code_list_management.data.CodeList;
import org.oagi.srt.gateway.http.api.code_list_management.data.CodeListForList;
import org.oagi.srt.gateway.http.api.code_list_management.data.CodeListForListRequest;
import org.oagi.srt.gateway.http.api.code_list_management.data.DeleteCodeListRequest;
import org.oagi.srt.gateway.http.api.code_list_management.service.CodeListService;
import org.oagi.srt.gateway.http.api.common.data.PageRequest;
import org.oagi.srt.gateway.http.api.common.data.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

@RestController
public class CodeListController {

    @Autowired
    private CodeListService service;

    @RequestMapping(value = "/code_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<CodeListForList> getCodeLists(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "states", required = false) String states,
            @RequestParam(name = "extensible", required = false) Boolean extensible,
            @RequestParam(name = "updaterLoginIds", required = false) String updaterLoginIds,
            @RequestParam(name = "updateStart", required = false) String updateStart,
            @RequestParam(name = "updateEnd", required = false) String updateEnd,
            @RequestParam(name = "sortActive") String sortActive,
            @RequestParam(name = "sortDirection") String sortDirection,
            @RequestParam(name = "pageIndex") int pageIndex,
            @RequestParam(name = "pageSize") int pageSize) {

        CodeListForListRequest request = new CodeListForListRequest();

        request.setName(name);
        request.setStates(StringUtils.isEmpty(states) ? Collections.emptyList() :
                Arrays.asList(states.split(",")).stream().map(e -> e.trim()).filter(e -> !StringUtils.isEmpty(e)).collect(Collectors.toList()));
        request.setExtensible(extensible);

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

        return service.getCodeLists(request);
    }

    @RequestMapping(value = "/code_list/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CodeList getCodeList(@PathVariable("id") long id) {
        return service.getCodeList(id);
    }

    @RequestMapping(value = "/code_list", method = RequestMethod.PUT)
    public ResponseEntity create(
            @AuthenticationPrincipal User user,
            @RequestBody CodeList codeList) {
        service.insert(user, codeList);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/code_list/{id}", method = RequestMethod.POST)
    public ResponseEntity update(
            @PathVariable("id") long id,
            @AuthenticationPrincipal User user,
            @RequestBody CodeList codeList) {
        codeList.setCodeListId(id);
        service.update(user, codeList);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/code_list/{id}", method = RequestMethod.DELETE)
    public ResponseEntity delete(
            @PathVariable("id") long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/code_list/delete", method = RequestMethod.POST)
    public ResponseEntity deletes(@RequestBody DeleteCodeListRequest request) {
        service.delete(request.getCodeListIds());
        return ResponseEntity.noContent().build();
    }
}
