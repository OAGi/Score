package org.oagi.score.gateway.http.api.cc_management.data.node;

import lombok.Data;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;

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
    private BigInteger namespaceId;

    private CcState state;
    private String owner;
    private BigInteger releaseId;
    private String releaseNum;
    private BigInteger logId;
    private int revisionNum;
    private int revisionTrackingNum;
}
