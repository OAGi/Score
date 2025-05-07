package org.oagi.score.gateway.http.api.cc_management.model.blob_content;

import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;

public record BlobContentSummaryRecord(
        LibrarySummaryRecord library,
        ReleaseSummaryRecord release,

        BlobContentManifestId blobContentManifestId,
        BlobContentId blobContentId,
        byte[] content) {
}
