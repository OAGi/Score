package org.oagi.score.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class AgencyIdListValue {

    private BigInteger agencyIdListValueManifestId = BigInteger.ZERO;
    private BigInteger agencyIdListValueId = BigInteger.ZERO;
    private String value;
    private String name;
    private String definition;
    private BigInteger agencyIdListManifestId = BigInteger.ZERO;
    private BigInteger ownerListId = BigInteger.ZERO;

}
