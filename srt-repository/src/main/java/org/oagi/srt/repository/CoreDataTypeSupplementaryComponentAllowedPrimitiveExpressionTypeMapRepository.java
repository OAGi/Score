package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository
        extends JpaRepository<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap, Integer> {

    @Query("select c from CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap c where c.cdtScAwdPriId = ?1")
    public List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> findByCdtScAwdPriId(int cdtScAwdPriId);

    @Query("select c from CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap c where c.cdtScAwdPriId in ?1")
    public List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> findByCdtScAwdPriIdIn(Collection<Integer> cdtScAwdPriIds);

    @Query("select c from CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap c where c.cdtScAwdPriId = ?1 and c.xbtId = ?2")
    public CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap findOneByCdtScAwdPriIdAndXbtId(int cdtScAwdPriId, int xbtId);
}
