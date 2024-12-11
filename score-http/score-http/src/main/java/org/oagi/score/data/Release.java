package org.oagi.score.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class Release {

    private BigInteger releaseId = BigInteger.ZERO;
    private String guid;
    private String releaseNum;
    private String releaseNote;
    private String releaseLicense;
    private BigInteger libraryId = BigInteger.ZERO;
    private BigInteger namespaceId = BigInteger.ZERO;
    private BigInteger createdBy = BigInteger.ZERO;
    private BigInteger lastUpdatedBy = BigInteger.ZERO;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private String state;

}
