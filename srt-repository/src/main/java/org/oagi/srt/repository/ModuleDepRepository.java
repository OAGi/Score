package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.ModuleDep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ModuleDepRepository extends JpaRepository<ModuleDep, Long> {

    @Query("select md from ModuleDep md where md.dependingModule.moduleId = ?1")
    List<ModuleDep> findByDependingModuleId(long moduleId);
}
