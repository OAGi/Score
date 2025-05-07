package org.oagi.score.gateway.http.api.info_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.CcListEntryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;

import java.util.List;
import java.util.Map;

public record SummaryCcInfoRecord(
        Map<CcState, Integer> numberOfTotalCcByStates,
        Map<CcState, Integer> numberOfMyCcByStates,
        Map<String, Map<CcState, Integer>> ccByUsersAndStates,
        List<CcListEntryRecord> myRecentCCs) {
}
