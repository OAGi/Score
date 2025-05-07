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

public record ModuleAssignedComponentsRecord(
        Map<AccManifestId, AssignNodeRecord> assignedAccManifestMap,
        Map<AsccpManifestId, AssignNodeRecord> assignedAsccpManifestMap,
        Map<BccpManifestId, AssignNodeRecord> assignedBccpManifestMap,
        Map<DtManifestId, AssignNodeRecord> assignedDtManifestMap,
        Map<CodeListManifestId, AssignNodeRecord> assignedCodeListManifestMap,
        Map<AgencyIdListManifestId, AssignNodeRecord> assignedAgencyIdListManifestMap,
        Map<XbtManifestId, AssignNodeRecord> assignedXbtManifestMap) {

}
