package org.oagi.score.gateway.http.api.agency_id_management.data;

import lombok.Data;

import java.util.List;

@Data
public class GetSimpleAgencyIdListValuesResponse {

    private List<SimpleAgencyIdList> agencyIdLists;
    private List<SimpleAgencyIdListValue> agencyIdListValues;

}
