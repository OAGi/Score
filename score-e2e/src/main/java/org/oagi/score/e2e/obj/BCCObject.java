package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomPrint;

@Data
public class BCCObject {

    private BigInteger bccManifestId;

    private BigInteger bccId;

    private BigInteger fromAccManifestId;

    private BigInteger toBccpManifestId;

    private BigInteger releaseId;

    private String guid;

    private int cardinalityMin = 0;

    private int cardinalityMax = -1;

    private String den;

    private String definition;

    private String definitionSource;

    private boolean attribute;

    private boolean deprecated;

    private boolean nillable;

    private String defaultValue;

    private String fixedValue;

    private String state;

    private BigInteger ownerUserId;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

    public static BCCObject createRandomBCC(ACCObject fromAcc, BCCPObject toBccp, String state) {
        BCCObject bcc = new BCCObject();

        bcc.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        bcc.setCardinalityMin(0);
        bcc.setCardinalityMax(-1);
        bcc.setDen(fromAcc.getObjectClassTerm() + ". " + toBccp.getDen());
        bcc.setDefinition(randomPrint(50, 100).trim());
        bcc.setDefinitionSource(randomPrint(50, 100).trim());
        bcc.setAttribute(false);
        bcc.setDeprecated(false);
        bcc.setState(state);
        bcc.setOwnerUserId(fromAcc.getOwnerUserId());
        bcc.setCreatedBy(fromAcc.getCreatedBy());
        bcc.setLastUpdatedBy(fromAcc.getLastUpdatedBy());
        bcc.setCreationTimestamp(LocalDateTime.now());
        bcc.setLastUpdateTimestamp(LocalDateTime.now());

        return bcc;
    }

}
