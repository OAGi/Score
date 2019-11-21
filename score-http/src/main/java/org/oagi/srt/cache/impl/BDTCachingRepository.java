package org.oagi.srt.cache.impl;

import org.oagi.srt.cache.CachingRepository;
import org.oagi.srt.data.DT;
import org.oagi.srt.repository.DTRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BDTCachingRepository extends CachingRepository<DT> {
    public BDTCachingRepository(@Autowired DTRepository delegate) {
        super("dt", DT.class, delegate);
    }

}
