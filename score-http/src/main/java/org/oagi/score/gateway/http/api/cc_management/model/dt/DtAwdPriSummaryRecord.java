package org.oagi.score.gateway.http.api.cc_management.model.dt;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;

public record DtAwdPriSummaryRecord(
        DtAwdPriId dtAwdPriId,
        ReleaseId releaseId,
        DtId dtId,
        String cdtPriName,
        XbtManifestId xbtManifestId,
        String xbtName,
        CodeListManifestId codeListManifestId,
        String codeListName,
        AgencyIdListManifestId agencyIdListManifestId,
        String agencyIdListName,
        boolean isDefault) {
}
