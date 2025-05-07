package org.oagi.score.gateway.http.api.module_management.controller.payload;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;

public record AssignCCToModuleNode(AccManifestId accManifestId,
                                   AsccpManifestId asccpManifestId,
                                   BccpManifestId bccpManifestId,
                                   DtManifestId dtManifestId,
                                   CodeListManifestId codeListManifestId,
                                   AgencyIdListManifestId agencyIdListManifestId,
                                   XbtManifestId xbtManifestId) {
}
