package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.*;

@Data
public class BusinessTermObject {
    private BigInteger businessTermId;

    private String businessTerm;

    private String definition;

    private String comment;

    private String externalReferenceUri;

    private String externalReferenceId;

    private String guid;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    public static BusinessTermObject createRandomBusinessTerm(AppUserObject creator) {
        return createRandomBusinessTerm(creator, "bt");
    }

    public static BusinessTermObject createRandomBusinessTerm(AppUserObject creator, String namePrefix) {
        BusinessTermObject businessTerm = new BusinessTermObject();
        businessTerm.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        businessTerm.setBusinessTerm(namePrefix + "_" + randomAlphanumeric(5, 10));
        businessTerm.setExternalReferenceUri("http://www." + randomAscii(3,8) + ".com" + businessTerm.getExternalReferenceId());
        businessTerm.setExternalReferenceId(randomNumeric(1,10));
        businessTerm.setDefinition(randomPrint(50, 100).trim());
        businessTerm.setComment(randomPrint(20,50).trim());
        businessTerm.setCreatedBy(creator.getAppUserId());
        businessTerm.setLastUpdatedBy(creator.getAppUserId());
        businessTerm.setCreationTimestamp(LocalDateTime.now());
        businessTerm.setLastUpdateTimestamp(LocalDateTime.now());
        return businessTerm;
    }

}
