package org.oagi.score.gateway.http.api.bie_management.service;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_search.BieSearchAttribute;
import org.oagi.score.gateway.http.api.bie_management.model.bie_search.BieSearchNodeKey;
import org.oagi.score.gateway.http.api.bie_management.model.bie_search.BieSearchPredicate;
import org.oagi.score.gateway.http.api.bie_management.model.bie_search.BieSearchRequest;
import org.oagi.score.gateway.http.api.bie_management.model.bie_search.BieSearchResponse;
import org.oagi.score.gateway.http.common.model.NotFoundException;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.TextSearchPredicates;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Answers the part of a BIE tree search that is about text too heavy to travel with the search graph (definition,
 * remark, example, fixed and default value, business term): which places of the BIE satisfy each predicate.
 */
@Service
@Transactional(readOnly = true)
public class BieSearchService {

    /** What the search bar can show of the values of an attribute. */
    static final int MAX_SUGGESTIONS = 30;

    @Autowired
    private RepositoryFactory repositoryFactory;

    public BieSearchResponse searchNodes(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId, BieSearchRequest request) {
        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null.");
        }
        TextSearchPredicates.validate(request == null ? null : request.predicates());

        // The same BIEs as the edit tree load: one that does not exist is not found.
        if (repositoryFactory.topLevelAsbiepQueryRepository(requester).getTopLevelAsbiepSummary(topLevelAsbiepId) == null) {
            throw new NotFoundException();
        }

        var query = repositoryFactory.bieSearchQueryRepository(requester);
        Map<String, List<BieSearchNodeKey>> matches = new LinkedHashMap<>();
        for (BieSearchPredicate predicate : request.predicates()) {
            matches.put(predicate.id(), query.searchNodes(topLevelAsbiepId, predicate));
        }
        return new BieSearchResponse(matches);
    }

    /** The values of a searched attribute that the BIE holds and that contain the text, for the search bar to offer. */
    public List<String> suggestValues(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId, BieSearchAttribute attribute, String text) {
        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null.");
        }
        String typed = text == null ? "" : text;
        if (typed.length() > TextSearchPredicates.MAX_VALUE_LENGTH) {
            throw new IllegalArgumentException("A text longer than " + TextSearchPredicates.MAX_VALUE_LENGTH + " characters.");
        }
        if (repositoryFactory.topLevelAsbiepQueryRepository(requester).getTopLevelAsbiepSummary(topLevelAsbiepId) == null) {
            throw new NotFoundException();
        }
        return repositoryFactory.bieSearchQueryRepository(requester).suggestValues(topLevelAsbiepId, attribute, typed, MAX_SUGGESTIONS);
    }
}
