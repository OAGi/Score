package org.oagi.score.gateway.http.common.model;

import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;

public class AccessControlException extends ScoreDataAccessException {

    public AccessControlException(String msg) {
        super(msg);
    }

    public AccessControlException(ScoreUser requester) {
        super(String.format("'%s' account does not have a right permission to access the function.", requester.username()));
    }

}
