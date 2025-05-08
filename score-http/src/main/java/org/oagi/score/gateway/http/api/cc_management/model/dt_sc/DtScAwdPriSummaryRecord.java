package org.oagi.score.gateway.http.api.cc_management.model.dt_sc;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;

public record DtScAwdPriSummaryRecord(
        DtScAwdPriId dtScAwdPriId,
        ReleaseId releaseId,
        DtScId dtScId,
        String cdtPriName,
        XbtManifestId xbtManifestId,
        String xbtName,
        CodeListManifestId codeListManifestId,
        String codeListName,
        AgencyIdListManifestId agencyIdListManifestId,
        String agencyIdListName,
        boolean isDefault) {
}
