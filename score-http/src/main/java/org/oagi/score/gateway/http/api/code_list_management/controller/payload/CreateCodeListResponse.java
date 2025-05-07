package org.oagi.score.gateway.http.api.code_list_management.controller.payload;

import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;

public record CreateCodeListResponse(CodeListManifestId codeListManifestId,
                                     String status, String statusMessage) {
}
