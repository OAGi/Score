package org.oagi.score.cache.impl;

import org.oagi.score.cache.DatabaseCacheWatchdog;
import org.oagi.score.data.BdtScPriRestri;
import org.oagi.score.repository.BdtScPriRestriRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BdtScPriRestriDatabaseCacheWatchdog extends DatabaseCacheWatchdog<BdtScPriRestri> {

    public BdtScPriRestriDatabaseCacheWatchdog(@Autowired BdtScPriRestriRepository delegate) {
        super("bdt_sc_pri_restri", BdtScPriRestri.class, delegate);
    }

}
