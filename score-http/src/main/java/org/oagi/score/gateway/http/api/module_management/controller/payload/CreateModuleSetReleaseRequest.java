package org.oagi.score.gateway.http.api.module_management.controller.payload;

import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

public record CreateModuleSetReleaseRequest(ModuleSetId moduleSetId,
                                            ReleaseId releaseId,
                                            ModuleSetReleaseId baseModuleSetReleaseId,
                                            String name,
                                            String description,
                                            boolean isDefault) {

}
