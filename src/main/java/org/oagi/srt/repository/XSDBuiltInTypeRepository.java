package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.XSDBuiltInType;

public interface XSDBuiltInTypeRepository {

    public XSDBuiltInType findOneByName(String name);

    public XSDBuiltInType findOneByBuiltInType(String builtInType);

    public XSDBuiltInType findOneByXbtId(int xbtId);
}
