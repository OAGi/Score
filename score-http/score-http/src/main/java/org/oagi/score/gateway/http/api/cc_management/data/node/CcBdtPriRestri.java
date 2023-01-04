package org.oagi.score.gateway.http.api.cc_management.data.node;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CcBdtPriRestri {

    private BigInteger bdtPriRestriId;
    private PrimitiveRestriType type;
    private BigInteger cdtAwdPriId;
    private String primitiveName;

    private BigInteger cdtAwdPriXpsTypeMapId;
    private BigInteger xbtId;
    private String xbtName;

    private BigInteger codeListManifestId;
    private String codeListName;

    private BigInteger agencyIdListManifestId;
    private String agencyIdListName;

    private boolean isDefault;

    private boolean inherited;
}
