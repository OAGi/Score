package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.CoreDataTypePrimitive;

public interface CoreDataTypePrimitiveRepository {

    public CoreDataTypePrimitive findOneByCdtPriId(int cdtPriId);
}
