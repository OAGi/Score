package org.oagi.score.gateway.http.api.cc_management.data.node;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;
import org.oagi.score.service.common.data.CcState;

import java.math.BigInteger;

@Data
public class CcBccpNodeDetail implements CcNodeDetail {

    private CcType type = CcType.BCCP;
    private Bcc bcc;
    private Bccp bccp;
    private Bdt bdt;

    @Data
    public static class Bcc {
        private BigInteger bccId = BigInteger.ZERO;
        private BigInteger manifestId = BigInteger.ZERO;
        private String guid;
        private String den;
        private Integer entityType;
        private int cardinalityMin;
        private int cardinalityMax;
        private boolean deprecated;
        private BigInteger replacementBccManifestId;
        private Bcc replacement;
        private boolean nillable;
        private String defaultValue;
        private String fixedValue;
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
    public static class Bccp {
        private BigInteger bccpId = BigInteger.ZERO;
        private BigInteger manifestId = BigInteger.ZERO;
        private String guid;
        private String propertyTerm;
        private String den;
        private boolean nillable;
        private boolean deprecated;
        private BigInteger replacementBccpManifestId;
        private Bccp replacement;
        private BigInteger namespaceId = BigInteger.ZERO;
        private String defaultValue;
        private String fixedValue;
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
    public static class Bdt {
        private BigInteger bdtId = BigInteger.ZERO;
        private BigInteger manifestId;
        private String guid;
        private String dataTypeTerm;
        private String representationTerm;
        private String qualifier;
        private BigInteger namespaceId;
        private String den;
        private String definition;
        private String definitionSource;
        private boolean hasNoSc;

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
