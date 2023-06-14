package org.oagi.score.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class Xbt {

    private BigInteger xbtId = BigInteger.ZERO;
    private BigInteger manifestId = BigInteger.ZERO;
    private String name;
    private String builtinType;
    private String jbtDraft05Map;
    private String openapi30Map;
    private String avroMap;
    private BigInteger subtypeOfXbtId = BigInteger.ZERO;
    private String schemaDefinition;
    private BigInteger releaseId = BigInteger.ZERO;
    private Integer state;
    private BigInteger createdBy = BigInteger.ZERO;
    private BigInteger ownerUserId = BigInteger.ZERO;
    private BigInteger lastUpdatedBy = BigInteger.ZERO;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private Boolean deprecated;

}
