package org.oagi.score.gateway.http.api.info.data;

import lombok.Data;
import org.oagi.score.service.common.data.CcState;

import java.util.List;
import java.util.Map;

@Data
public class SummaryCcExtInfo {

    private Map<CcState, Integer> numberOfTotalCcExtByStates;
    private Map<CcState, Integer> numberOfMyCcExtByStates;
    private Map<String, Map<CcState, Integer>> ccExtByUsersAndStates;
    private List<SummaryCcExt> myExtensionsUnusedInBIEs;

}
