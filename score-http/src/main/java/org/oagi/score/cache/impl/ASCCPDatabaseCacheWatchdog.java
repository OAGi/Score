package org.oagi.score.cache.impl;

import org.oagi.score.cache.DatabaseCacheWatchdog;
import org.oagi.score.data.ASCCP;
import org.oagi.score.repository.ASCCPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ASCCPDatabaseCacheWatchdog extends DatabaseCacheWatchdog<ASCCP> {

    public ASCCPDatabaseCacheWatchdog(@Autowired ASCCPRepository delegate) {
        super("asccp", ASCCP.class, delegate);
    }

}
