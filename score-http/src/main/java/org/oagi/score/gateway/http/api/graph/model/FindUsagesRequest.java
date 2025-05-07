package org.oagi.score.gateway.http.api.graph.model;

import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;

public record FindUsagesRequest(CcType type, ManifestId manifestId) {

}
