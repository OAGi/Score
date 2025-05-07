package org.oagi.score.gateway.http.api.tag_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;

public record AccManifestTagSummaryRecord(AccManifestId accManifestId,
                                          TagId tagId) {
}
