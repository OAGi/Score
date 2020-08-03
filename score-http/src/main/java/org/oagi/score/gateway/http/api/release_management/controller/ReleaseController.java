package org.oagi.score.gateway.http.api.release_management.controller;

import org.oagi.score.gateway.http.api.release_management.data.ReleaseList;
import org.oagi.score.gateway.http.api.release_management.data.SimpleRelease;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReleaseController {

    @Autowired
    private ReleaseService service;

    @RequestMapping(value = "/simple_releases", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SimpleRelease> getSimpleReleases() {
        return service.getSimpleReleases();
    }

    @RequestMapping(value = "/simple_release/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public SimpleRelease getSimpleRelease(@PathVariable("id") long releaseId) {
        return service.getSimpleReleaseByReleaseId(releaseId);
    }

    @RequestMapping(value = "/release_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ReleaseList> getReleaseList(@AuthenticationPrincipal User user) {
        return service.getReleaseList(user);
    }

}
