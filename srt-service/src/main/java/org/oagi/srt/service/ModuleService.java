package org.oagi.srt.service;

import org.oagi.srt.repository.ModuleDepRepository;
import org.oagi.srt.repository.ModuleRepository;
import org.oagi.srt.repository.entity.Module;
import org.oagi.srt.repository.entity.ModuleDep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class ModuleService {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ModuleDepRepository moduleDepRepository;

    public List<Module> findAll() {
        return moduleRepository.findAll();
    }

    public Module findById(long moduleId) {
        return moduleRepository.findById(moduleId).orElse(null);
    }

    public List<Module> findAll(Sort.Direction direction, String property) {
        return moduleRepository.findAll(new Sort(new Sort.Order(direction, property)));
    }

    public Module update(Module module) {
        return moduleRepository.saveAndFlush(module);
    }

    public void delete(Module module) {
        moduleRepository.delete(module);
    }

    public boolean isExistsModule(String moduleFilePath) {
        return Objects.nonNull(moduleRepository.findByModule(moduleFilePath));
    }

    public List<ModuleDep> findDependedModules(Module module) {
        return moduleDepRepository.findByDependingModuleId(module.getModuleId());
    }

    public Module findByModule(String module) {
        return moduleRepository.findByModule(module);
    }

    public Module update(Module module, List<ModuleDep> dependedModules) {
        Module res = update(module);
        moduleDepRepository.saveAll(dependedModules);

        return res;
    }

    public void delete(Module module, List<ModuleDep> dependedModules) {
        moduleDepRepository.deleteAll(dependedModules);
        moduleRepository.delete(module);
    }

    public void delete(ModuleDep moduleDep) {
        moduleDepRepository.delete(moduleDep);
    }
}
