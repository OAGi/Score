package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.TopLevelAbie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TopLevelAbieRepository
        extends JpaRepository<TopLevelAbie, Long> {

    @Query("select t from TopLevelAbie t where t.abie.abieId = ?1")
    public TopLevelAbie findByAbieId(long abieId);
}
