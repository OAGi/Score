package org.oagi.srt.cache.impl;

import org.oagi.srt.cache.CachingRepository;
import org.oagi.srt.data.ACC;
import org.oagi.srt.repository.ACCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ACCCachingRepository extends CachingRepository<ACC> {

    public ACCCachingRepository(@Autowired ACCRepository delegate) {
        super("acc", ACC.class, delegate);
    }

}
