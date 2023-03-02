package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;

@Data
public class ACCObject {

    private BigInteger accManifestId;

    private BigInteger accId;

    private BigInteger basedAccManifestId;

    private BigInteger releaseId;

    private String guid;

    private String objectClassTerm;

    private ComponentType componentType;

    private String den;

    private String definition;

    private String definitionSource;

    private BigInteger namespaceId;

    private boolean isAbstract;

    private boolean deprecated;

    private String state;

    private BigInteger ownerUserId;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

    public static ACCObject createRandomACC(AppUserObject creator, NamespaceObject namespace, String state) {
        ACCObject acc = new ACCObject();
        acc.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        String randomObjectClassTerm = randomAlphabetic(5, 10).replaceAll(" ", "");
        randomObjectClassTerm = Character.toUpperCase(randomObjectClassTerm.charAt(0)) + randomObjectClassTerm.substring(1).toLowerCase();
        acc.setObjectClassTerm("Test Object " + randomObjectClassTerm);
        acc.setComponentType(ComponentType.Semantics);
        acc.setDen(acc.getObjectClassTerm() + ". Details");
        acc.setDefinition(randomPrint(50, 100).trim());
        acc.setDefinitionSource(randomPrint(50, 100).trim());
        acc.setNamespaceId(namespace.getNamespaceId());
        acc.setAbstract(false);
        acc.setDeprecated(false);
        acc.setState(state);
        acc.setOwnerUserId(creator.getAppUserId());
        acc.setCreatedBy(creator.getAppUserId());
        acc.setLastUpdatedBy(creator.getAppUserId());
        acc.setCreationTimestamp(LocalDateTime.now());
        acc.setLastUpdateTimestamp(LocalDateTime.now());
        return acc;
    }

}
