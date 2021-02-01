package org.oagi.score.gateway.http.api;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.AuthenticatedPrincipal;

public class DataAccessForbiddenException extends DataAccessException {

    public DataAccessForbiddenException(String msg) {
        super(msg);
    }

    public DataAccessForbiddenException(AuthenticatedPrincipal user) {
        super("'" + user.getName() + "' doesn't have an access privilege.");
    }

}
