package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.CoreDataTypeAllowedPrimitiveExpressionTypeMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository
        extends JpaRepository<CoreDataTypeAllowedPrimitiveExpressionTypeMap, Integer> {

    @Query("select c from CoreDataTypeAllowedPrimitiveExpressionTypeMap c where c.cdtAwdPriId = ?1")
    public List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> findByCdtAwdPriId(int cdtAwdPriId);

    @Query("select c from CoreDataTypeAllowedPrimitiveExpressionTypeMap c where c.cdtAwdPriId in ?1")
    public List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> findByCdtAwdPriIdIn(Collection<Integer> cdtAwdPriIds);

    @Query("select c from CoreDataTypeAllowedPrimitiveExpressionTypeMap c where c.cdtAwdPriXpsTypeMapId in ?1")
    public List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> findByCdtAwdPriXpsTypeMapIdIn(Collection<Integer> cdtAwdPriXpsTypeMapIds);
}
