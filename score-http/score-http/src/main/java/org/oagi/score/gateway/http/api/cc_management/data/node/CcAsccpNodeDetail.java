package org.oagi.score.gateway.http.api.cc_management.data.node;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;
import org.oagi.score.service.common.data.CcState;

import java.math.BigInteger;

@Data
public class CcAsccpNodeDetail implements CcNodeDetail {

    private CcType type = CcType.ASCCP;
    private Ascc ascc;
    private Asccp asccp;

    @Data
    public static class Ascc {
        private BigInteger manifestId = BigInteger.ZERO;
        private BigInteger asccId = BigInteger.ZERO;
        private String guid;
        private String den;
        private int cardinalityMin;
        private int cardinalityMax;
        private boolean deprecated;
        private BigInteger replacementAsccManifestId;
        private Ascc replacement;
        private String definition;
        private String definitionSource;

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

    @Data
    public static class Asccp {
        private BigInteger manifestId = BigInteger.ZERO;
        private BigInteger asccpId = BigInteger.ZERO;
        private String guid;
        private String propertyTerm;
        private String den;
        private BigInteger namespaceId = BigInteger.ZERO;
        private boolean reusable;
        private boolean deprecated;
        private BigInteger replacementAsccpManifestId;
        private Asccp replacement;
        private boolean nillable;
        private String definition;
        private String definitionSource;

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
}
