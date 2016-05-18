package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BusinessDataTypeSupplementaryComponentPrimitiveRestriction;

import java.util.List;

public interface BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository {

    public List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> findByBdtScId(int bdtScId);

    public void save(BusinessDataTypeSupplementaryComponentPrimitiveRestriction businessDataTypeSupplementaryComponentPrimitiveRestriction);
}
