package org.oagi.score.cache.impl;

import org.oagi.score.cache.DatabaseCacheWatchdog;
import org.oagi.score.data.DTSC;
import org.oagi.score.repository.DTSCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DTSCDatabaseCacheWatchdog extends DatabaseCacheWatchdog<DTSC> {

    public DTSCDatabaseCacheWatchdog(@Autowired DTSCRepository delegate) {
        super("dt_sc", DTSC.class, delegate);
    }

}
