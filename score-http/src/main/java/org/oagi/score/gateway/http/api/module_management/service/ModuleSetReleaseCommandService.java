package org.oagi.score.gateway.http.api.module_management.service;

import org.oagi.score.gateway.http.api.module_management.controller.payload.AssignCCToModuleRequest;
import org.oagi.score.gateway.http.api.module_management.controller.payload.CreateModuleSetReleaseRequest;
import org.oagi.score.gateway.http.api.module_management.controller.payload.UpdateModuleSetReleaseRequest;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseDetailsRecord;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleManifestCommandRepository;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleSetReleaseCommandRepository;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleSetReleaseQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ModuleSetReleaseCommandService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private ModuleSetReleaseCommandRepository command(ScoreUser requester) {
        return repositoryFactory.moduleSetReleaseCommandRepository(requester);
    }

    private ModuleSetReleaseQueryRepository query(ScoreUser requester) {
        return repositoryFactory.moduleSetReleaseQueryRepository(requester);
    }

    private ModuleManifestCommandRepository moduleManifestCommand(ScoreUser requester) {
        return repositoryFactory.moduleManifestCommandRepository(requester);
    }

    public ModuleSetReleaseId create(ScoreUser requester, CreateModuleSetReleaseRequest request) {

        var command = command(requester);

        if (request.isDefault()) {
            command.disableDefaultFlag(request.releaseId());
        }

        String moduleSetReleaseName = request.name();
        if (!StringUtils.hasLength(moduleSetReleaseName)) {
            moduleSetReleaseName = query(requester).getDefaultName(request.moduleSetId());
        }

        ModuleSetReleaseId moduleSetReleaseId = command.create(
                request.moduleSetId(), request.releaseId(), moduleSetReleaseName,
                request.description(), request.isDefault());

        if (request.baseModuleSetReleaseId() != null) {
            command.copyModuleCcManifest(
                    moduleSetReleaseId, request.baseModuleSetReleaseId());
        }

        return moduleSetReleaseId;
    }

    public boolean update(ScoreUser requester, UpdateModuleSetReleaseRequest request) {

        var query = query(requester);
        var command = command(requester);

        ModuleSetReleaseDetailsRecord moduleSetRelease =
                query.getModuleSetReleaseDetails(request.moduleSetReleaseId());

        if (request.isDefault()) {
            command.disableDefaultFlag(moduleSetRelease.release().releaseId());
        }

        String moduleSetReleaseName = request.name();
        if (!StringUtils.hasLength(moduleSetReleaseName)) {
            moduleSetReleaseName = query.getDefaultName(moduleSetRelease.moduleSet().moduleSetId());
        }

        String moduleSetReleaseDescription = request.description();

        return command.update(request.moduleSetReleaseId(),
                moduleSetReleaseName, moduleSetReleaseDescription, request.isDefault());
    }

    public boolean discard(ScoreUser requester, ModuleSetReleaseId moduleSetReleaseId) {

        return command(requester).delete(moduleSetReleaseId);
    }

    public void assignCc(ScoreUser requester, AssignCCToModuleRequest assignCCToModuleRequest) {

        var command = moduleManifestCommand(requester);

        assignCCToModuleRequest.nodes().forEach(node -> {

            if (node.accManifestId() != null) {
                command.createAccModuleManifest(
                        node.accManifestId(),
                        assignCCToModuleRequest.moduleId(), assignCCToModuleRequest.moduleSetReleaseId());
            } else if (node.asccpManifestId() != null) {
                command.createAsccpModuleManifest(
                        node.asccpManifestId(),
                        assignCCToModuleRequest.moduleId(), assignCCToModuleRequest.moduleSetReleaseId());
            } else if (node.bccpManifestId() != null) {
                command.createBccpModuleManifest(
                        node.bccpManifestId(),
                        assignCCToModuleRequest.moduleId(), assignCCToModuleRequest.moduleSetReleaseId());
            } else if (node.dtManifestId() != null) {
                command.createDtModuleManifest(
                        node.dtManifestId(),
                        assignCCToModuleRequest.moduleId(), assignCCToModuleRequest.moduleSetReleaseId());
            } else if (node.codeListManifestId() != null) {
                command.createCodeListModuleManifest(
                        node.codeListManifestId(),
                        assignCCToModuleRequest.moduleId(), assignCCToModuleRequest.moduleSetReleaseId());
            } else if (node.agencyIdListManifestId() != null) {
                command.createAgencyIdListModuleManifest(
                        node.agencyIdListManifestId(),
                        assignCCToModuleRequest.moduleId(), assignCCToModuleRequest.moduleSetReleaseId());
            } else if (node.xbtManifestId() != null) {
                command.createXbtModuleManifest(
                        node.xbtManifestId(),
                        assignCCToModuleRequest.moduleId(), assignCCToModuleRequest.moduleSetReleaseId());
            }
        });
    }

    public void unassignCc(ScoreUser requester, AssignCCToModuleRequest assignCCToModuleRequest) {

        var command = moduleManifestCommand(requester);

        assignCCToModuleRequest.nodes().forEach(node -> {

            if (node.accManifestId() != null) {
                command.deleteAccModuleManifest(
                        node.accManifestId(),
                        assignCCToModuleRequest.moduleId(), assignCCToModuleRequest.moduleSetReleaseId());
            } else if (node.asccpManifestId() != null) {
                command.deleteAsccpModuleManifest(
                        node.asccpManifestId(),
                        assignCCToModuleRequest.moduleId(), assignCCToModuleRequest.moduleSetReleaseId());
            } else if (node.bccpManifestId() != null) {
                command.deleteBccpModuleManifest(
                        node.bccpManifestId(),
                        assignCCToModuleRequest.moduleId(), assignCCToModuleRequest.moduleSetReleaseId());
            } else if (node.dtManifestId() != null) {
                command.deleteDtModuleManifest(
                        node.dtManifestId(),
                        assignCCToModuleRequest.moduleId(), assignCCToModuleRequest.moduleSetReleaseId());
            } else if (node.codeListManifestId() != null) {
                command.deleteCodeListModuleManifest(
                        node.codeListManifestId(),
                        assignCCToModuleRequest.moduleId(), assignCCToModuleRequest.moduleSetReleaseId());
            } else if (node.agencyIdListManifestId() != null) {
                command.deleteAgencyIdListModuleManifest(
                        node.agencyIdListManifestId(),
                        assignCCToModuleRequest.moduleId(), assignCCToModuleRequest.moduleSetReleaseId());
            } else if (node.xbtManifestId() != null) {
                command.deleteXbtModuleManifest(
                        node.xbtManifestId(),
                        assignCCToModuleRequest.moduleId(), assignCCToModuleRequest.moduleSetReleaseId());
            }
        });
    }
}
