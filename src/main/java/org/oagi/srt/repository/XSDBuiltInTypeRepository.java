package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.XSDBuiltInType;

import java.util.List;

public interface XSDBuiltInTypeRepository {

    public List<XSDBuiltInType> findAll();

    public XSDBuiltInType findOneByName(String name);

    public XSDBuiltInType findOneByBuiltInType(String builtInType);

    public XSDBuiltInType findOneByXbtId(int xbtId);
}
