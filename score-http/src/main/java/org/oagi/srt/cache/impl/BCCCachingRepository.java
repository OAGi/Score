package org.oagi.srt.cache.impl;

import org.oagi.srt.cache.CachingRepository;
import org.oagi.srt.data.BCC;
import org.oagi.srt.repository.BCCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BCCCachingRepository extends CachingRepository<BCC> {

    public BCCCachingRepository(@Autowired BCCRepository delegate) {
        super("bcc", BCC.class, delegate);
    }

}
