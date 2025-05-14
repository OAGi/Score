package org.oagi.score.gateway.http.api.agency_id_management.controller.payload;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

public record CreateAgencyIdListRequest(
        LibraryId libraryId,
        ReleaseId releaseId,
        AgencyIdListManifestId basedAgencyIdListManifestId) {

}
