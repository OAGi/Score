package org.oagi.score.cache.impl;

import org.oagi.score.cache.DatabaseCacheWatchdog;
import org.oagi.score.data.BdtPriRestri;
import org.oagi.score.repository.BdtPriRestriRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BdtPriRestriDatabaseCacheWatchdog extends DatabaseCacheWatchdog<BdtPriRestri> {

    public BdtPriRestriDatabaseCacheWatchdog(@Autowired BdtPriRestriRepository delegate) {
        super("bdt_pri_restri", BdtPriRestri.class, delegate);
    }

}
