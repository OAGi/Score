package org.oagi.score.gateway.http.api.cc_management.controller.payload;

import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;

import java.util.List;

public record CcUpdateStateRequest(
        CcState toState,
        List<AccManifestId> accManifestIdList,
        List<BccpManifestId> bccpManifestIdList,
        List<AsccpManifestId> asccpManifestIdList,
        List<DtManifestId> dtManifestIdList) {
}
