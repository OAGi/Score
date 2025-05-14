package org.oagi.score.gateway.http.api.module_management.repository;

import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleType;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

public interface ModuleCommandRepository {

    ModuleId createRootModule(ModuleSetId moduleSetId, NamespaceId namespaceId);

    ModuleId create(ModuleSetId moduleSetId,
                    ModuleId parentModuleId, NamespaceId namespaceId,
                    ModuleType moduleType, String path, String name, String versionNum);

    boolean update(ModuleId moduleId, NamespaceId namespaceId,
                   String path, String name, String versionNum);

    boolean updateVersionNumAndNamespaceId(ModuleId moduleId, NamespaceId namespaceId,
                                           String versionNum);

    boolean updateVersionNumAndNamespaceIdAndType(ModuleId moduleId, NamespaceId namespaceId,
                                                  String versionNum, ModuleType moduleType);

    boolean updatePath(ModuleId moduleId, String path);

    boolean delete(ModuleId moduleId);
}
