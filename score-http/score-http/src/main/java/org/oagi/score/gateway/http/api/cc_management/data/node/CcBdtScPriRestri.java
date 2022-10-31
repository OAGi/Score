package org.oagi.score.gateway.http.api.cc_management.data.node;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CcBdtScPriRestri {

    private BigInteger bdtScPriRestriId;
    private PrimitiveRestriType type;
    private BigInteger cdtScAwdPriId;
    private String primitiveName;

    private BigInteger cdtScAwdPriXpsTypeMapId;
    private BigInteger xbtId;
    private String xbtName;

    private BigInteger codeListManifestId;
    private String codeListName;

    private BigInteger agencyIdListManifestId;
    private String agencyIdListName;

    private boolean isDefault;

    private boolean inherited;
}
