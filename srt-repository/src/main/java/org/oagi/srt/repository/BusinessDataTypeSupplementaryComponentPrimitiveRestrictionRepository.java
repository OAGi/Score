package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BusinessDataTypeSupplementaryComponentPrimitiveRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository extends
        JpaRepository<BusinessDataTypeSupplementaryComponentPrimitiveRestriction, Integer> {

    @Query("select b from BusinessDataTypeSupplementaryComponentPrimitiveRestriction b where b.bdtScId = ?1")
    public List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> findByBdtScId(int bdtScId);

    @Query("select b from BusinessDataTypeSupplementaryComponentPrimitiveRestriction b where b.bdtScId in ?1")
    public List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> findByBdtScIdIn(Collection<Integer> bdtScIds);

    @Query("select b from BusinessDataTypeSupplementaryComponentPrimitiveRestriction b where b.bdtScId = ?1 and b.isDefault = ?2")
    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findOneByBdtScIdAndDefault(int bdtScId, boolean isDefault);

    @Query("select b from BusinessDataTypeSupplementaryComponentPrimitiveRestriction b where b.bdtScId = ?1 and b.cdtScAwdPriXpsTypeMapId = ?2")
    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findOneByBdtScIdAndCdtScAwdPriXpsTypeMapId(
            int bdtScId, int cdtScAwdPriXpsTypeMapId
    );

    @Query("select b from BusinessDataTypeSupplementaryComponentPrimitiveRestriction b where b.bdtScId = ?1 and b.codeListId = ?2")
    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findOneByBdtScIdAndCodeListId(
            int bdtScId, int codeListId
    );

    @Query("select b from BusinessDataTypeSupplementaryComponentPrimitiveRestriction b where b.bdtScId = ?1 and b.agencyIdListId = ?2")
    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findOneByBdtScIdAndAgencyIdListId(
            int bdtScId, int agencyIdListId
    );

    @Query("select b from BusinessDataTypeSupplementaryComponentPrimitiveRestriction b where b.bdtScId = ?1 and b.cdtScAwdPriXpsTypeMapId = ?2 and b.codeListId = ?3 and b.agencyIdListId = ?4")
    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findOneByBdtScIdAndCdtScAwdPriXpsTypeMapIdAndCodeListIdAndAgencyIdListId(
            int bdtScId, int cdtScAwdPriXpsTypeMapId, int codeListId, int agencyIdListId
    );
}
