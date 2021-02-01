package org.oagi.score.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class ABIE implements BIE {

    private BigInteger abieId = BigInteger.ZERO;
    private String guid;
    private BigInteger basedAccManifestId = BigInteger.ZERO;
    private String definition;
    private BigInteger createdBy = BigInteger.ZERO;
    private BigInteger lastUpdatedBy = BigInteger.ZERO;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private Integer state;
    private BigInteger clientId = BigInteger.ZERO;
    private String version;
    private String status;
    private String remark;
    private String bizTerm;
    private BigInteger ownerTopLevelAsbiepId = BigInteger.ZERO;

}
