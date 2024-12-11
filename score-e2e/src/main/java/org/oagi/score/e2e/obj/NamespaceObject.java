package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;

@Data
public class NamespaceObject {

    private BigInteger namespaceId;

    private BigInteger libraryId;

    private String uri;

    private String prefix;

    private String description;

    private boolean standardNamespace;

    private BigInteger ownerUserId;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

    public static NamespaceObject createRandomNamespace(AppUserObject creator) {
        NamespaceObject namespace = new NamespaceObject();
        String randomDomain = randomAlphabetic(5, 10);
        namespace.setUri("https://test." + randomDomain + ".com");
        namespace.setPrefix(randomDomain);
        namespace.setDescription(randomPrint(50, 100).trim());
        namespace.setStandardNamespace(creator.isDeveloper());
        namespace.setOwnerUserId(creator.getAppUserId());
        namespace.setCreatedBy(creator.getAppUserId());
        namespace.setLastUpdatedBy(creator.getAppUserId());
        namespace.setCreationTimestamp(LocalDateTime.now());
        namespace.setLastUpdateTimestamp(LocalDateTime.now());
        return namespace;
    }

}
