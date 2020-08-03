package org.oagi.score.cache.impl;

import org.oagi.score.cache.DatabaseCacheWatchdog;
import org.oagi.score.data.ASCC;
import org.oagi.score.repository.ASCCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ASCCDatabaseCacheWatchdog extends DatabaseCacheWatchdog<ASCC> {

    public ASCCDatabaseCacheWatchdog(@Autowired ASCCRepository delegate) {
        super("ascc", ASCC.class, delegate);
    }

}
