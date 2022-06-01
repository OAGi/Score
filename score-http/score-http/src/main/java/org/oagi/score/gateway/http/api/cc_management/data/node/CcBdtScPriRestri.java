package org.oagi.score.gateway.http.api.cc_management.data.node;

import lombok.Data;

import java.math.BigInteger;
import java.util.List;

@Data
public class CcBdtScPriRestri {

    private BigInteger bdtScPriRestriId;
    private PrimitiveRestriType type;
    private BigInteger cdtScAwdPriId;
    private String primitiveName;

    private BigInteger cdtScAwdPriXpsTypeMapId;
    private BigInteger xbtId;
    private String xbtName;

    private BigInteger codeListId;
    private String codeListName;

    private BigInteger agencyIdListId;
    private String agencyIdListName;

    private boolean isDefault;

    private boolean inherited;
}
