package org.oagi.srt.service;

import org.oagi.srt.repository.AggregateCoreComponentRepository;
import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class ACCService {

    @Autowired
    private AggregateCoreComponentRepository accRepository;

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

    public AggregateCoreComponent findByGuid(String guid) {
        if (StringUtils.isEmpty(guid)) {
            return null;
        }
        return accRepository.findOneByGuid(guid);
    }
}
