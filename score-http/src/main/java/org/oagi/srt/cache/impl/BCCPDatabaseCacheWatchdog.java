package org.oagi.srt.cache.impl;

import org.oagi.srt.cache.DatabaseCacheWatchdog;
import org.oagi.srt.data.BCCP;
import org.oagi.srt.repository.BCCPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BCCPDatabaseCacheWatchdog extends DatabaseCacheWatchdog<BCCP> {

    public BCCPDatabaseCacheWatchdog(@Autowired BCCPRepository delegate) {
        super("bccp", BCCP.class, delegate);
    }

}
