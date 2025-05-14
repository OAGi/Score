package org.oagi.score.gateway.http.api.module_management.model;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record ModuleSetReleaseListEntryRecord(ModuleSetReleaseId moduleSetReleaseId,
                                              ModuleSetId moduleSetId,
                                              String moduleSetName,
                                              LibraryId libraryId,
                                              String libraryName,
                                              ReleaseId releaseId,
                                              String releaseNum,
                                              String name,
                                              String description,
                                              boolean isDefault,
                                              WhoAndWhen created,
                                              WhoAndWhen lastUpdated) {
}
