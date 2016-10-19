package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.TopLevelAbie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TopLevelAbieRepository
        extends JpaRepository<TopLevelAbie, Long> {

    @Query("select t from TopLevelAbie t where t.abie.abieId = ?1")
    public TopLevelAbie findByAbieId(long abieId);

    @Modifying
    @Query("update TopLevelAbie t set t.abie.abieId = NULL where t.topLevelAbieId = ?1")
    public void updateAbieToNull(long topLevelAbieId);
}
