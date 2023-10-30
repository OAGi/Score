package org.oagi.score.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class BBIESC implements BIE, FacetRestrictionsAware {

    private BigInteger bbieScId = BigInteger.ZERO;
    private String guid;
    private BigInteger bbieId = BigInteger.ZERO;
    private BigInteger basedDtScManifestId = BigInteger.ZERO;
    private BigInteger dtScPriRestriId = BigInteger.ZERO;
    private BigInteger codeListManifestId = BigInteger.ZERO;
    private BigInteger agencyIdListManifestId = BigInteger.ZERO;
    private int cardinalityMin;
    private int cardinalityMax;
    private BigInteger facetMinLength;
    private BigInteger facetMaxLength;
    private String facetPattern;
    private String defaultValue;
    private String fixedValue;
    private String definition;
    private String remark;
    private String bizTerm;
    private String example;
    private boolean deprecated;
    private boolean used;
    private BigInteger ownerTopLevelAsbiepId = BigInteger.ZERO;
}
