package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ModuleRepository extends JpaRepository<Module, Integer> {

    @Query("select m from Module m where m.module = ?1")
    public Module findByModule(String module);

    @Query("select m from Module m where m.module like %?1%")
    public Module findByModuleContaining(String module);

    @Query("select case when count(m) > 0 then true else false end from Module m where m.module = ?1")
    public boolean existsByModule(String module);
}
