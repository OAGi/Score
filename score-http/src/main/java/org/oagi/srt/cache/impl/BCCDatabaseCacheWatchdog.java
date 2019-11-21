package org.oagi.srt.cache.impl;

import org.oagi.srt.cache.DatabaseCacheWatchdog;
import org.oagi.srt.data.BCC;
import org.oagi.srt.repository.BCCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BCCDatabaseCacheWatchdog extends DatabaseCacheWatchdog<BCC> {

    public BCCDatabaseCacheWatchdog(@Autowired BCCRepository delegate) {
        super("bcc", BCC.class, delegate);
    }

}
