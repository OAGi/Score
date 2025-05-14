package org.oagi.score.gateway.http.api.module_management.controller.payload;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.module_management.model.AssignNodeRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;

import java.util.Map;

public record ModuleAssignableComponentsRecord(
        Map<AccManifestId, AssignNodeRecord> assignableAccManifestMap,
        Map<AsccpManifestId, AssignNodeRecord> assignableAsccpManifestMap,
        Map<BccpManifestId, AssignNodeRecord> assignableBccpManifestMap,
        Map<DtManifestId, AssignNodeRecord> assignableDtManifestMap,
        Map<CodeListManifestId, AssignNodeRecord> assignableCodeListManifestMap,
        Map<AgencyIdListManifestId, AssignNodeRecord> assignableAgencyIdListManifestMap,
        Map<XbtManifestId, AssignNodeRecord> assignableXbtManifestMap) {

}
