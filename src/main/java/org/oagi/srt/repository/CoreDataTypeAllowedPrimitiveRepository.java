package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.CoreDataTypeAllowedPrimitive;

import java.util.List;

public interface CoreDataTypeAllowedPrimitiveRepository {

    public List<CoreDataTypeAllowedPrimitive> findByCdtId(int cdtId);

    public CoreDataTypeAllowedPrimitive findOneByCdtAwdPriId(int cdtAwdPriId);
}
