package org.oagi.score.gateway.http.api.cc_management.data.node;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;
import org.oagi.score.service.common.data.CcState;

import java.math.BigInteger;

@Data
public class CcAccNodeDetail implements CcNodeDetail {
    private CcType type = CcType.ACC;
    private BigInteger accId;
    private String guid;
    private String objectClassTerm;
    private String den;
    private int oagisComponentType;
    private boolean abstracted;
    private boolean deprecated;
    private String definition;
    private String definitionSource;
    private BigInteger manifestId;
    private BigInteger replacementAccManifestId;
    private CcAccNodeDetail replacement;
    private BigInteger namespaceId;

    private CcState state;
    private String owner;
    private BigInteger releaseId;
    private String releaseNum;
    private BigInteger logId;
    private int revisionNum;
    private int revisionTrackingNum;

    private BigInteger sinceManifestId = BigInteger.ZERO;
    private BigInteger sinceReleaseId = BigInteger.ZERO;
    private String sinceReleaseNum;
}
