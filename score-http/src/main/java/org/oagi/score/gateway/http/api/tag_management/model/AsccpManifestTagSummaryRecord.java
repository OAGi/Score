package org.oagi.score.gateway.http.api.tag_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;

public record AsccpManifestTagSummaryRecord(AsccpManifestId asccpManifestId,
                                            TagId tagId) {
}
