package org.oagi.score.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.oagi.score.service.common.data.CcState;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DT implements CoreComponent, FacetRestrictionsAware {

    private BigInteger dtManifestId = BigInteger.ZERO;
    private BigInteger dtId = BigInteger.ZERO;
    private String guid;
    private int type;
    private String dataTypeTerm;
    private String representationTerm;
    private String qualifier;
    private BigInteger facetMinLength;
    private BigInteger facetMaxLength;
    private String facetPattern;
    private String facetMinInclusive;
    private String facetMinExclusive;
    private String facetMaxInclusive;
    private String facetMaxExclusive;
    private String sixDigitId;
    private BigInteger basedDtManifestId = BigInteger.ZERO;
    private BigInteger basedDtId = BigInteger.ZERO;
    private String den;
    private String definition;
    private String definitionSource;
    private String contentComponentDefinition;
    private CcState state;
    private BigInteger releaseId = BigInteger.ZERO;
    private String releaseNum;
    private BigInteger logId = BigInteger.ZERO;
    private int revisionNum;
    private int revisionTrackingNum;
    private BigInteger createdBy = BigInteger.ZERO;
    private BigInteger ownerUserId = BigInteger.ZERO;
    private BigInteger lastUpdatedBy = BigInteger.ZERO;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private boolean deprecated;

    @Override
    public BigInteger getId() {
        return getDtId();
    }
}
