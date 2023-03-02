package org.oagi.score.repo.api.security;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.user.model.ScoreUser;

public class AccessControlException extends ScoreDataAccessException {

    public AccessControlException(String msg) {
        super(msg);
    }

    public AccessControlException(ScoreUser requester) {
        super(String.format("'%s' account does not have a right permission to access the function.", requester.getUsername()));
    }

}
