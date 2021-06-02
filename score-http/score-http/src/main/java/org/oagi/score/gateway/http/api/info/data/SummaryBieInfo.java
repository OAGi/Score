package org.oagi.score.gateway.http.api.info.data;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.data.BieList;
import org.oagi.score.repo.api.bie.model.BieState;

import java.util.List;
import java.util.Map;

@Data
public class SummaryBieInfo {

    private Map<BieState, Integer> numberOfTotalBieByStates;
    private Map<BieState, Integer> numberOfMyBieByStates;
    private Map<String, Map<BieState, Integer>> bieByUsersAndStates;
    private List<BieList> myRecentBIEs;

}
