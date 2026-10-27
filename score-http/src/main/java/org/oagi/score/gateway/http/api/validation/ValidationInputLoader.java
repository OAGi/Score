package org.oagi.score.gateway.http.api.validation;

import org.oagi.score.gateway.http.common.model.ScoreUser;

/** Reads what the rules of a subject need, once per validation. All reading of the database is here and only reading. */
public interface ValidationInputLoader<I> {

    I load(ScoreUser requester, ValidationSubject subject);
}
