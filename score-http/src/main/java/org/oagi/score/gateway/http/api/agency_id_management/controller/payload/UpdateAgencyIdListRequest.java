package org.oagi.score.gateway.http.api.agency_id_management.controller.payload;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

import java.util.Collection;
import java.util.Collections;

public record UpdateAgencyIdListRequest(
        AgencyIdListManifestId agencyIdListManifestId,
        String name, String versionId, String listId,
        AgencyIdListValueManifestId agencyIdListValueManifestId,
        Definition definition, String remark,
        NamespaceId namespaceId,
        Boolean deprecated,
        Collection<UpdateAgencyIdListValueRequest> valueList) {

    public Collection<UpdateAgencyIdListValueRequest> valueList() {
        return (valueList != null) ? valueList : Collections.emptyList();
    }

    // Copy constructor to create a new instance with an agencyIdListManifestId
    public UpdateAgencyIdListRequest withAgencyIdListManifestId(AgencyIdListManifestId agencyIdListManifestId) {
        return new UpdateAgencyIdListRequest(agencyIdListManifestId, name, versionId, listId,
                agencyIdListValueManifestId,
                definition, remark,
                namespaceId, deprecated,
                valueList);
    }

}
