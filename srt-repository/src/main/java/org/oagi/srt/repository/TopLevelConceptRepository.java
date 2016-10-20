package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.TopLevelConcept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TopLevelConceptRepository extends JpaRepository<TopLevelConcept, Long> {

    @Query("select a from TopLevelConcept a order by a.propertyTerm asc")
    public List<TopLevelConcept> findAllOrderByPropertyTermAsc();

    @Query("select a from TopLevelConcept a where a.propertyTerm = ?1")
    public List<TopLevelConcept> findByPropertyTermContaining(String propertyTerm);

}
