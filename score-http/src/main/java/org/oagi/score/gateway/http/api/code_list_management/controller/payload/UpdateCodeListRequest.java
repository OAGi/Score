package org.oagi.score.gateway.http.api.code_list_management.controller.payload;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

import java.util.Collection;
import java.util.Collections;

public record UpdateCodeListRequest(
        CodeListManifestId codeListManifestId,
        String name, String versionId, String listId,
        AgencyIdListValueManifestId agencyIdListValueManifestId,
        Definition definition, String remark,
        NamespaceId namespaceId,
        Boolean deprecated, Boolean extensible,
        Collection<UpdateCodeListValueRequest> valueList) {

    public Collection<UpdateCodeListValueRequest> valueList() {
        return (valueList != null) ? valueList : Collections.emptyList();
    }

    // Copy constructor to create a new instance with an codeListManifestId
    public UpdateCodeListRequest withCodeListManifestId(CodeListManifestId codeListManifestId) {
        return new UpdateCodeListRequest(codeListManifestId, name, versionId, listId,
                agencyIdListValueManifestId,
                definition, remark,
                namespaceId,
                deprecated, extensible,
                valueList);
    }

}
