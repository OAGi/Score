package org.oagi.score.gateway.http.api.cc_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;

import java.util.Set;

/**
 * The components a graph rooted at one component reaches: through the base of an ACC, the ASCCs and BCCs of an ACC to
 * their ASCCPs and BCCPs, the ACC an ASCCP is of, and the DT of a BCCP.
 */
public record GraphManifestIds(Set<AccManifestId> accs,
                               Set<AsccpManifestId> asccps,
                               Set<BccpManifestId> bccps,
                               Set<DtManifestId> dts) {
}
