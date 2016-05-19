package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.CoreDataTypeAllowedPrimitiveExpressionTypeMap;

import java.util.List;

public interface CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository {

    public List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> findAll();

    public List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> findByCdtAwdPriId(int cdtAwdPriId);

    public CoreDataTypeAllowedPrimitiveExpressionTypeMap findOneByCdtAwdPriXpsTypeMapId(int cdtAwdPriXpsTypeMapId);
}
