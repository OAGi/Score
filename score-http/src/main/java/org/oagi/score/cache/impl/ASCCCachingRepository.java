package org.oagi.score.cache.impl;

import org.oagi.score.cache.CachingRepository;
import org.oagi.score.data.ASCC;
import org.oagi.score.repository.ASCCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ASCCCachingRepository extends CachingRepository<ASCC> {

    public ASCCCachingRepository(@Autowired ASCCRepository delegate) {
        super("ascc", ASCC.class, delegate);
    }

}
