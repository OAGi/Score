package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.CoreDataTypeSupplementaryComponentAllowedPrimitive;

import java.util.List;

public interface CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository {

    public List<CoreDataTypeSupplementaryComponentAllowedPrimitive> findByCdtScId(int cdtScId);

    public CoreDataTypeSupplementaryComponentAllowedPrimitive findOneByCdtScIdAndCdtPriId(int cdtScId, int cdtPriId);

    public void save(CoreDataTypeSupplementaryComponentAllowedPrimitive coreDataTypeSupplementaryComponentAllowedPrimitive);
}
