package org.oagi.score.gateway.http.api.agency_id_management.data;

import lombok.Data;

@Data
public class SameNameAgencyIdListParams {

    private long releaseId;
    private Long agencyIdListManifestId;
    private String agencyIdListName;
}
