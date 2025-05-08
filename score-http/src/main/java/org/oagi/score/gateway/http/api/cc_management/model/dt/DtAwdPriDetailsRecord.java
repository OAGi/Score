package org.oagi.score.gateway.http.api.cc_management.model.dt;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;

public record DtAwdPriDetailsRecord(
        DtAwdPriId dtAwdPriId,
        ReleaseSummaryRecord release,
        DtId dtId,
        String cdtPriName,
        XbtSummaryRecord xbt,
        CodeListSummaryRecord codeList,
        AgencyIdListSummaryRecord agencyIdList,
        boolean isDefault,
        boolean inherited) {
}
