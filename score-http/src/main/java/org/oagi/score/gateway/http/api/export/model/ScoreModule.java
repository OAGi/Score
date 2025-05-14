package org.oagi.score.gateway.http.api.export.model;

import lombok.Data;
import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

@Data
public class ScoreModule {

    private ModuleSetReleaseId moduleSetReleaseId;
    private ModuleSetId moduleSetId;
    private ReleaseId releaseId;
    private ModuleId moduleId;
    private String name;
    private Namespace moduleNamespace;
    private Namespace releaseNamespace;
    private String versionNum;
    private String path;

    public String getModulePath() {
        return this.path;
    }
}
