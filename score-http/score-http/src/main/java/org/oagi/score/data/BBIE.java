package org.oagi.score.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class BBIE implements BIE, FacetRestrictionsAware {

    private BigInteger bbieId = BigInteger.ZERO;
    private String guid;
    private BigInteger basedBccManifestId = BigInteger.ZERO;
    private BigInteger fromAbieId = BigInteger.ZERO;
    private BigInteger toBbiepId = BigInteger.ZERO;
    private BigInteger bdtPriRestriId = BigInteger.ZERO;
    private BigInteger codeListManifestId = BigInteger.ZERO;
    private BigInteger agencyIdListManifestId = BigInteger.ZERO;
    private int cardinalityMin;
    private int cardinalityMax;
    private BigInteger facetMinLength;
    private BigInteger facetMaxLength;
    private String facetPattern;
    private String defaultValue;
    private boolean nillable;
    private String fixedValue;
    private boolean nill;
    private boolean deprecated;
    private String definition;
    private String remark;
    private String example;
    private BigInteger createdBy = BigInteger.ZERO;
    private BigInteger lastUpdatedBy = BigInteger.ZERO;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private double seqKey;
    private boolean used;
    private BigInteger ownerTopLevelAsbiepId = BigInteger.ZERO;
}
