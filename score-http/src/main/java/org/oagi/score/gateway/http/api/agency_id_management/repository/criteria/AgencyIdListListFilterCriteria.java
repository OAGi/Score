package org.oagi.score.gateway.http.api.agency_id_management.repository.criteria;

import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.AccessPrivilege;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;

import java.util.Collection;

public record AgencyIdListListFilterCriteria(
        LibraryId libraryId, ReleaseId releaseId,
        String name, String definition, String module,
        Boolean deprecated,
        Boolean ownedByDeveloper, Boolean newComponent,
        AccessPrivilege access,
        Collection<CcState> states,
        Collection<NamespaceId> namespaces,
        Collection<String> ownerLoginIdSet,
        Collection<String> updaterLoginIdSet,
        DateRangeCriteria lastUpdatedTimestampRange) {
}
