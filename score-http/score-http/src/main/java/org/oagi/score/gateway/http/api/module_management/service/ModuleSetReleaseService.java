package org.oagi.score.gateway.http.api.module_management.service;

import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.module.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ModuleSetReleaseService {


    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    public GetModuleSetReleaseListResponse getModuleSetReleaseList(GetModuleSetReleaseListRequest request) {
        return scoreRepositoryFactory.createModuleSetReleaseReadRepository().getModuleSetReleaseList(request);
    }

    public GetModuleSetReleaseResponse getModuleSetRelease(GetModuleSetReleaseRequest request) {
        return scoreRepositoryFactory.createModuleSetReleaseReadRepository().getModuleSetRelease(request);
    }

    @Transactional
    public CreateModuleSetReleaseResponse createModuleSetRelease(CreateModuleSetReleaseRequest request) {
        return scoreRepositoryFactory.createModuleSetReleaseWriteRepository().createModuleSetRelease(request);
    }

    @Transactional
    public UpdateModuleSetReleaseResponse updateModuleSetRelease(UpdateModuleSetReleaseRequest request) {
        return scoreRepositoryFactory.createModuleSetReleaseWriteRepository().updateModuleSetRelease(request);
    }

    @Transactional
    public DeleteModuleSetReleaseResponse discardModuleSetRelease(DeleteModuleSetReleaseRequest request) {
        return scoreRepositoryFactory.createModuleSetReleaseWriteRepository().deleteModuleSetRelease(request);
    }
}
