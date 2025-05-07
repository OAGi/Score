package org.oagi.score.gateway.http.api.info_management.model;

import org.oagi.score.gateway.http.api.bie_management.model.BieListEntryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;

import java.util.List;
import java.util.Map;

public record SummaryBieInfoRecord(
        Map<BieState, Integer> numberOfTotalBieByStates,
        Map<BieState, Integer> numberOfMyBieByStates,
        Map<String, Map<BieState, Integer>> bieByUsersAndStates,
        List<BieListEntryRecord> myRecentBIEs) {
}
