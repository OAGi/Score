package org.oagi.score.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class CodeList {

    private BigInteger codeListManifestId = BigInteger.ZERO;
    private BigInteger codeListId = BigInteger.ZERO;
    private String guid;
    private String enumTypeGuid;
    private String name;
    private String listId;
    private BigInteger agencyIdListValueId = BigInteger.ZERO;
    private String versionId;
    private String definition;
    private String remark;
    private String definitionSource;
    private BigInteger basedCodeListId = BigInteger.ZERO;
    private boolean extensibleIndicator;
    private BigInteger moduleId = BigInteger.ZERO;
    private BigInteger createdBy = BigInteger.ZERO;
    private BigInteger lastUpdatedBy = BigInteger.ZERO;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private String state;
}
