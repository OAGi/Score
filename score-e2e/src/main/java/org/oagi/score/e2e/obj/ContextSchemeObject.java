package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;

@Data
public class ContextSchemeObject {

    private BigInteger contextSchemeId;

    private String guid;

    private String schemeId;

    private String schemeName;

    private String description;

    private String schemeAgencyId;

    private String schemeVersionId;

    private BigInteger contextCategoryId;

    private BigInteger codeListId;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

    public static ContextSchemeObject createRandomContextScheme(ContextCategoryObject contextCategory,
                                                                AppUserObject creator) {
        return createRandomContextScheme(contextCategory, creator, "cs");
    }

    public static ContextSchemeObject createRandomContextScheme(ContextCategoryObject contextCategory,
                                                                AppUserObject creator, String namePrefix) {
        ContextSchemeObject contextScheme = new ContextSchemeObject();
        contextScheme.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        contextScheme.setSchemeId("cs_scheme_id" + randomAlphanumeric(5, 10));
        contextScheme.setSchemeName(namePrefix + "_" + randomAlphanumeric(5, 10));
        contextScheme.setDescription(randomPrint(50, 100).trim());
        contextScheme.setSchemeAgencyId("cs_agency_id_" + randomAlphanumeric(5, 10));
        contextScheme.setSchemeVersionId("cs_version_id_" + randomAlphanumeric(5, 10));
        contextScheme.setContextCategoryId(contextCategory.getContextCategoryId());
        contextScheme.setCreatedBy(creator.getAppUserId());
        contextScheme.setLastUpdatedBy(creator.getAppUserId());
        contextScheme.setCreationTimestamp(LocalDateTime.now());
        contextScheme.setLastUpdateTimestamp(contextCategory.getCreationTimestamp());
        return contextScheme;
    }
}
