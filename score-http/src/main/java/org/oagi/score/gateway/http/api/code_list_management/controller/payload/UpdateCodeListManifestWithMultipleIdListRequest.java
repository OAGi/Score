package org.oagi.score.gateway.http.api.code_list_management.controller.payload;

import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;

import java.util.List;

public record UpdateCodeListManifestWithMultipleIdListRequest(
        List<CodeListManifestId> codeListManifestIds) {
}
