package org.oagi.score.gateway.http.api.cc_management.controller.payload;

import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;

import java.util.List;
import java.util.Map;

/**
 * @param comments the optional per-component GitHub status comments to post verbatim
 *                 (issue #1533, sub-task 5), keyed by {@code "<type>:<manifestId>"} with the
 *                 lowercase type in {@code {acc, asccp, bccp, dt}} (e.g. {@code "acc:123"});
 *                 may be {@code null}/empty — a missing entry means "post nothing"
 */
public record CcUpdateStateRequest(
        CcState toState,
        List<AccManifestId> accManifestIdList,
        List<BccpManifestId> bccpManifestIdList,
        List<AsccpManifestId> asccpManifestIdList,
        List<DtManifestId> dtManifestIdList,
        Map<String, String> comments) {
}
