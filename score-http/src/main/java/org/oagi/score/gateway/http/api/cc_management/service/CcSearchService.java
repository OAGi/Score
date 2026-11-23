package org.oagi.score.gateway.http.api.cc_management.service;

import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchNodes;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchPredicate;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchRequest;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchResponse;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.NotFoundException;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.TextSearchPredicates;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Answers the part of a core component tree search that is about text too heavy to travel with the search graph (the
 * definitions): which components of the release of the tree's root satisfy each predicate.
 */
@Service
@Transactional(readOnly = true)
public class CcSearchService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    public CcSearchResponse searchNodes(ScoreUser requester, CcSearchRequest request) {
        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null.");
        }
        TextSearchPredicates.validate(request == null ? null : request.predicates());

        var query = repositoryFactory.ccSearchQueryRepository(requester);
        ReleaseId releaseId = query.getReleaseOfRoot(request);
        if (releaseId == null) {
            throw new NotFoundException();
        }
        Map<String, CcSearchNodes> matches = new LinkedHashMap<>();
        for (CcSearchPredicate predicate : request.predicates()) {
            matches.put(predicate.id(), query.searchNodes(releaseId, predicate));
        }
        return new CcSearchResponse(matches);
    }
}
