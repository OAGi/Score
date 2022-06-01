package org.oagi.score.gateway.http.api.agency_id_management.data;

import lombok.Data;

@Data
public class SameAgencyIdListParams {

    private long releaseId;
    private Long agencyIdListManifestId;
    private String listId;
    private Long agencyIdListValueManifestId;
    private String versionId;
}
