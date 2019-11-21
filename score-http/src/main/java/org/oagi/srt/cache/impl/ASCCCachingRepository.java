package org.oagi.srt.cache.impl;

import org.oagi.srt.cache.CachingRepository;
import org.oagi.srt.data.ASCC;
import org.oagi.srt.repository.ASCCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ASCCCachingRepository extends CachingRepository<ASCC> {

    public ASCCCachingRepository(@Autowired ASCCRepository delegate) {
        super("ascc", ASCC.class, delegate);
    }

}
