package org.oagi.srt.cache.impl;

import org.oagi.srt.cache.DatabaseCacheWatchdog;
import org.oagi.srt.data.BdtScPriRestri;
import org.oagi.srt.repository.BdtScPriRestriRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BdtScPriRestriDatabaseCacheWatchdog extends DatabaseCacheWatchdog<BdtScPriRestri> {

    public BdtScPriRestriDatabaseCacheWatchdog(@Autowired BdtScPriRestriRepository delegate) {
        super("bdt_sc_pri_restri", BdtScPriRestri.class, delegate);
    }

}
