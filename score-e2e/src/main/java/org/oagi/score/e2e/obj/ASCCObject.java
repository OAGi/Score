package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomPrint;

@Data
public class ASCCObject {

    private BigInteger asccManifestId;

    private BigInteger asccId;

    private BigInteger fromAccManifestId;

    private BigInteger toAsccpManifestId;

    private BigInteger releaseId;

    private String guid;

    private int cardinalityMin = 0;

    private int cardinalityMax = -1;

    private String den;

    private String definition;

    private String definitionSource;

    private boolean deprecated;

    private String state;

    private BigInteger ownerUserId;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

    public static ASCCObject createRandomASCC(ACCObject fromAcc, ASCCPObject toAsccp, String state) {
        ASCCObject ascc = new ASCCObject();

        ascc.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        ascc.setCardinalityMin(0);
        ascc.setCardinalityMax(-1);
        ascc.setDen(fromAcc.getObjectClassTerm() + ". " + toAsccp.getDen());
        ascc.setDefinition(randomPrint(50, 100).trim());
        ascc.setDefinitionSource(randomPrint(50, 100).trim());
        ascc.setDeprecated(false);
        ascc.setState(state);
        ascc.setOwnerUserId(fromAcc.getOwnerUserId());
        ascc.setCreatedBy(fromAcc.getCreatedBy());
        ascc.setLastUpdatedBy(fromAcc.getLastUpdatedBy());
        ascc.setCreationTimestamp(LocalDateTime.now());
        ascc.setLastUpdateTimestamp(LocalDateTime.now());

        return ascc;
    }

}
