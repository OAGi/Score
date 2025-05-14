package org.oagi.score.gateway.http.api.code_list_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.common.model.Guid;

import java.util.List;

public record CodeListSummaryRecord(
        CodeListManifestId codeListManifestId,
        CodeListId codeListId,
        Guid guid, String enumTypeGuid,
        CodeListManifestId basedCodeListManifestId,
        AgencyIdListValueManifestId agencyIdListValueManifestId,
        String name, String listId, String versionId,
        Definition definition,
        NamespaceId namespaceId,
        boolean deprecated,
        CcState state,
        UserSummaryRecord owner,
        CodeListManifestId prevCodeListManifestId,
        CodeListManifestId nextCodeListManifestId,
        List<CodeListValueSummaryRecord> valueList) {
}
