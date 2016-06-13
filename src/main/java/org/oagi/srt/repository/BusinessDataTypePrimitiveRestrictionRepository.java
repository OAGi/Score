package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BusinessDataTypePrimitiveRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BusinessDataTypePrimitiveRestrictionRepository extends JpaRepository<BusinessDataTypePrimitiveRestriction, Integer> {

    @Query("select b from BusinessDataTypePrimitiveRestriction b where b.bdtId = ?1 and b.isDefault = ?2")
    public BusinessDataTypePrimitiveRestriction findOneByBdtIdAndDefault(int bdtId, boolean isDefault);

    @Query("select b from BusinessDataTypePrimitiveRestriction b where b.bdtId = ?1 and b.cdtAwdPriXpsTypeMapId = ?2")
    public BusinessDataTypePrimitiveRestriction findOneByBdtIdAndCdtAwdPriXpsTypeMapId(int bdtId, int cdtAwdPriXpsTypeMapId);

    @Query("select b from BusinessDataTypePrimitiveRestriction b where b.codeListId = ?1 and b.cdtAwdPriXpsTypeMapId = ?2")
    public BusinessDataTypePrimitiveRestriction findOneByCodeListIdAndCdtAwdPriXpsTypeMapId(int codeListId, int cdtAwdPriXpsTypeMapId);

    @Query("select b from BusinessDataTypePrimitiveRestriction b where b.cdtAwdPriXpsTypeMapId = ?1")
    public List<BusinessDataTypePrimitiveRestriction> findByCdtAwdPriXpsTypeMapId(int cdtAwdPriXpsTypeMapId);

    @Query("select b from BusinessDataTypePrimitiveRestriction b where b.codeListId = ?1")
    public BusinessDataTypePrimitiveRestriction findOneByCodeListId(int codeListId);

    @Query("select b from BusinessDataTypePrimitiveRestriction b where b.bdtId = ?1")
    public List<BusinessDataTypePrimitiveRestriction> findByBdtId(int bdtId);
}
