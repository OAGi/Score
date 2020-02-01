package org.oagi.srt.gateway.http.api.cc_management.repository;

import org.oagi.srt.cache.impl.*;
import org.oagi.srt.data.*;
import org.oagi.srt.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CoreComponentRepository {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ACCCachingRepository accRepository;
    @Autowired
    private ASCCCachingRepository asccRepository;
    @Autowired
    private BCCCachingRepository bccRepository;
    @Autowired
    private ASCCPCachingRepository asccpRepository;
    @Autowired
    private BCCPCachingRepository bccpRepository;
    @Autowired
    private BDTCachingRepository bdtRepository;

    public List<ACC> getAccList() {
        return accRepository.findAll();
    }

    public List<ASCC> getAsccList() {
        return asccRepository.findAll();
    }

    public List<BCC> getBccList() {
        return bccRepository.findAll();
    }

    public List<ASCCP> getAsccpList() {
        return asccpRepository.findAll();
    }

    public List<BCCP> getBccpList() {
        return bccpRepository.findAll();
    }

    public List<DT> getBdtList() {
        return bdtRepository.findAll();
    }
}
