package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

import java.math.BigInteger;

@Data
public class BieEditAgencyIdList {

    private BigInteger agencyIdListId;
    private boolean isDefault;
    private String agencyIdListName;

}
