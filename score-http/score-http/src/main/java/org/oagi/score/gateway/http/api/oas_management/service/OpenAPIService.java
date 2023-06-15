package org.oagi.score.gateway.http.api.oas_management.service;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.service.authentication.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OpenAPIService {
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;
    @Autowired
    private DSLContext dslContext;


}
