package org.oagi.score.gateway.http.api.module_management.model;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.common.model.Guid;

public record ModuleSetSummaryRecord(ModuleSetId moduleSetId,
                                     LibraryId libraryId,
                                     Guid guid,
                                     String name, String description) {
}
