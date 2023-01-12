package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;

@Data
public class ContextCategoryObject {

    private BigInteger contextCategoryId;

    private String guid;

    private String name;

    private String description;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

    public static ContextCategoryObject newRandomContextCategory(AppUserObject creator) {
        return newRandomContextCategory(creator, "cat");
    }

    public static ContextCategoryObject newRandomContextCategory(AppUserObject creator, String namePrefix) {
        ContextCategoryObject contextCategory = new ContextCategoryObject();
        contextCategory.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        contextCategory.setName(namePrefix + "_" + randomAlphanumeric(5, 10));
        contextCategory.setDescription(randomPrint(50, 100).trim());
        contextCategory.setCreatedBy(creator.getAppUserId());
        contextCategory.setLastUpdatedBy(creator.getAppUserId());
        contextCategory.setCreationTimestamp(LocalDateTime.now());
        contextCategory.setLastUpdateTimestamp(contextCategory.getCreationTimestamp());
        return contextCategory;
    }

}
