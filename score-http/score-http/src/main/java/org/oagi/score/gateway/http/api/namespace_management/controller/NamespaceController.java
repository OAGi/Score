package org.oagi.score.gateway.http.api.namespace_management.controller;

import com.google.common.collect.ImmutableMap;
import org.oagi.score.gateway.http.api.namespace_management.data.Namespace;
import org.oagi.score.gateway.http.api.namespace_management.data.NamespaceList;
import org.oagi.score.gateway.http.api.namespace_management.data.NamespaceListRequest;
import org.oagi.score.gateway.http.api.namespace_management.data.SimpleNamespace;
import org.oagi.score.gateway.http.api.namespace_management.service.NamespaceService;
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
public class NamespaceController {

    @Autowired
    private NamespaceService service;

    @RequestMapping(value = "/simple_namespaces", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SimpleNamespace> getSimpleNamespaces(@AuthenticationPrincipal AuthenticatedPrincipal user) {
        return service.getSimpleNamespaces(user);
    }

    @RequestMapping(value = "/namespace_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<NamespaceList> getNamespaceList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                        @RequestParam(name = "uri", required = false) String uri,
                                                        @RequestParam(name = "prefix", required = false) String prefix,
                                                        @RequestParam(name = "description", required = false) String description,
                                                        @RequestParam(name = "ownerLoginIds", required = false) String ownerLoginIds,
                                                        @RequestParam(name = "updaterLoginIds", required = false) String updaterLoginIds,
                                                        @RequestParam(name = "updateStart", required = false) String updateStart,
                                                        @RequestParam(name = "updateEnd", required = false) String updateEnd,
                                                        @RequestParam(name = "standard", required = false) String standard,
                                                        @RequestParam(name = "sortActive") String sortActive,
                                                        @RequestParam(name = "sortDirection") String sortDirection,
                                                        @RequestParam(name = "pageIndex") int pageIndex,
                                                        @RequestParam(name = "pageSize") int pageSize) {
        NamespaceListRequest request = new NamespaceListRequest();

        request.setUri(uri);
        request.setPrefix(prefix);
        request.setDescription(description);
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

        if (StringUtils.hasLength(standard)) {
            if ("true".equalsIgnoreCase(standard.toLowerCase())) {
                request.setStandard(true);
            } else if ("false".equalsIgnoreCase(standard.toLowerCase())) {
                request.setStandard(false);
            }
        }

        PageRequest pageRequest = new PageRequest();
        pageRequest.setSortActive(sortActive);
        pageRequest.setSortDirection(sortDirection);
        pageRequest.setPageIndex(pageIndex);
        pageRequest.setPageSize(pageSize);
        request.setPageRequest(pageRequest);
        return service.getNamespaceList(user, request);
    }

    @RequestMapping(value = "/namespace/{id:[\\d]+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Namespace getNamespace(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                  @PathVariable("id") BigInteger namespaceId) {
        return service.getNamespace(user, namespaceId);
    }

    @RequestMapping(value = "/namespace", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> createNamespace(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                               @RequestBody Namespace namespace) {
        BigInteger namespaceId = service.create(user, namespace);
        return ImmutableMap.<String, Object>builder()
                .put("namespaceId", namespaceId)
                .build();
    }

    @RequestMapping(value = "/namespace/{id:[\\d]+}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateNamespace(@PathVariable("id") BigInteger namespaceId,
                                          @AuthenticationPrincipal AuthenticatedPrincipal user,
                                          @RequestBody Namespace namespace) {
        namespace.setNamespaceId(namespaceId);
        service.update(user, namespace);
        return ResponseEntity.accepted().build();
    }

    @RequestMapping(value = "/namespace/{id:[\\d]+}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity discardNamespace(@PathVariable("id") BigInteger namespaceId,
                                           @AuthenticationPrincipal AuthenticatedPrincipal user) {
        service.discard(user, namespaceId);
        return ResponseEntity.accepted().build();
    }

    @RequestMapping(value = "/namespace/{id:[\\d]+}/transfer_ownership", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateNamespace(@PathVariable("id") BigInteger namespaceId,
                                          @AuthenticationPrincipal AuthenticatedPrincipal user,
                                          @RequestBody Map<String, String> request) {
        String targetLoginId = request.get("targetLoginId");
        service.transferOwnership(user, namespaceId, targetLoginId);
        return ResponseEntity.accepted().build();
    }

}
