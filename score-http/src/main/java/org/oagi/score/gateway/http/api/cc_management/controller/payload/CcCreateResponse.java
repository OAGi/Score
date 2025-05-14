package org.oagi.score.gateway.http.api.cc_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;

@Data
public class CcCreateResponse {

    private ManifestId manifestId;

}
