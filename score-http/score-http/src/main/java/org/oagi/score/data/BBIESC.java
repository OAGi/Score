package org.oagi.score.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class BBIESC implements BIE {

    private BigInteger bbieScId = BigInteger.ZERO;
    private String guid;
    private BigInteger bbieId = BigInteger.ZERO;
    private BigInteger basedDtScManifestId = BigInteger.ZERO;
    private BigInteger dtScPriRestriId = BigInteger.ZERO;
    private BigInteger codeListId = BigInteger.ZERO;
    private BigInteger agencyIdListId = BigInteger.ZERO;
    private int cardinalityMin;
    private int cardinalityMax;
    private String defaultValue;
    private String fixedValue;
    private String definition;
    private String remark;
    private String bizTerm;
    private String example;
    private boolean used;
    private BigInteger ownerTopLevelAsbiepId = BigInteger.ZERO;
}
