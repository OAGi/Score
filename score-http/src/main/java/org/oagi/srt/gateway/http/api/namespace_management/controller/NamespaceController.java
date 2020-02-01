package org.oagi.srt.gateway.http.api.namespace_management.controller;

import org.oagi.srt.gateway.http.api.namespace_management.data.Namespace;
import org.oagi.srt.gateway.http.api.namespace_management.data.NamespaceList;
import org.oagi.srt.gateway.http.api.namespace_management.data.SimpleNamespace;
import org.oagi.srt.gateway.http.api.namespace_management.service.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NamespaceController {

    @Autowired
    private NamespaceService service;

    @RequestMapping(value = "/simple_namespaces", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SimpleNamespace> getSimpleNamespaces() {
        return service.getSimpleNamespaces();
    }

    @RequestMapping(value = "/namespace_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<NamespaceList> getNamespaceList(@AuthenticationPrincipal User user) {
        return service.getNamespaceList(user);
    }

    @RequestMapping(value = "/namespace/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Namespace getNamespace(@AuthenticationPrincipal User user,
                                  @PathVariable("id") long namespaceId) {
        return service.getNamespace(user, namespaceId);
    }

    @RequestMapping(value = "/namespace", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createNamespace(@AuthenticationPrincipal User user,
                                          @RequestBody Namespace namespace) {
        service.create(user, namespace);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/namespace/{id}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createNamespace(@PathVariable("id") long namespaceId,
                                          @AuthenticationPrincipal User user,
                                          @RequestBody Namespace namespace) {
        namespace.setNamespaceId(namespaceId);
        service.update(user, namespace);
        return ResponseEntity.accepted().build();
    }
}
