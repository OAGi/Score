package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class AgencyIDListObject {

    private BigInteger agencyIDListManifestId;

    private BigInteger agencyIDListId;

    private BigInteger basedAgencyIDListManifestId;

    private BigInteger releaseId;

    private String guid;

    private String enumTypeGuid;

    private String name;

    private String listId;

    private String versionId;

    private String definition;

    private String definitionSource;

    private String remark;

    private BigInteger namespaceId;

    private boolean deprecated;

    private String state;

    private BigInteger ownerUserId;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

}
