package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelperTest {

    @Test
    void agencyListTypeNameSupportsAnUnsetAgencyIdListValue() {
        AgencyIdListSummaryRecord agencyIdList = new AgencyIdListSummaryRecord(
                null, null, null, null, null,
                "Agency List", "AgencyList", "1", null, null,
                null, null, false, CcState.WIP, null,
                null, null, List.of());

        assertEquals("il_1_agencyListContentType_AgencyList",
                Helper.getAgencyListTypeName(agencyIdList, null));
    }
}
