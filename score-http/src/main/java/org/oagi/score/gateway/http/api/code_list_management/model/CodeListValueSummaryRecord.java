package org.oagi.score.gateway.http.api.code_list_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.common.model.Guid;

public record CodeListValueSummaryRecord(
        CodeListValueManifestId codeListValueManifestId,
        CodeListValueId codeListValueId,
        CodeListManifestId codeListManifestId,
        Guid guid,

        String value,
        String meaning,
        CcState state) {
}
