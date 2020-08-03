package org.oagi.score.gateway.http.api.bie_management.controller;

import org.oagi.score.gateway.http.api.bie_management.data.AsccpForBie;
import org.oagi.score.gateway.http.api.bie_management.data.BieCreateRequest;
import org.oagi.score.gateway.http.api.bie_management.data.BieCreateResponse;
import org.oagi.score.gateway.http.api.bie_management.service.BieService;
import org.oagi.score.gateway.http.api.module_management.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BieCreateController {

    @Autowired
    private BieService bieService;

    @Autowired
    private ModuleService moduleService;

    @RequestMapping(value = "/profile_bie/asccp/release/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AsccpForBie> getAsccpListForBie(@PathVariable("id") long releaseId) {
        return bieService.getAsccpListForBie(releaseId);
    }

    @RequestMapping(value = "/profile_bie/create", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BieCreateResponse create(
            @AuthenticationPrincipal User user,
            @RequestBody BieCreateRequest bieCreateRequest) {

        BieCreateResponse response = bieService.createBie(user, bieCreateRequest);
        return response;
    }

}
