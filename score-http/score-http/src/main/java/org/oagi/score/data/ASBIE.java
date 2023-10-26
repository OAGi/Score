package org.oagi.score.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class ASBIE implements BIE {

    private BigInteger asbieId = BigInteger.ZERO;
    private String guid;
    private BigInteger fromAbieId = BigInteger.ZERO;
    private BigInteger toAsbiepId = BigInteger.ZERO;
    private BigInteger basedAsccManifestId = BigInteger.ZERO;
    private String definition;
    private int cardinalityMin;
    private int cardinalityMax;
    private boolean nillable;
    private boolean deprecated;
    private String remark;
    private BigInteger createdBy = BigInteger.ZERO;
    private BigInteger lastUpdatedBy = BigInteger.ZERO;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private double seqKey;
    private boolean used;
    private BigInteger ownerTopLevelAsbiepId = BigInteger.ZERO;
}
