package org.oagi.srt.gateway.http.api;

import org.springframework.dao.DataAccessException;

public class DataAccessForbiddenException extends DataAccessException {

    public DataAccessForbiddenException(String msg) {
        super(msg);
    }

}
