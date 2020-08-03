package org.oagi.score.cache.impl;

import org.oagi.score.cache.DatabaseCacheWatchdog;
import org.oagi.score.data.DT;
import org.oagi.score.repository.DTRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DTDatabaseCacheWatchdog extends DatabaseCacheWatchdog<DT> {

    public DTDatabaseCacheWatchdog(@Autowired DTRepository delegate) {
        super("dt", DT.class, delegate);
    }

}
