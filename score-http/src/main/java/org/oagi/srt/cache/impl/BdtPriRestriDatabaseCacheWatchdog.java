package org.oagi.srt.cache.impl;

import org.oagi.srt.cache.DatabaseCacheWatchdog;
import org.oagi.srt.data.BdtPriRestri;
import org.oagi.srt.repository.BdtPriRestriRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BdtPriRestriDatabaseCacheWatchdog extends DatabaseCacheWatchdog<BdtPriRestri> {

    public BdtPriRestriDatabaseCacheWatchdog(@Autowired BdtPriRestriRepository delegate) {
        super("bdt_pri_restri", BdtPriRestri.class, delegate);
    }

}
