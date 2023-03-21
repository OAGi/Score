package org.oagi.score.gateway.http.api.module_management.service;

import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.module.ModuleSetReadRepository;
import org.oagi.score.repo.api.module.model.Module;
import org.oagi.score.repo.api.module.model.*;
import org.oagi.score.service.module.ModuleElementContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public GetModuleSetMetadataResponse getModuleSetMetadata(GetModuleSetMetadataRequest request) {
        return scoreRepositoryFactory.createModuleSetReadRepository().getModuleSetMetadata(request);
    }

    @Transactional
    public CreateModuleSetResponse createModuleSet(CreateModuleSetRequest request) {
        CreateModuleSetResponse response = scoreRepositoryFactory.createModuleSetWriteRepository().createModuleSet(request);

        if (request.isCreateModuleSetRelease()) {
            GetModuleSetReleaseRequest getModuleSetReleaseRequest = new GetModuleSetReleaseRequest(request.getRequester());
            getModuleSetReleaseRequest.setModuleSetReleaseId(request.getTargetModuleSetReleaseId());
            GetModuleSetReleaseResponse getModuleSetReleaseResponse = scoreRepositoryFactory.createModuleSetReleaseReadRepository().getModuleSetRelease(getModuleSetReleaseRequest);
            List<Module> copyTargetModules = scoreRepositoryFactory.createModuleSetReadRepository().getToplevelModules(getModuleSetReleaseResponse.getModuleSetRelease().getModuleSetId());
            copyTargetModules.forEach(target -> {
                CopyModuleRequest copyRequest = new CopyModuleRequest();
                copyRequest.setRequester(request.getRequester());
                copyRequest.setModuleSetId(response.getModuleSet().getModuleSetId());
                copyRequest.setParentModuleId(response.getRootModuleId());
                copyRequest.setTargetModuleId(target.getModuleId());
                copyRequest.setCopySubModules(true);
                copyModule(copyRequest);
            });

            CreateModuleSetReleaseRequest createModuleSetReleaseRequest = new CreateModuleSetReleaseRequest();
            createModuleSetReleaseRequest.setRequester(request.getRequester());
            createModuleSetReleaseRequest.setDefault(false);
            createModuleSetReleaseRequest.setModuleSetId(response.getModuleSet().getModuleSetId());
            createModuleSetReleaseRequest.setReleaseId(request.getTargetReleaseId());
            createModuleSetReleaseRequest.setBaseModuleSetReleaseId(getModuleSetReleaseResponse.getModuleSetRelease().getModuleSetReleaseId());
            scoreRepositoryFactory.createModuleSetReleaseWriteRepository().createModuleSetRelease(createModuleSetReleaseRequest);
        }
        return response;
    }

    @Transactional
    public UpdateModuleSetResponse updateModuleSet(UpdateModuleSetRequest request) {
        return scoreRepositoryFactory.createModuleSetWriteRepository().updateModuleSet(request);
    }

    @Transactional
    public DeleteModuleSetResponse discardModuleSet(DeleteModuleSetRequest request) {
        return scoreRepositoryFactory.createModuleSetWriteRepository().deleteModuleSet(request);
    }

    @Transactional
    public DeleteModuleSetAssignmentResponse unassignModule(DeleteModuleSetAssignmentRequest request) {
        return scoreRepositoryFactory.createModuleSetWriteRepository().unassignModule(request);
    }

    public ModuleElement getModuleSetModules(GetModuleElementRequest request) {
        ModuleSetReadRepository repository = scoreRepositoryFactory.createModuleSetReadRepository();
        ModuleElementContext context = new ModuleElementContext(repository, request.getModuleSetId());
        return context.getRootElement();
    }

    @Transactional
    public CreateModuleResponse createModule(CreateModuleRequest request) {
        return scoreRepositoryFactory.createModuleWriteRepository().createModule(request);
    }

    @Transactional
    public UpdateModuleResponse updateModule(UpdateModuleRequest request) {
        return scoreRepositoryFactory.createModuleWriteRepository().updateModule(request);
    }

    @Transactional
    public DeleteModuleResponse deleteModule(DeleteModuleRequest request) {
        return scoreRepositoryFactory.createModuleWriteRepository().deleteModule(request);
    }

    @Transactional
    public void copyModule(CopyModuleRequest request) {
        scoreRepositoryFactory.createModuleWriteRepository().copyModule(request);
    }
}
