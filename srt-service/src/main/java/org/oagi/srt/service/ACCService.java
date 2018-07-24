package org.oagi.srt.service;

import org.oagi.srt.provider.CoreComponentProvider;
import org.oagi.srt.repository.AggregateCoreComponentRepository;
import org.oagi.srt.repository.AssociationCoreComponentRepository;
import org.oagi.srt.repository.BasicCoreComponentRepository;
import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.entity.BasicCoreComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ACCService {

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private BasicCoreComponentRepository bccRepository;

    @Autowired
    private AssociationCoreComponentRepository asccRepository;

    private CoreComponentProvider coreComponentProvider;

    @Autowired
    private CoreComponentService coreComponentService;

    @PostConstruct
    public void init() {
        coreComponentProvider = new CoreComponentProvider() {
            @Override
            public List<BasicCoreComponent> getBCCs(long accId) {
                return bccRepository.findByFromAccId(accId);
            }

            @Override
            public List<BasicCoreComponent> getBCCsWithoutAttributes(long accId) {
                return getBCCs(accId).stream()
                        .filter(e -> e.getSeqKey() != 0)
                        .collect(Collectors.toList());
            }

            @Override
            public List<AssociationCoreComponent> getASCCs(long accId) {
                return asccRepository.findByFromAccId(accId);
            }
        };
    }

    public Page<AggregateCoreComponent> findAll(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("'page' parameter must be positive");
        }
        if (size < 1) {
            throw new IllegalArgumentException("'size' parameter must be positive");
        }

        return accRepository.findAll(new PageRequest(page, size));
    }

    public AggregateCoreComponent findById(Long accId) {
        return accRepository.findOne(accId);
    }
}
