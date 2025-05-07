package org.oagi.score.gateway.http.api.code_list_management.controller.payload;

import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

public record CreateCodeListRequest(
        LibraryId libraryId,
        ReleaseId releaseId,
        CodeListManifestId basedCodeListManifestId) {

}
