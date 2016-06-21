package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BusinessObjectDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BusinessObjectDocumentRepository
        extends JpaRepository<BusinessObjectDocument, Integer> {

    @Query("select b from BusinessObjectDocument b where b.topLevelAbieId = ?1")
    public BusinessObjectDocument findByTopLevelAbieId(int topLevelAbieId);
}
