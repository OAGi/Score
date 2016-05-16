package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BusinessDataTypePrimitiveRestriction;

public interface BusinessDataTypePrimitiveRestrictionRepository {

    public BusinessDataTypePrimitiveRestriction findOneByBdtIdAndDefault(int bdtId, boolean isDefault);

    public BusinessDataTypePrimitiveRestriction findOneByBdtPriRestriId(int bdtPriRestriId);
}
