package org.oagi.score.gateway.http.api.module_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.blob_content.BlobContentManifestId;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record ModuleBlobContentRecord(
        ModuleBlobContentManifestId moduleBlobContentManifestId,
        ModuleSetReleaseId moduleSetReleaseId,
        BlobContentManifestId blobContentManifestId,
        ModuleId moduleId,
        String modulePath,
        WhoAndWhen created,
        WhoAndWhen lastUpdated) {
}
