package org.oagi.score.e2e.obj;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.*;

@Data
public class BusinessTermObject {
    private String bieBiztermId;
    private String bieId;
    private String primaryIndicator;
    private String typeCode;
    private String businessTerm;
    private String guid;
    private String externalReferenceUri;

    public static BusinessTermObject createRandomBusinessTerm(AppUserObject creator) {
        return createRandomBusinessTerm(creator, "bt");
    }

    public static BusinessTermObject createRandomBusinessTerm(AppUserObject creator, String namePrefix) {
        BusinessTermObject businessTerm = new BusinessTermObject();
        businessTerm.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        businessTerm.setBusinessTerm(namePrefix + "_" + randomAlphanumeric(5, 10));
        businessTerm.setExternalReferenceUri("http://www." + randomAscii(3,8) + ".com" + businessTerm.getBieBiztermId());
        businessTerm.setBieBiztermId(randomNumeric(1,10));
        return businessTerm;
    }

}
