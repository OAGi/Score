package org.oagi.score.gateway.http.api.cc_management.model.cc_search;

import java.util.Map;

/** For each predicate (by its id), the components of the release it holds for. */
public record CcSearchResponse(Map<String, CcSearchNodes> matches) {
}
