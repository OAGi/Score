package org.oagi.score.gateway.http.api.module_management.model;

public record ModuleSetMetadataRecord(ModuleSetId moduleSetId,
                                      int numberOfDirectories,
                                      int numberOfFiles) {
}
