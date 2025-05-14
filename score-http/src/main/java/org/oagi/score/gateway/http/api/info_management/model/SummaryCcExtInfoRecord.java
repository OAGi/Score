package org.oagi.score.gateway.http.api.info_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.CcState;

import java.util.List;
import java.util.Map;

public record SummaryCcExtInfoRecord(
        Map<CcState, Integer> numberOfTotalCcExtByStates,
        Map<CcState, Integer> numberOfMyCcExtByStates,
        Map<String, Map<CcState, Integer>> ccExtByUsersAndStates,
        List<SummaryCcExt> myExtensionsUnusedInBIEs) {
}
