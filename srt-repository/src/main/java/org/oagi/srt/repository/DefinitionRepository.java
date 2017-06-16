package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.Definition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface DefinitionRepository extends JpaRepository<Definition, Long> {

    @Query("select d from Definition d where d.definitionId in (?1)")
    public List<Definition> findByDefinitionIdIn(Collection<Long> definitionIds);

    @Modifying
    @Query("delete from Definition d where d.definitionId in (?1)")
    public void deleteByDefinitionIdIn(Collection<Long> definitionIds);
}
