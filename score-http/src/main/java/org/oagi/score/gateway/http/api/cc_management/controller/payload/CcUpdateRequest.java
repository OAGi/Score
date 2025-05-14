package org.oagi.score.gateway.http.api.cc_management.controller.payload;

import org.oagi.score.gateway.http.api.cc_management.controller.payload.acc.AccUpdateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.ascc.AsccUpdateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.asccp.AsccpUpdateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.bcc.BccUpdateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.bccp.BccpUpdateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.dt.DtUpdateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.dt_sc.DtScUpdateRequest;

import java.util.List;

public record CcUpdateRequest(
        List<AccUpdateRequest> accUpdateRequestList,
        List<AsccUpdateRequest> asccUpdateRequestList,
        List<AsccpUpdateRequest> asccpUpdateRequestList,
        List<BccUpdateRequest> bccUpdateRequestList,
        List<BccpUpdateRequest> bccpUpdateRequestList,
        List<DtUpdateRequest> dtUpdateRequestList,
        List<DtScUpdateRequest> dtScUpdateRequestList) {
}
