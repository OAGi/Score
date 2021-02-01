package org.oagi.score.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class BBIEP implements BIE {

    private BigInteger bbiepId = BigInteger.ZERO;
    private String guid;
    private BigInteger basedBccpManifestId = BigInteger.ZERO;
    private String definition;
    private String remark;
    private String bizTerm;
    private BigInteger createdBy = BigInteger.ZERO;
    private BigInteger lastUpdatedBy = BigInteger.ZERO;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private BigInteger ownerTopLevelAsbiepId = BigInteger.ZERO;
}
