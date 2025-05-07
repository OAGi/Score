package org.oagi.score.gateway.http.api.tag_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;

public record BccpManifestTagSummaryRecord(BccpManifestId bccpManifestId,
                                           TagId tagId) {
}
