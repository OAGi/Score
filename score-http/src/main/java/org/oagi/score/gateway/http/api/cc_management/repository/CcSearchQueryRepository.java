package org.oagi.score.gateway.http.api.cc_management.repository;

import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchNodes;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchPredicate;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchRequest;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

public interface CcSearchQueryRepository {

    /** The release of the component a tree is rooted at, or null when there is no such component. */
    ReleaseId getReleaseOfRoot(CcSearchRequest request);

    /**
     * Finds the components of one release whose text satisfies the predicate. A component whose text is absent (NULL)
     * satisfies no predicate, not even "not equals".
     */
    CcSearchNodes searchNodes(ReleaseId releaseId, CcSearchPredicate predicate);

}
