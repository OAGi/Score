package org.oagi.score.gateway.http.api.info.data;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.data.CcList;
import org.oagi.score.service.common.data.CcState;

import java.util.List;
import java.util.Map;

@Data
public class SummaryCcInfo {

    private Map<CcState, Integer> numberOfTotalCcByStates;
    private Map<CcState, Integer> numberOfMyCcByStates;
    private Map<String, Map<CcState, Integer>> ccByUsersAndStates;
    private List<CcList> myRecentCCs;

}
