package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.CoreDataTypeSupplementaryComponentAllowedPrimitive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository
        extends JpaRepository<CoreDataTypeSupplementaryComponentAllowedPrimitive, Long> {

    @Query("select c from CoreDataTypeSupplementaryComponentAllowedPrimitive c where c.cdtScId = ?1")
    public List<CoreDataTypeSupplementaryComponentAllowedPrimitive> findByCdtScId(long cdtScId);

    @Query("select c from CoreDataTypeSupplementaryComponentAllowedPrimitive c where c.cdtScId in ?1")
    public List<CoreDataTypeSupplementaryComponentAllowedPrimitive> findByCdtScIdIn(Collection<Long> cdtScIds);

    @Query("select c from CoreDataTypeSupplementaryComponentAllowedPrimitive c where c.cdtPriId = ?1")
    public List<CoreDataTypeSupplementaryComponentAllowedPrimitive> findByCdtPriId(long cdtPriId);

    @Query("select c from CoreDataTypeSupplementaryComponentAllowedPrimitive c where c.cdtScId = ?1 and c.cdtPriId = ?2")
    public CoreDataTypeSupplementaryComponentAllowedPrimitive findOneByCdtScIdAndCdtPriId(long cdtScId, long cdtPriId);
}
