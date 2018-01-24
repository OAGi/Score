package org.oagi.srt.service;

import org.oagi.srt.repository.ModuleRepository;
import org.oagi.srt.repository.entity.Module;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ModuleService {

    @Autowired
    private ModuleRepository moduleRepository;

    public List<Module> findAll() {
        return moduleRepository.findAll();
    }

    public Module findById(long moduleId) {
        return moduleRepository.findOne(moduleId);
    }

    public List<Module> findAll(Sort.Direction direction, String property) {
        return moduleRepository.findAll(new Sort(new Sort.Order(direction, property)));
    }

}
