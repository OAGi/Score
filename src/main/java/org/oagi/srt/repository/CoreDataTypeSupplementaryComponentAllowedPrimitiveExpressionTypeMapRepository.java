package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository
        extends JpaRepository<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap, Integer> {

    @Query("select c from CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap c where c.cdtScAwdPri = ?1")
    public List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> findByCdtScAwdPri(int cdtScAwdPri);

    @Query("select c from CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap c where c.cdtScAwdPri = ?1 and c.xbtId = ?2")
    public CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap findOneByCdtScAwdPriAndXbtId(int cdtScAwdPri, int xbtId);
}
