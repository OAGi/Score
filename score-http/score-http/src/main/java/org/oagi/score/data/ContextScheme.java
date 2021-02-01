package org.oagi.score.data;

import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

@Data
public class ContextScheme implements Serializable {

    private BigInteger ctxSchemeId = BigInteger.ZERO;
    private String guid;
    private String schemeId;
    private String schemeName;
    private String description;
    private String schemeAgencyId;
    private String schemeVersionId;
    private BigInteger ctxCategoryId = BigInteger.ZERO;
    private BigInteger createdBy = BigInteger.ZERO;
    private BigInteger lastUpdatedBy = BigInteger.ZERO;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
}
