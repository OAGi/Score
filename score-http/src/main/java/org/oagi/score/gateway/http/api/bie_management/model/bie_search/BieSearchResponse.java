package org.oagi.score.gateway.http.api.bie_management.model.bie_search;

import java.util.List;
import java.util.Map;

/** For each predicate (by its id), the places of the BIE it holds for. */
public record BieSearchResponse(Map<String, List<BieSearchNodeKey>> matches) {
}
