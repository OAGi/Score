package org.oagi.score.cache.impl;

import org.oagi.score.cache.CachingRepository;
import org.oagi.score.data.BCCP;
import org.oagi.score.repository.BCCPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BCCPCachingRepository extends CachingRepository<BCCP> {

    public BCCPCachingRepository(@Autowired BCCPRepository delegate) {
        super("bccp", BCCP.class, delegate);
    }

}
