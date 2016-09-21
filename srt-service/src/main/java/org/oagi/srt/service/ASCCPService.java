package org.oagi.srt.service;

import org.oagi.srt.repository.AssociationCoreComponentPropertyRepository;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

import static org.oagi.srt.common.SRTConstants.OAGI_GUID_PREFIX;

@Service
@Transactional(readOnly = true)
public class ASCCPService {

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    public Page<AssociationCoreComponentProperty> findAll(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("'page' parameter must be positive");
        }
        if (size < 1) {
            throw new IllegalArgumentException("'size' parameter must be positive");
        }

        return asccpRepository.findAll(new PageRequest(page, size));
    }

    public AssociationCoreComponentProperty findByGuid(String guid) {
        if (StringUtils.isEmpty(guid)) {
            return null;
        }
        return asccpRepository.findOneByGuid(guid);
    }

}
