package org.oagi.score.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class AgencyIdListValue {

    private BigInteger agencyIdListValueId = BigInteger.ZERO;
    private String value;
    private String name;
    private String definition;
    private BigInteger ownerListId = BigInteger.ZERO;
}
