package org.oagi.score.gateway.http.api.code_list_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record CodeListValueDetailsRecord(
        CodeListValueManifestId codeListValueManifestId,
        CodeListValueId codeListValueId,
        CodeListManifestId codeListManifestId,
        Guid guid,

        String value,
        String meaning,
        Definition definition,
        boolean deprecated,
        boolean used,
        CcState state,

        UserSummaryRecord owner,
        WhoAndWhen created,
        WhoAndWhen lastUpdated,

        CodeListValueManifestId prevCodeListValueManifestId,
        CodeListValueManifestId nextCodeListValueManifestId) {

}
