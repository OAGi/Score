package org.oagi.score.cache.impl;

import org.oagi.score.cache.CachingRepository;
import org.oagi.score.data.ASCCP;
import org.oagi.score.repository.ASCCPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ASCCPCachingRepository extends CachingRepository<ASCCP> {

    public ASCCPCachingRepository(@Autowired ASCCPRepository delegate) {
        super("asccp", ASCCP.class, delegate);
    }

}
