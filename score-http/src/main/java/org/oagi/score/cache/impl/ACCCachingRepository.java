package org.oagi.score.cache.impl;

import org.oagi.score.cache.CachingRepository;
import org.oagi.score.data.ACC;
import org.oagi.score.repository.ACCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ACCCachingRepository extends CachingRepository<ACC> {

    public ACCCachingRepository(@Autowired ACCRepository delegate) {
        super("acc", ACC.class, delegate);
    }

}
