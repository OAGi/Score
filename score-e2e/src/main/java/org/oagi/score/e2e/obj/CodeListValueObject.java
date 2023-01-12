package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;

@Data
public class CodeListValueObject {

    private BigInteger codeListValueManifestId;

    private BigInteger codeListValueId;

    private BigInteger basedCodeListValueManifestId;

    private BigInteger codeListManifestId;

    private BigInteger releaseId;

    private String guid;

    private String value;

    private String meaning;

    private String definition;

    private String definitionSource;

    private boolean deprecated;

    private BigInteger ownerUserId;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

    public static CodeListValueObject createRandomCodeListValue(CodeListObject codeList, AppUserObject creator) {
        CodeListValueObject codeListValue = new CodeListValueObject();
        codeListValue.setCodeListManifestId(codeList.getCodeListManifestId());
        codeListValue.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        codeListValue.setValue("clv_value_" + randomAlphanumeric(5, 10));
        codeListValue.setMeaning("clv_meaning_" + randomAlphanumeric(5, 10));
        codeListValue.setDefinition(randomPrint(50, 100).trim());
        codeListValue.setDefinitionSource("cl_description_source_" + randomAlphanumeric(5, 10));
        codeListValue.setDeprecated(false);
        codeListValue.setOwnerUserId(creator.getAppUserId());
        codeListValue.setCreatedBy(creator.getAppUserId());
        codeListValue.setLastUpdatedBy(creator.getAppUserId());
        codeListValue.setCreationTimestamp(LocalDateTime.now());
        codeListValue.setLastUpdateTimestamp(LocalDateTime.now());
        return codeListValue;
    }

    public static CodeListValueObject createDerivedCodeListValue(CodeListValueObject baseCodeListValue,
                                                                 CodeListObject codeList, AppUserObject creator) {
        CodeListValueObject codeListValue = new CodeListValueObject();
        codeListValue.setBasedCodeListValueManifestId(baseCodeListValue.getCodeListValueManifestId());
        codeListValue.setCodeListManifestId(codeList.getCodeListManifestId());
        codeListValue.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        codeListValue.setValue(baseCodeListValue.getValue());
        codeListValue.setMeaning(baseCodeListValue.getMeaning());
        codeListValue.setDefinition(baseCodeListValue.getDefinition());
        codeListValue.setDefinitionSource(baseCodeListValue.getDefinitionSource());
        codeListValue.setDeprecated(false);
        codeListValue.setOwnerUserId(creator.getAppUserId());
        codeListValue.setCreatedBy(creator.getAppUserId());
        codeListValue.setLastUpdatedBy(creator.getAppUserId());
        codeListValue.setCreationTimestamp(LocalDateTime.now());
        codeListValue.setLastUpdateTimestamp(LocalDateTime.now());
        return codeListValue;
    }

}
