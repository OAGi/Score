package org.oagi.score.cache.impl;

import org.oagi.score.cache.DatabaseCacheWatchdog;
import org.oagi.score.data.BCC;
import org.oagi.score.repository.BCCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BCCDatabaseCacheWatchdog extends DatabaseCacheWatchdog<BCC> {

    public BCCDatabaseCacheWatchdog(@Autowired BCCRepository delegate) {
        super("bcc", BCC.class, delegate);
    }

}
