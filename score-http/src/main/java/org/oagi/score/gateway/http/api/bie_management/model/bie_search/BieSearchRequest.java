package org.oagi.score.gateway.http.api.bie_management.model.bie_search;

import java.util.List;

/** The server-side predicates of one search; they are all answered by one request. */
public record BieSearchRequest(List<BieSearchPredicate> predicates) {
}
