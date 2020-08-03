package org.oagi.score.gateway.http.api;

import org.springframework.dao.DataAccessException;

public class DataAccessForbiddenException extends DataAccessException {

    public DataAccessForbiddenException(String msg) {
        super(msg);
    }

}
