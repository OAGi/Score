package org.oagi.score.gateway.http.api.module_management.repository;

import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

public interface ModuleSetReleaseCommandRepository {

    ModuleSetReleaseId create(ModuleSetId moduleSetId, ReleaseId releaseId,
                              String name, String description, boolean isDefault);

    void copyModuleCcManifest(ModuleSetReleaseId moduleSetReleaseId,
                              ModuleSetReleaseId baseModuleSetReleaseId);

    void disableDefaultFlag(ReleaseId releaseId);

    boolean update(ModuleSetReleaseId moduleSetReleaseId,
                   String name, String description, boolean isDefault);

    boolean delete(ModuleSetReleaseId moduleSetReleaseId);

}
