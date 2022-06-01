package org.oagi.score.gateway.http.api.agency_id_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Data
public class GetSimpleAgencyIdListValuesResponse {

    private List<SimpleAgencyIdList> agencyIdLists;
    private List<SimpleAgencyIdListValue> agencyIdListValues;

}
