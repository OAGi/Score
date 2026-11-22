package org.oagi.score.gateway.http.api.cc_management.model.cc_search;

import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;

import java.util.List;

/** The components of a release that a predicate holds for, by kind. */
public record CcSearchNodes(List<AccManifestId> accs, List<AsccManifestId> asccs, List<AsccpManifestId> asccps,
                            List<BccManifestId> bccs, List<BccpManifestId> bccps, List<DtManifestId> dts,
                            List<DtScManifestId> dtScs) {

    public static CcSearchNodes none() {
        return new CcSearchNodes(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }
}
