package org.oagi.score.gateway.http.api.oas_management.service;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.OasDocRepository;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.openapidoc.model.*;
import org.oagi.score.service.authentication.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OpenAPIDocService {
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;
    @Autowired
    private OasDocRepository oasDocRepository;
    @Autowired
    private DSLContext dslContext;

    public GetOasDocResponse getOasDoc(GetOasDocRequest request) {
        GetOasDocResponse response = scoreRepositoryFactory.createOasDocReadRepository().getOasDoc(request);
        return response;
    }

    public GetOasDocListResponse getOasDocList(GetOasDocListRequest request) {
        GetOasDocListResponse response = scoreRepositoryFactory.createOasDocReadRepository().getOasDocList(request);
        return response;
    }

    @Transactional
    public CreateOasDocResponse createOasDoc(CreateOasDocRequest request) {
        CreateOasDocResponse response = scoreRepositoryFactory.createOasDocWriteRepository().createOasDoc(request);
        return response;
    }

    @Transactional
    public UpdateOasDocResponse updateOasDoc(UpdateOasDocRequest request) {
        UpdateOasDocResponse response = scoreRepositoryFactory.createOasDocWriteRepository().updateOasDoc(request);
        return response;
    }

    @Transactional
    public DeleteOasDocResponse DeleteOasDoc(DeleteOasDocRequest request) {
        DeleteOasDocResponse response = scoreRepositoryFactory.createOasDocWriteRepository().deleteOasDoc(request);
        return response;
    }

    public GetBieForOasDocResponse getBieForOasDoc(GetBieForOasDocRequest request) {
        GetBieForOasDocResponse response = scoreRepositoryFactory.createBieForOasDocReadRepository().getBieForOasDoc(request);
        return response;
    }

    public boolean checkOasDocUniqueness(OasDoc oasDoc) {
        return oasDocRepository.checkOasDocUniqueness(oasDoc);
    }

    public boolean checkOasDocTitleUniqueness(OasDoc oasDoc) {
        return oasDocRepository.checkOasDocTitleUniqueness(oasDoc);
    }


}
