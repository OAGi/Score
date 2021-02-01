package org.oagi.score.gateway.http.api.cc_management.data.node;

import lombok.Data;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;

import java.math.BigInteger;

@Data
public class CcBdtScNodeDetail implements CcNodeDetail {

    private CcType type = CcType.BDT_SC;
    private BigInteger manifestId = BigInteger.ZERO;
    private BigInteger bdtScId = BigInteger.ZERO;
    private String guid;
    private String den;
    private int cardinalityMin;
    private int cardinalityMax;
    private String definition;
    private String definitionSource;
    private String defaultValue;
    private String fixedValue;

    private CcState state;
    private String owner;
    private BigInteger releaseId;
    private String releaseNum;
    private BigInteger logId;
    private int revisionNum;
    private int revisionTrackingNum;
}
