package org.oagi.score.gateway.http.api.code_list_management.controller.payload;

import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListValueManifestId;

public record UpdateCodeListValueRequest(
        CodeListValueManifestId codeListValueManifestId,
        String value, String meaning,
        Definition definition,
        Boolean deprecated, Boolean developerDefault, Boolean userDefault) {

}
