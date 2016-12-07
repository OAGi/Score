package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.CoreDataTypeAllowedPrimitive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CoreDataTypeAllowedPrimitiveRepository extends JpaRepository<CoreDataTypeAllowedPrimitive, Long> {

    @Query("select c from CoreDataTypeAllowedPrimitive c where c.cdtId = ?1")
    public List<CoreDataTypeAllowedPrimitive> findByCdtId(long cdtId);

    @Query("select c from CoreDataTypeAllowedPrimitive c where c.cdtId = ?1 and c.cdtPriId = ?2")
    public CoreDataTypeAllowedPrimitive findOneByCdtIdAndCdtPriId(long cdtId, long cdtPriId);
}
