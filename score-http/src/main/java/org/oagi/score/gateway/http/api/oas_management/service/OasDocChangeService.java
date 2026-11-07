package org.oagi.score.gateway.http.api.oas_management.service;

import org.oagi.score.gateway.http.api.oas_management.model.OasDocChangeResult;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocChangeSet;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocViolationException;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * What a client asks of an OpenAPI Document about what the user has changed: whether it can be saved ({@link #check}), and to
 * save it ({@link #save}). The client holds no rule: it only says what was changed, and shows the answer.
 *
 * The transaction is {@link OasDocChangeApplier}'s; this is outside of it, so that a rule that is broken, which rolls the
 * transaction back, is an answer here and not an error.
 */
@Service
public class OasDocChangeService {

    @Autowired
    private OasDocChangeApplier applier;

    /** Whether the change set can be saved, and the entries the document would have then. Nothing is saved. */
    public OasDocChangeResult check(ScoreUser requester, OasDocId oasDocId, OasDocChangeSet changeSet) {
        try {
            return applier.apply(requester, oasDocId, changeSet, true);
        } catch (OasDocViolationException e) {
            return OasDocChangeResult.invalid(e.violations());
        }
    }

    /**
     * Saves the change set, all of it or nothing. A rule that is broken is an {@link OasDocViolationException} for the
     * caller: it is checked again here, as what was checked may have changed since.
     */
    public OasDocChangeResult save(ScoreUser requester, OasDocId oasDocId, OasDocChangeSet changeSet) {
        return applier.apply(requester, oasDocId, changeSet, false);
    }
}
