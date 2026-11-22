package org.oagi.score.gateway.http.api.cc_management.service;

import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchNodes;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchPredicate;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchRequest;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchResponse;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.NotFoundException;
import org.oagi.score.gateway.http.common.model.ScoreUser;
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

    /** More predicates or longer text than a person types into a search bar is a mistake, or an attack. */
    static final int MAX_PREDICATES = 20;
    static final int MAX_VALUE_LENGTH = 1000;

    @Autowired
    private RepositoryFactory repositoryFactory;

    public CcSearchResponse searchNodes(ScoreUser requester, CcSearchRequest request) {
        if (request == null || request.predicates() == null || request.predicates().size() > MAX_PREDICATES) {
            throw new IllegalArgumentException("A search has at most " + MAX_PREDICATES + " server predicates.");
        }
        for (CcSearchPredicate predicate : request.predicates()) {
            if (predicate.id() == null || predicate.id().isEmpty() || predicate.operator() == null
                    || predicate.value() == null || predicate.value().length() > MAX_VALUE_LENGTH) {
                throw new IllegalArgumentException("A server predicate needs an id, an operator and a value of at most "
                        + MAX_VALUE_LENGTH + " characters.");
            }
        }

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
