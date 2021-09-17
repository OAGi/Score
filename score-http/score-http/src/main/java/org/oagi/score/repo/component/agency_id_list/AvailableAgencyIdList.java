package org.oagi.score.repo.component.agency_id_list;

import lombok.Data;

import java.math.BigInteger;

@Data
public class AvailableAgencyIdList {

    private BigInteger agencyIdListId;
    private BigInteger agencyIdListManifestId;
    private BigInteger basedAgencyIdListManifestId;
    private String agencyIdListName;
    private String versionId;
    private String state;
    private boolean isDeprecated;
}
