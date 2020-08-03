package org.oagi.score.cache.impl;

import org.oagi.score.cache.CachingRepository;
import org.oagi.score.data.BCC;
import org.oagi.score.repository.BCCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BCCCachingRepository extends CachingRepository<BCC> {

    public BCCCachingRepository(@Autowired BCCRepository delegate) {
        super("bcc", BCC.class, delegate);
    }

}
