package org.oagi.srt.cache.impl;

import org.oagi.srt.cache.DatabaseCacheWatchdog;
import org.oagi.srt.data.ASCC;
import org.oagi.srt.repository.ASCCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ASCCDatabaseCacheWatchdog extends DatabaseCacheWatchdog<ASCC> {

    public ASCCDatabaseCacheWatchdog(@Autowired ASCCRepository delegate) {
        super("ascc", ASCC.class, delegate);
    }

}
