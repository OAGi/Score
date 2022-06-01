package org.oagi.score.gateway.http.api.xbt_management.controller;

import org.oagi.score.data.Xbt;
import org.oagi.score.gateway.http.api.xbt_management.service.XbtListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.List;

@RestController
public class XbtListController {

    @Autowired
    private XbtListService service;

    @RequestMapping(value = "/xbt/simple_list/{releaseId}", method = RequestMethod.GET)
    public List<Xbt> getSimpleXbtList(
            @PathVariable("releaseId") BigInteger releaseId,
            @AuthenticationPrincipal AuthenticatedPrincipal user) {
        return service.getXbtSimpleList(user, releaseId);
    }
}
