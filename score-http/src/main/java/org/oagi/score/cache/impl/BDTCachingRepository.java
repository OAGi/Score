package org.oagi.score.cache.impl;

import org.oagi.score.cache.CachingRepository;
import org.oagi.score.data.DT;
import org.oagi.score.repository.DTRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BDTCachingRepository extends CachingRepository<DT> {
    public BDTCachingRepository(@Autowired DTRepository delegate) {
        super("dt", DT.class, delegate);
    }

}
