package org.oagi.srt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface BulkInsertRepository<T> {

    public void saveBulk(JpaRepository<T, Integer> jpaRepository, Collection<T> entities);
}
