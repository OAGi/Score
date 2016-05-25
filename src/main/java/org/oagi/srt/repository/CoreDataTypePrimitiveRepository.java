package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.CoreDataTypePrimitive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CoreDataTypePrimitiveRepository extends JpaRepository<CoreDataTypePrimitive, Integer> {

    @Query("select c from CoreDataTypePrimitive c where c.name = ?1")
    public CoreDataTypePrimitive findOneByName(String name);
}
