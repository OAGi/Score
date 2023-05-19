package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.*;

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

    public static AgencyIDListValueObject createRandomAgencyIDListValue(AppUserObject creator) {
        AgencyIDListValueObject agencyIDListValue = new AgencyIDListValueObject();
        agencyIDListValue.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        agencyIDListValue.setValue(randomAlphanumeric(5, 10).trim());
        agencyIDListValue.setName(randomAlphanumeric(5, 10).trim());
        agencyIDListValue.setDefinition(randomPrint(50, 100).trim());
        agencyIDListValue.setDefinitionSource(randomAlphanumeric(5, 10));
        agencyIDListValue.setDeprecated(false);
        agencyIDListValue.setOwnerUserId(creator.getAppUserId());
        agencyIDListValue.setCreatedBy(creator.getAppUserId());
        agencyIDListValue.setLastUpdatedBy(creator.getAppUserId());
        agencyIDListValue.setCreationTimestamp(LocalDateTime.now());
        agencyIDListValue.setLastUpdateTimestamp(LocalDateTime.now());
        return agencyIDListValue;
    }

}
