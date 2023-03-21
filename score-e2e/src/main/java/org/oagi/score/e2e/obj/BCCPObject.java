package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;

@Data
public class BCCPObject {

    private BigInteger bccpManifestId;

    private BigInteger bccpId;

    private BigInteger bdtId;

    private BigInteger bdtManifestId;

    private BigInteger releaseId;

    private String propertyTerm;

    private String representationTerm;

    private String den;

    private String guid;

    private String definition;

    private String definitionSource;

    private String defaultValue;

    private String fixedValue;

    private BigInteger namespaceId;

    private boolean deprecated;

    private boolean nillable;

    private String state;

    private BigInteger ownerUserId;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

    public static BCCPObject createRandonBCCP(DTObject dataType, AppUserObject creator, NamespaceObject namespace, String state) {
        BCCPObject bccp = new BCCPObject();
        bccp.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        String randomPropertyTerm = randomAlphabetic(5, 10).replaceAll(" ", "");
        randomPropertyTerm = Character.toUpperCase(randomPropertyTerm.charAt(0)) + randomPropertyTerm.substring(1).toLowerCase();
        bccp.setPropertyTerm("Test Object " + randomPropertyTerm);
        bccp.setRepresentationTerm(dataType.getRepresentationTerm());
        bccp.setBdtId(dataType.getDtId());
        bccp.setDen(bccp.getPropertyTerm() + ". " + dataType.getRepresentationTerm());
        bccp.setDefinition(randomPrint(50, 100).trim());
        bccp.setDefinitionSource(randomPrint(50, 100).trim());
        bccp.setNamespaceId(namespace.getNamespaceId());
        bccp.setDeprecated(false);
        bccp.setState(state);
        bccp.setOwnerUserId(creator.getAppUserId());
        bccp.setCreatedBy(creator.getAppUserId());
        bccp.setLastUpdatedBy(creator.getAppUserId());
        bccp.setCreationTimestamp(LocalDateTime.now());
        bccp.setLastUpdateTimestamp(LocalDateTime.now());
        bccp.setNillable(true);
        return bccp;
    }
}
