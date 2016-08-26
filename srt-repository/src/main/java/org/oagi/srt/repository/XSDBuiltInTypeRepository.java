package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.XSDBuiltInType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface XSDBuiltInTypeRepository extends JpaRepository<XSDBuiltInType, Long> {

    @Query("select x from XSDBuiltInType x where x.xbtId in ?1")
    public List<XSDBuiltInType> findByXbtIdIn(Collection<Long> xbtIds);

    @Query("select x from XSDBuiltInType x where x.name = ?1")
    public XSDBuiltInType findOneByName(String name);

    @Query("select x from XSDBuiltInType x where x.builtInType = ?1")
    public XSDBuiltInType findOneByBuiltInType(String builtInType);
}
