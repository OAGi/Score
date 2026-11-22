package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_search.BieSearchNodeKey;
import org.oagi.score.gateway.http.api.bie_management.model.bie_search.BieSearchPredicate;

import java.util.List;

public interface BieSearchQueryRepository {

    /**
     * Finds the places of one BIE whose text satisfies the predicate. A place whose value is absent (NULL) satisfies
     * no predicate, not even "not equals".
     */
    List<BieSearchNodeKey> searchNodes(TopLevelAsbiepId topLevelAsbiepId, BieSearchPredicate predicate);

    /**
     * The values of an attribute that the BIE holds and that contain the text (all of them when it is empty), at most
     * {@code limit}, each one a value that {@link #searchNodes} can find.
     */
    List<String> suggestValues(TopLevelAsbiepId topLevelAsbiepId, String attributeName, String text, int limit);

}
