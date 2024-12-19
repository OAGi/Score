package org.oagi.score.gateway.http.api.library_management.controller;

import com.google.common.collect.ImmutableMap;
import org.oagi.score.gateway.http.api.library_management.data.Library;
import org.oagi.score.gateway.http.api.library_management.data.LibraryList;
import org.oagi.score.gateway.http.api.library_management.data.LibraryListRequest;
import org.oagi.score.gateway.http.api.library_management.service.LibraryService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
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
public class LibraryController {

    @Autowired
    private LibraryService service;

    @Autowired
    private SessionService sessionService;

    @RequestMapping(value = "/libraries", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Library> getLibraries(@AuthenticationPrincipal AuthenticatedPrincipal user) {
        return service.getLibraries();
    }

    @RequestMapping(value = "/library/{id:[\\d]+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Library getLibraryById(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                  @PathVariable("id") BigInteger libraryId) {
        return service.getLibraryById(user, libraryId);
    }

    @RequestMapping(value = "/library", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> createLibrary(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                             @RequestBody Library library) {
        BigInteger libraryId = service.create(user, library);
        return ImmutableMap.<String, Object>builder()
                .put("libraryId", libraryId)
                .build();
    }

    @RequestMapping(value = "/library/{id:[\\d]+}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateLibrary(@PathVariable("id") BigInteger libraryId,
                                        @AuthenticationPrincipal AuthenticatedPrincipal user,
                                        @RequestBody Library library) {
        library.setLibraryId(libraryId);
        service.update(user, library);
        return ResponseEntity.accepted().build();
    }

    @RequestMapping(value = "/library/{id:[\\d]+}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity discardLibrary(@PathVariable("id") BigInteger libraryId,
                                         @AuthenticationPrincipal AuthenticatedPrincipal user) {
        service.discard(user, libraryId);
        return ResponseEntity.accepted().build();
    }

    @RequestMapping(value = "/library_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<LibraryList> getLibraryList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                    @RequestParam(name = "name", required = false) String name,
                                                    @RequestParam(name = "organization", required = false) String organization,
                                                    @RequestParam(name = "description", required = false) String description,
                                                    @RequestParam(name = "domain", required = false) String domain,
                                                    @RequestParam(name = "state", required = false) String state,
                                                    @RequestParam(name = "updaterLoginIds", required = false) String updaterLoginIds,
                                                    @RequestParam(name = "updateStart", required = false) String updateStart,
                                                    @RequestParam(name = "updateEnd", required = false) String updateEnd,
                                                    @RequestParam(name = "sortActive") String sortActive,
                                                    @RequestParam(name = "sortDirection") String sortDirection,
                                                    @RequestParam(name = "pageIndex") int pageIndex,
                                                    @RequestParam(name = "pageSize") int pageSize) {
        LibraryListRequest request = new LibraryListRequest();

        request.setName(name);
        request.setOrganization(organization);
        request.setDescription(description);
        request.setDomain(domain);
        request.setState(state);
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
        return service.getLibraryList(user, request);
    }

}
