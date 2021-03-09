package org.oagi.score.gateway.http.api.module_management.service;

import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.module.model.*;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ModuleSetService {


    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    public GetModuleSetListResponse getModuleSetList(GetModuleSetListRequest request) {
        return scoreRepositoryFactory.createModuleSetReadRepository().getModuleSetList(request);
    }

    public GetModuleSetResponse getModuleSet(GetModuleSetRequest request) {
        return scoreRepositoryFactory.createModuleSetReadRepository().getModuleSet(request);
    }

    @Transactional
    public CreateModuleSetResponse createModuleSet(CreateModuleSetRequest request) {
        return scoreRepositoryFactory.createModuleSetWriteRepository().createModuleSet(request);
    }

    @Transactional
    public UpdateModuleSetResponse updateModuleSet(UpdateModuleSetRequest request) {
        return scoreRepositoryFactory.createModuleSetWriteRepository().updateModuleSet(request);
    }

    @Transactional
    public DeleteModuleSetResponse discardModuleSet(DeleteModuleSetRequest request) {
        return scoreRepositoryFactory.createModuleSetWriteRepository().deleteModuleSet(request);
    }

    public GetModuleListResponse getModuleSetModuleList(GetModuleListRequest request) {
        return scoreRepositoryFactory.createModuleReadRepository().getModuleList(request);
    }
}
