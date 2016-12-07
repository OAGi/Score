package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BusinessContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BusinessContextRepository extends JpaRepository<BusinessContext, Long> {

    @Query("select b from BusinessContext b where b.createdBy = ?1")
    public List<BusinessContext> findAllByCreatedBy(long createdBy);

    @Query("select b from BusinessContext b where b.name = ?1")
    public List<BusinessContext> findByName(String name);
}
