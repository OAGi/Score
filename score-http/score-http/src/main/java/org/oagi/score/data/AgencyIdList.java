package org.oagi.score.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class AgencyIdList {

    private BigInteger agencyIdListId = BigInteger.ZERO;
    private String guid;
    private String enumTypeGuid;
    private String name;
    private String listId;
    private BigInteger agencyIdListValueId = BigInteger.ZERO;
    private String versionId;
    private BigInteger moduleId = BigInteger.ZERO;
    private String definition;
}
