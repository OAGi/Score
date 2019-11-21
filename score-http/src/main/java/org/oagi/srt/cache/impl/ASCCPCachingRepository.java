package org.oagi.srt.cache.impl;

import org.oagi.srt.cache.CachingRepository;
import org.oagi.srt.data.ASCCP;
import org.oagi.srt.repository.ASCCPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ASCCPCachingRepository extends CachingRepository<ASCCP> {

    public ASCCPCachingRepository(@Autowired ASCCPRepository delegate) {
        super("asccp", ASCCP.class, delegate);
    }

}
