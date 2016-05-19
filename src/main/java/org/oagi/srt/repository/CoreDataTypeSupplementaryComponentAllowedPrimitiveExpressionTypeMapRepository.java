package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap;

import java.util.List;

public interface CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository {

    public List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> findAll();

    public List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> findByCdtScAwdPri(int cdtScAwdPri);

    public CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap findOneByCdtScAwdPriXpsTypeMapId(int cdtScAwdPriXpsTypeMapId);

    public CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap findOneByCdtScAwdPriAndXbtId(int cdtScAwdPri, int xbtId);

    public void save(CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap);
}
