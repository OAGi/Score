package org.oagi.score.gateway.http.api.cc_management.data.node;

import lombok.Data;
import org.oagi.score.data.BdtPriRestri;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;
import org.oagi.score.service.common.data.CcState;

import java.math.BigInteger;
import java.util.List;

@Data
public class CcBdtNodeDetail implements CcNodeDetail {

    private CcType type = CcType.BDT;

    private BigInteger bdtId = BigInteger.ZERO;
    private BigInteger manifestId;
    private String guid;
    private String dtType;
    private String versionNum;
    private String dataTypeTerm;
    private String qualifier;
    private BigInteger basedBdtId;
    private BigInteger basedBdtManifestId;
    private String basedBdtDen;
    private String contentComponentDen;
    private String contentComponentDefinition;
    private boolean commonlyUsed;
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

    private List<CcBdtPriResri> bdtPriRestriList;
}
