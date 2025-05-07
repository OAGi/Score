package org.oagi.score.gateway.http.api.bie_management.model;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;

public record PrimitiveRestriction(XbtManifestId xbtManifestId,
                                   CodeListManifestId codeListManifestId,
                                   AgencyIdListManifestId agencyIdListManifestId) {

    public static PrimitiveRestriction fromDtAwdPri(DtAwdPriSummaryRecord dtAwdPri) {
        return new PrimitiveRestriction(
                dtAwdPri.xbtManifestId(),
                dtAwdPri.codeListManifestId(),
                dtAwdPri.agencyIdListManifestId());
    }

    public static PrimitiveRestriction fromDtScAwdPri(DtScAwdPriSummaryRecord dtScAwdPri) {
        return new PrimitiveRestriction(
                dtScAwdPri.xbtManifestId(),
                dtScAwdPri.codeListManifestId(),
                dtScAwdPri.agencyIdListManifestId());
    }

}
