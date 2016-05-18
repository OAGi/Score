package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BusinessDataTypePrimitiveRestriction;

import java.util.List;

public interface BusinessDataTypePrimitiveRestrictionRepository {

    public BusinessDataTypePrimitiveRestriction findOneByBdtIdAndDefault(int bdtId, boolean isDefault);

    public BusinessDataTypePrimitiveRestriction findOneByBdtPriRestriId(int bdtPriRestriId);

    public List<BusinessDataTypePrimitiveRestriction> findByBdtId(int bdtId);

    public void save(BusinessDataTypePrimitiveRestriction businessDataTypePrimitiveRestriction);
}
