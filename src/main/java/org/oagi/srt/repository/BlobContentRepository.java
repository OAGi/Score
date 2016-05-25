package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BlobContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlobContentRepository extends JpaRepository<BlobContent, Integer> {
}
