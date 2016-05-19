package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BusinessDataTypePrimitiveRestriction;

import java.util.List;

public interface BusinessDataTypePrimitiveRestrictionRepository {

    public BusinessDataTypePrimitiveRestriction findOneByBdtIdAndDefault(int bdtId, boolean isDefault);

    public BusinessDataTypePrimitiveRestriction findOneByBdtPriRestriId(int bdtPriRestriId);

    public BusinessDataTypePrimitiveRestriction findOneByBdtIdAndCdtAwdPriXpsTypeMapId(int bdtId, int cdtAwdPriXpsTypeMapId);

    public BusinessDataTypePrimitiveRestriction findOneByCodeListIdAndCdtAwdPriXpsTypeMapId(int codeListId, int cdtAwdPriXpsTypeMapId);

    public BusinessDataTypePrimitiveRestriction findOneByCdtAwdPriXpsTypeMapId(int cdtAwdPriXpsTypeMapId);

    public BusinessDataTypePrimitiveRestriction findOneByCodeListId(int codeListId);

    public List<BusinessDataTypePrimitiveRestriction> findByBdtId(int bdtId);

    public void save(BusinessDataTypePrimitiveRestriction businessDataTypePrimitiveRestriction);
}
