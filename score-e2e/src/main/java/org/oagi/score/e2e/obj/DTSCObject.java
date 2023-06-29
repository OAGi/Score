package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class DTSCObject {

    private BigInteger dtSCId;
    private String guid;

    private String objectClassTerm;

    private String propertyTerm;

    private String representationTerm;

    private BigInteger basedDtManifestId;

    private BigInteger basedDtId;

    private String definition;

    private String definitionSource;

    private BigInteger ownerDTId;

    private Integer cardinalityMin;

    private Integer cardinalityMax;

    private BigInteger basedDTSCId;

    private String defaultValue;

    private String fixedValue;

    private boolean deprecated;

    private BigInteger replacementDTSCId;

    private BigInteger ownerUserId;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

    private BigInteger previousDTSCId;

    private BigInteger nextDTSCId;
}
