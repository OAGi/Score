package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleId;

public record ModuleCCID<M extends ManifestId>(
        ModuleId moduleId,
        M manifestId,
        String path) {

}
