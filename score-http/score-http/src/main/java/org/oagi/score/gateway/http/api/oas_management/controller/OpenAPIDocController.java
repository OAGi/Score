package org.oagi.score.gateway.http.api.oas_management.controller;

import org.oagi.score.service.authentication.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenAPIDocController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private AuthenticationService authenticationService;

}
