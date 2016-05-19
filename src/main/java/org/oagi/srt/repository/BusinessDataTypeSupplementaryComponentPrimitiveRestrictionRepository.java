package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BusinessDataTypeSupplementaryComponentPrimitiveRestriction;

import java.util.List;

public interface BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository {

    public List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> findByBdtScId(int bdtScId);

    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findOneByBdtScPriRestriId(int bdtScPriRestriId);

    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findOneByBdtScIdAndDefault(int bdtScId, boolean isDefault);

    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findOneByBdtScIdAndCdtScAwdPriXpsTypeMapId(
            int bdtScId, int cdtScAwdPriXpsTypeMapId
    );

    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findOneByBdtScIdAndCodeListId(
            int bdtScId, int codeListId
    );

    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findOneByBdtScIdAndAgencyIdListId(
            int bdtScId, int agencyIdListId
    );

    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findOneByBdtScIdAndCdtScAwdPriXpsTypeMapIdAndCodeListIdAndAgencyIdListId(
            int bdtScId, int cdtScAwdPriXpsTypeMapId, int codeListId, int agencyIdListId
    );

    public void save(BusinessDataTypeSupplementaryComponentPrimitiveRestriction businessDataTypeSupplementaryComponentPrimitiveRestriction);
}
