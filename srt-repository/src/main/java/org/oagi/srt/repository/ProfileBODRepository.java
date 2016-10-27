package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.ProfileBOD;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProfileBODRepository extends JpaRepository<ProfileBOD, Long> {

    @Query("select p from ProfileBOD p where p.createdBy = ?1")
    public List<ProfileBOD> findAllByCreatedBy(long createdBy);
}
