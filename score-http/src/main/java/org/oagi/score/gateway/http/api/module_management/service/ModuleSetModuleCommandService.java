package org.oagi.score.gateway.http.api.module_management.service;

import org.oagi.score.gateway.http.api.module_management.controller.payload.CopyModuleSetModuleRequest;
import org.oagi.score.gateway.http.api.module_management.controller.payload.CreateModuleSetModuleRequest;
import org.oagi.score.gateway.http.api.module_management.controller.payload.UpdateModuleSetModuleRequest;
import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSummaryRecord;
import org.oagi.score.gateway.http.api.module_management.model.ModuleType;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleCommandRepository;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.oagi.score.gateway.http.api.module_management.model.ModuleType.DIRECTORY;
import static org.oagi.score.gateway.http.api.module_management.model.ModuleType.FILE;
import static org.springframework.util.StringUtils.hasLength;

@Service
@Transactional
public class ModuleSetModuleCommandService {

    private final String MODULE_PATH_SEPARATOR = "\\";

    @Autowired
    private RepositoryFactory repositoryFactory;

    private ModuleCommandRepository command(ScoreUser requester) {
        return repositoryFactory.moduleCommandRepository(requester);
    }

    private ModuleQueryRepository query(ScoreUser requester) {
        return repositoryFactory.moduleQueryRepository(requester);
    }

    public ModuleId create(ScoreUser requester, CreateModuleSetModuleRequest request) {

        var query = query(requester);

        if (query.hasDuplicateName(request.parentModuleId(), request.name())) {
            throw new IllegalArgumentException("Duplicate module name exist.");
        }

        ModuleSummaryRecord parentModule = null;
        if (request.parentModuleId() != null) {
            parentModule = query.getModule(request.parentModuleId());
        }

        String path = (parentModule != null && hasLength(parentModule.path())) ?
                String.join(MODULE_PATH_SEPARATOR, Arrays.asList(parentModule.path(), request.name())) : request.name();

        return command(requester).create(
                request.moduleSetId(),
                parentModule.moduleId(),
                request.namespaceId(),
                (request.directory() ? DIRECTORY : FILE),
                path,
                request.name(),
                request.versionNum());
    }

    public boolean update(ScoreUser requester, UpdateModuleSetModuleRequest request) {

        var query = query(requester);

        ModuleSummaryRecord module = query.getModule(request.moduleId());
        if (module == null) {
            throw new IllegalArgumentException("Cannot found a module record [moduleId=" + request.moduleId() + "]");
        }

        String path = module.path();
        boolean nameChanged = false;
        List<String> tokens = Collections.emptyList();
        if (hasLength(request.name()) && !Objects.equals(request.name(), module.name())) {
            if (query.hasDuplicateName(module.parentModuleId(), request.name())) {
                throw new IllegalArgumentException("Duplicate module name exist.");
            }

            tokens = Arrays.asList(module.path().split(MODULE_PATH_SEPARATOR + MODULE_PATH_SEPARATOR));
            tokens.set(tokens.size() - 1, request.name());

            path = String.join(MODULE_PATH_SEPARATOR, tokens);
            nameChanged = true;
        }

        boolean updated = command(requester).update(
                request.moduleId(),
                request.namespaceId(),
                path,
                request.name(),
                request.versionNum());

        if (updated && nameChanged && module.type() == DIRECTORY) {
            propagateChangedModulePath(requester, module.moduleId(), tokens);
        }

        return updated;
    }

    private void propagateChangedModulePath(ScoreUser requester, ModuleId moduleId, List<String> tokens) {

        var command = command(requester);

        for (ModuleSummaryRecord child : query(requester).getChildren(moduleId)) {
            String path = String.join(MODULE_PATH_SEPARATOR, tokens) + MODULE_PATH_SEPARATOR + child.name();
            command.updatePath(child.moduleId(), path);
            propagateChangedModulePath(requester, child.moduleId(), Arrays.asList(path.split(MODULE_PATH_SEPARATOR + MODULE_PATH_SEPARATOR)));
        }
    }

    public boolean discard(ScoreUser requester, ModuleId moduleId, ModuleSetId moduleSetId) {

        ModuleSummaryRecord module = query(requester).getModule(moduleId);
        if (module == null) {
            throw new IllegalArgumentException("Cannot found a module record [moduleId=" + moduleId + "]");
        }

        return discard(requester, module);
    }

    private boolean discard(ScoreUser requester, ModuleSummaryRecord module) {
        if (module == null) {
            return false;
        }

        if (module.type() == DIRECTORY) {
            for (ModuleSummaryRecord child : query(requester).getChildren(module.moduleId())) {
                discard(requester, child);
            }
        }

        return command(requester).delete(
                module.moduleId());
    }

    public void copyModule(ScoreUser requester, CopyModuleSetModuleRequest request) {

        var query = query(requester);

        ModuleSummaryRecord targetModule = query.getModule(request.targetModuleId());
        ModuleSummaryRecord parentModule = query.getModule(request.parentModuleId());

        if (query.hasDuplicateName(request.parentModuleId(), targetModule.name())) {
            copyOverWriteModule(requester, targetModule, parentModule, request.copySubModules());
        } else {
            copyInsertModule(requester, targetModule, parentModule, request.copySubModules());
        }
    }

    private void copyOverWriteModule(
            ScoreUser requester, ModuleSummaryRecord targetModule, ModuleSummaryRecord parentModule, boolean copySubModules) {

        var query = query(requester);

        ModuleSummaryRecord duplicated = query.getDuplicateModule(parentModule.moduleId(), targetModule.name());
        if (duplicated == null) {
            copyInsertModule(requester, targetModule, parentModule, copySubModules);
            return;
        }

        var command = command(requester);

        if (duplicated.type() == ModuleType.FILE) {
            if (targetModule.type() == ModuleType.FILE) {
                command.updateVersionNumAndNamespaceId(duplicated.moduleId(),
                        targetModule.namespaceId(), targetModule.versionNum());
            } else {
                command.updateVersionNumAndNamespaceIdAndType(duplicated.moduleId(),
                        targetModule.namespaceId(), targetModule.versionNum(), ModuleType.DIRECTORY);
                if (copySubModules) {
                    query.getChildren(targetModule.moduleId()).stream().forEach(child -> {
                        copyInsertModule(requester, child, duplicated, copySubModules);
                    });
                }
            }
        } else {
            if (targetModule.type() == ModuleType.FILE) {
                command.delete(duplicated.moduleId());
                if (copySubModules) {
                    copyInsertModule(requester, targetModule, parentModule, copySubModules);
                }
            } else {
                command.updateVersionNumAndNamespaceId(duplicated.moduleId(),
                        targetModule.namespaceId(), targetModule.versionNum());
                if (copySubModules) {
                    query.getChildren(targetModule.moduleId()).stream().forEach(child -> {
                        copyInsertModule(requester, child, duplicated, copySubModules);
                    });
                }
            }
        }
    }

    private void copyInsertModule(
            ScoreUser requester, ModuleSummaryRecord targetModule, ModuleSummaryRecord parentModule, boolean copySubModules) {
        String path;
        if (parentModule.path().length() == 0) {
            path = targetModule.name();
        } else {
            path = parentModule.path() + MODULE_PATH_SEPARATOR + targetModule.name();
        }

        ModuleId createdModuleId = command(requester).create(
                parentModule.moduleSetId(), parentModule.moduleId(), targetModule.namespaceId(),
                targetModule.type(), path, targetModule.name(), targetModule.versionNum());
        if (copySubModules) {
            var query = query(requester);

            ModuleSummaryRecord createdModule = query.getModule(createdModuleId);
            query.getChildren(targetModule.moduleId()).stream().forEach(child -> {
                copyInsertModule(requester, child, createdModule, copySubModules);
            });
        }
    }

}
