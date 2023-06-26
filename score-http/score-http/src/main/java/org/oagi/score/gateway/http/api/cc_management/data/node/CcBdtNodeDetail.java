package org.oagi.score.gateway.http.api.cc_management.data.node;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;
import org.oagi.score.service.common.data.CcState;

import java.math.BigInteger;
import java.util.List;

@Data
public class CcBdtNodeDetail implements CcNodeDetail {

    private CcType type = CcType.DT;

    private BigInteger bdtId = BigInteger.ZERO;
    private BigInteger manifestId;
    private String guid;
    private String dataTypeTerm;
    private String representationTerm;
    private String sixDigitId;
    private String qualifier;
    private BigInteger basedBdtId;
    private BigInteger basedBdtManifestId;
    private String basedBdtDen;
    private String basedBdtState;
    private String contentComponentDefinition;
    private boolean commonlyUsed;
    private boolean deprecated;
    private BigInteger replacementDtManifestId;
    private CcBdtNodeDetail replacement;
    private String den;
    private String definition;
    private String definitionSource;
    private boolean hasNoSc;
    private BigInteger namespaceId;

    private CcState state;
    private String owner;
    private BigInteger releaseId;
    private String releaseNum;
    private BigInteger logId;
    private int revisionNum;
    private int revisionTrackingNum;
    private String spec;

    private BigInteger sinceManifestId = BigInteger.ZERO;
    private BigInteger sinceReleaseId = BigInteger.ZERO;
    private String sinceReleaseNum;
    private BigInteger lastChangedManifestId = BigInteger.ZERO;
    private BigInteger lastChangedReleaseId = BigInteger.ZERO;
    private String lastChangedReleaseNum;

    private List<CcBdtPriRestri> bdtPriRestriList;
}
