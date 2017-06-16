package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.Definition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DefinitionRepository extends JpaRepository<Definition, Long> {
}
