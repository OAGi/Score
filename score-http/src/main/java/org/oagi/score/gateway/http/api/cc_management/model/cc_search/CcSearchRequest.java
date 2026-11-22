package org.oagi.score.gateway.http.api.cc_management.model.cc_search;

import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;

import java.util.List;

/**
 * The server-side predicates of one search, all answered by one request, and the component the tree is rooted at (one of
 * the four manifest ids is set), which tells the release to search in.
 */
public record CcSearchRequest(AccManifestId accManifestId, AsccpManifestId asccpManifestId, BccpManifestId bccpManifestId,
                              DtManifestId dtManifestId, List<CcSearchPredicate> predicates) {
}
