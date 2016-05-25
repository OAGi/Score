package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.XSDBuiltInType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface XSDBuiltInTypeRepository extends JpaRepository<XSDBuiltInType, Integer> {

    @Query("select x from XSDBuiltInType x where x.name = ?1")
    public XSDBuiltInType findOneByName(String name);

    @Query("select x from XSDBuiltInType x where x.builtInType = ?1")
    public XSDBuiltInType findOneByBuiltInType(String builtInType);
}
