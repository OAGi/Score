package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.CoreDataTypeSupplementaryComponentAllowedPrimitive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository
        extends JpaRepository<CoreDataTypeSupplementaryComponentAllowedPrimitive, Integer> {

    @Query("select c from CoreDataTypeSupplementaryComponentAllowedPrimitive c where c.cdtScId = ?1")
    public List<CoreDataTypeSupplementaryComponentAllowedPrimitive> findByCdtScId(int cdtScId);

    @Query("select c from CoreDataTypeSupplementaryComponentAllowedPrimitive c where c.cdtPriId = ?1")
    public List<CoreDataTypeSupplementaryComponentAllowedPrimitive> findByCdtPriId(int cdtPriId);

    @Query("select c from CoreDataTypeSupplementaryComponentAllowedPrimitive c where c.cdtScId = ?1 and c.cdtPriId = ?2")
    public CoreDataTypeSupplementaryComponentAllowedPrimitive findOneByCdtScIdAndCdtPriId(int cdtScId, int cdtPriId);
}
