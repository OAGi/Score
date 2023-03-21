package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

@Data
public class BusinessContextObject {

    private BigInteger businessContextId;

    private String guid;

    private String name;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

    public static BusinessContextObject createRandomBusinessContext(AppUserObject creator) {
        return createRandomBusinessContext(creator, "bc");
    }

    public static BusinessContextObject createRandomBusinessContext(AppUserObject creator, String namePrefix) {
        BusinessContextObject businessContext = new BusinessContextObject();
        businessContext.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        businessContext.setName(namePrefix + "_" + randomAlphanumeric(5, 10));
        businessContext.setCreatedBy(creator.getAppUserId());
        businessContext.setLastUpdatedBy(creator.getAppUserId());
        businessContext.setCreationTimestamp(LocalDateTime.now());
        businessContext.setLastUpdateTimestamp(LocalDateTime.now());
        return businessContext;
    }

}
