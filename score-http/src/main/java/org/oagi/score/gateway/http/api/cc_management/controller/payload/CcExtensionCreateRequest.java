package org.oagi.score.gateway.http.api.cc_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;

@Data
public class CcExtensionCreateRequest {

    private AccManifestId accManifestId;

}
