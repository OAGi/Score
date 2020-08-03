package org.oagi.score.cache.impl;

import org.oagi.score.cache.DatabaseCacheWatchdog;
import org.oagi.score.data.BCCP;
import org.oagi.score.repository.BCCPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BCCPDatabaseCacheWatchdog extends DatabaseCacheWatchdog<BCCP> {

    public BCCPDatabaseCacheWatchdog(@Autowired BCCPRepository delegate) {
        super("bccp", BCCP.class, delegate);
    }

}
