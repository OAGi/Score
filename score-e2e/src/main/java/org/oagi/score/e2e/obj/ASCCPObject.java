package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomPrint;

@Data
public class ASCCPObject {

    private BigInteger asccpManifestId;

    private BigInteger asccpId;

    private BigInteger releaseId;

    private String guid;

    private String propertyTerm;

    private String den;

    private String definition;

    private String definitionSource;

    private BigInteger roleOfAccManifestId;

    private BigInteger namespaceId;

    private boolean deprecated;

    private boolean nillable;

    private boolean reusable;

    private String state;

    private BigInteger ownerUserId;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

    public static ASCCPObject createRandomASCCP(ACCObject roleOfAcc, AppUserObject creator,
                                                NamespaceObject namespace, String state) {
        ASCCPObject asccp = new ASCCPObject();
        asccp.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        String objectClassTerm = roleOfAcc.getObjectClassTerm();
        asccp.setRoleOfAccManifestId(roleOfAcc.getAccManifestId());
        asccp.setPropertyTerm(objectClassTerm);
        asccp.setDen(asccp.getPropertyTerm() + ". " + asccp.getPropertyTerm());
        asccp.setDefinition(randomPrint(50, 100).trim());
        asccp.setDefinitionSource(randomPrint(50, 100).trim());
        asccp.setNamespaceId(namespace.getNamespaceId());
        asccp.setDeprecated(false);
        asccp.setNillable(false);
        asccp.setReusable(true);
        asccp.setState(state);
        asccp.setOwnerUserId(creator.getAppUserId());
        asccp.setCreatedBy(creator.getAppUserId());
        asccp.setLastUpdatedBy(creator.getAppUserId());
        asccp.setCreationTimestamp(LocalDateTime.now());
        asccp.setLastUpdateTimestamp(LocalDateTime.now());
        return asccp;
    }

}
