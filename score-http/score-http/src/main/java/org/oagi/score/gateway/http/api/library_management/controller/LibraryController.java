package org.oagi.score.gateway.http.api.library_management.controller;

import org.oagi.score.gateway.http.api.library_management.data.Library;
import org.oagi.score.gateway.http.api.library_management.service.LibraryService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

}
