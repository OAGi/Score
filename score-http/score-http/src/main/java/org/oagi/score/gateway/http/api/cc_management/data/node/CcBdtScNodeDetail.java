package org.oagi.score.gateway.http.api.cc_management.data.node;

import lombok.Data;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;

import java.math.BigInteger;
import java.util.List;

@Data
public class CcBdtScNodeDetail implements CcNodeDetail {

    private CcType type = CcType.DT_SC;
    private BigInteger manifestId = BigInteger.ZERO;
    private BigInteger bdtScId = BigInteger.ZERO;
    private String guid;
    private String den;
    private int cardinalityMin;
    private int cardinalityMax;
    private BigInteger prevCardinalityMin;
    private BigInteger prevCardinalityMax;
    private BigInteger baseCardinalityMin;
    private BigInteger baseCardinalityMax;
    private String definition;
    private String definitionSource;
    private String defaultValue;
    private String fixedValue;

    private String objectClassTerm;
    private String propertyTerm;
    private String representationTerm;
    private Boolean deprecated;

    private CcState state;
    private String owner;
    private BigInteger releaseId;
    private String releaseNum;
    private BigInteger logId;
    private int revisionNum;
    private int revisionTrackingNum;
    private String spec;
    private BigInteger basedDtManifestId;
    private BigInteger basedDtScManifestId;
    private BigInteger basedDtScId;

    private List<CcBdtScPriRestri> bdtScPriRestriList;
}
