package org.oagi.srt.service;

import org.oagi.srt.repository.BasicCoreComponentPropertyRepository;
import org.oagi.srt.repository.entity.BasicCoreComponent;
import org.oagi.srt.repository.entity.BasicCoreComponentProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class BCCPService {

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;

    public Page<BasicCoreComponentProperty> findAll(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("'page' parameter must be positive");
        }
        if (size < 1) {
            throw new IllegalArgumentException("'size' parameter must be positive");
        }

        return bccpRepository.findAll(new PageRequest(page, size));
    }

    public BasicCoreComponentProperty findByGuid(String guid) {
        if (StringUtils.isEmpty(guid)) {
            return null;
        }
        return bccpRepository.findOneByGuid(guid);
    }

    public BasicCoreComponentProperty findByBCC(BasicCoreComponent bcc) {
        return bccpRepository.findById(bcc.getToBccpId()).orElse(null);
    }

}
