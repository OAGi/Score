package org.oagi.score.cache.impl;

import org.oagi.score.cache.DatabaseCacheWatchdog;
import org.oagi.score.data.ACC;
import org.oagi.score.repository.ACCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ACCDatabaseCacheWatchdog extends DatabaseCacheWatchdog<ACC> {

    public ACCDatabaseCacheWatchdog(@Autowired ACCRepository delegate) {
        super("acc", ACC.class, delegate);
    }

}
