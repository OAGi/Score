package org.oagi.score.gateway.http.api.module_management.service;

import org.oagi.score.gateway.http.api.module_management.controller.payload.CopyModuleSetModuleRequest;
import org.oagi.score.gateway.http.api.module_management.controller.payload.CreateModuleSetReleaseRequest;
import org.oagi.score.gateway.http.api.module_management.controller.payload.CreateModuleSetRequest;
import org.oagi.score.gateway.http.api.module_management.controller.payload.UpdateModuleSetRequest;
import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseDetailsRecord;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleCommandRepository;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleQueryRepository;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleSetCommandRepository;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleSetReleaseQueryRepository;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.repository.NamespaceQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ModuleSetCommandService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private ModuleSetCommandRepository command(ScoreUser requester) {
        return repositoryFactory.moduleSetCommandRepository(requester);
    }

    private ModuleCommandRepository moduleCommand(ScoreUser requester) {
        return repositoryFactory.moduleCommandRepository(requester);
    }

    private ModuleQueryRepository moduleQuery(ScoreUser requester) {
        return repositoryFactory.moduleQueryRepository(requester);
    }

    private ModuleSetReleaseQueryRepository moduleSetReleaseQuery(ScoreUser requester) {
        return repositoryFactory.moduleSetReleaseQueryRepository(requester);
    }

    private NamespaceQueryRepository namespaceQuery(ScoreUser requester) {
        return repositoryFactory.namespaceQueryRepository(requester);
    }

    @Autowired
    private ModuleSetModuleCommandService moduleSetModuleCommandService;

    @Autowired
    private ModuleSetReleaseCommandService moduleSetReleaseCommandService;

    public ModuleSetId create(ScoreUser requester, CreateModuleSetRequest request) {

        var command = command(requester);

        ModuleSetId moduleSetId = command.create(
                request.libraryId(), request.name(), request.description());

        NamespaceSummaryRecord namespace =
                namespaceQuery(requester).getAnyStandardNamespaceSummary(request.libraryId());

        ModuleId rootModuleId = moduleCommand(requester).createRootModule(
                moduleSetId, (namespace != null) ? namespace.namespaceId() : null);

        if (request.createModuleSetRelease()) {
            ModuleSetReleaseDetailsRecord moduleSetRelease =
                    moduleSetReleaseQuery(requester).getModuleSetReleaseDetails(request.targetModuleSetReleaseId());

            moduleQuery(requester).getTopLevelModules(moduleSetRelease.moduleSet().moduleSetId())
                    .forEach(target ->
                            moduleSetModuleCommandService.copyModule(requester, new CopyModuleSetModuleRequest(
                                    moduleSetId,
                                    rootModuleId,
                                    target.moduleId(),
                                    true)));

            moduleSetReleaseCommandService.create(requester, new CreateModuleSetReleaseRequest(
                    moduleSetId,
                    request.targetReleaseId(),
                    moduleSetRelease.moduleSetReleaseId(),
                    null, null, false));
        }

        return moduleSetId;
    }

    public boolean update(ScoreUser requester, UpdateModuleSetRequest request) {

        return command(requester).update(
                request.moduleSetId(), request.name(), request.description());
    }

    public boolean discard(ScoreUser requester, ModuleSetId moduleSetId) {

        return command(requester).delete(moduleSetId);
    }
}
