package org.oagi.score.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class BizCtx {

    private BigInteger bizCtxId = BigInteger.ZERO;
    private String guid;
    private String name;
    private BigInteger createdBy = BigInteger.ZERO;
    private BigInteger lastUpdatedBy = BigInteger.ZERO;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
}
