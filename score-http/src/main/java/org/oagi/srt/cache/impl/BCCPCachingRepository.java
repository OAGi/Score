package org.oagi.srt.cache.impl;

import org.oagi.srt.cache.CachingRepository;
import org.oagi.srt.data.BCCP;
import org.oagi.srt.repository.BCCPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BCCPCachingRepository extends CachingRepository<BCCP> {

    public BCCPCachingRepository(@Autowired BCCPRepository delegate) {
        super("bccp", BCCP.class, delegate);
    }

}
