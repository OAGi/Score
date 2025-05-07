package org.oagi.score.gateway.http.api.tag_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;

public record DtManifestTagSummaryRecord(DtManifestId dtManifestId,
                                         TagId tagId) {
}
