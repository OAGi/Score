package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class AgencyIDListValueObject {

    private BigInteger agencyIDListValueManifestId;

    private BigInteger agencyIDListValueId;

    private BigInteger basedAgencyIDListValueManifestId;

    private BigInteger releaseId;

    private String guid;

    private String value;

    private String name;

    private String definition;

    private String definitionSource;

    private BigInteger ownerListId;

    private boolean deprecated;

    private BigInteger ownerUserId;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

}
