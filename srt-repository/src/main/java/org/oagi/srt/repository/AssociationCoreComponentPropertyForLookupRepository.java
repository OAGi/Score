package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AssociationCoreComponentPropertyForLookup;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AssociationCoreComponentPropertyForLookupRepository
        extends LookupRepository<AssociationCoreComponentPropertyForLookup, Long> {

    @Query("select a from AssociationCoreComponentPropertyForLookup a where a.revisionNum = ?1")
    public List<AssociationCoreComponentPropertyForLookup> findAllWithRevisionNum(int revisionNum);
}
