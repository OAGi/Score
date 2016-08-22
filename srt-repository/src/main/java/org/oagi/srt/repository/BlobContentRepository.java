package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BlobContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BlobContentRepository extends JpaRepository<BlobContent, Long> {

    @Query("select b from BlobContent b where b.module.module like %?1")
    public BlobContent findByModuleEndsWith(String module);
}
