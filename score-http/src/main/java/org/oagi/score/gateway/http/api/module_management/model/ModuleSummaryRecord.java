package org.oagi.score.gateway.http.api.module_management.model;

import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

public record ModuleSummaryRecord(ModuleId moduleId,
                                  ModuleSetId moduleSetId,
                                  ModuleId parentModuleId,
                                  ModuleType type,
                                  String path,
                                  NamespaceId namespaceId,
                                  String namespaceUri,
                                  String name,
                                  String versionNum) {
}
