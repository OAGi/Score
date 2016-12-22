package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BusinessDataTypePrimitiveRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface BusinessDataTypePrimitiveRestrictionRepository extends JpaRepository<BusinessDataTypePrimitiveRestriction, Long> {

    @Query("select b from BusinessDataTypePrimitiveRestriction b where b.bdtId = ?1 and b.isDefault = ?2")
    public BusinessDataTypePrimitiveRestriction findOneByBdtIdAndDefault(long bdtId, boolean isDefault);

    @Query("select b from BusinessDataTypePrimitiveRestriction b where b.bdtId = ?1 and b.codeListId > 0")
    public BusinessDataTypePrimitiveRestriction findOneByBdtIdAndCodeListIdIsNotZero(long bdtId);

    @Query("select b from BusinessDataTypePrimitiveRestriction b where b.bdtId = ?1 and b.cdtAwdPriXpsTypeMapId = ?2")
    public BusinessDataTypePrimitiveRestriction findOneByBdtIdAndCdtAwdPriXpsTypeMapId(long bdtId, long cdtAwdPriXpsTypeMapId);

    @Query("select b from BusinessDataTypePrimitiveRestriction b where b.codeListId = ?1 and b.cdtAwdPriXpsTypeMapId = ?2")
    public BusinessDataTypePrimitiveRestriction findOneByCodeListIdAndCdtAwdPriXpsTypeMapId(long codeListId, long cdtAwdPriXpsTypeMapId);

    @Query("select b from BusinessDataTypePrimitiveRestriction b where b.cdtAwdPriXpsTypeMapId = ?1")
    public List<BusinessDataTypePrimitiveRestriction> findByCdtAwdPriXpsTypeMapId(long cdtAwdPriXpsTypeMapId);

    @Query("select b from BusinessDataTypePrimitiveRestriction b where b.cdtAwdPriXpsTypeMapId in ?1")
    public List<BusinessDataTypePrimitiveRestriction> findByCdtAwdPriXpsTypeMapIdIn(Collection<Long> cdtAwdPriXpsTypeMapIdList);

    @Query("select b from BusinessDataTypePrimitiveRestriction b where b.codeListId = ?1")
    public BusinessDataTypePrimitiveRestriction findOneByCodeListId(long codeListId);

    @Query("select b from BusinessDataTypePrimitiveRestriction b where b.bdtId = ?1")
    public List<BusinessDataTypePrimitiveRestriction> findByBdtId(long bdtId);
}
