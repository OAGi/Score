package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.IdEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface BulkInsertRepository<T extends IdEntity> {

    public void saveBulk(JpaRepository<T, Long> jpaRepository, Collection<T> entities);
}
