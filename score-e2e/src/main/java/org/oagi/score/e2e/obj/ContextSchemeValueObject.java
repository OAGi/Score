package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;

@Data
public class ContextSchemeValueObject {

    private BigInteger contextSchemeValueId;

    private String guid;

    private String value;

    private String meaning;

    private BigInteger ownerContextSchemeId;

    public static ContextSchemeValueObject createRandomContextSchemeValue(ContextSchemeObject contextScheme) {
        ContextSchemeValueObject contextSchemeValue = new ContextSchemeValueObject();
        contextSchemeValue.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        contextSchemeValue.setValue("csv_" + randomAlphanumeric(5, 10));
        contextSchemeValue.setMeaning(randomPrint(50, 100).trim());
        contextSchemeValue.setOwnerContextSchemeId(contextScheme.getContextSchemeId());
        return contextSchemeValue;
    }


}
