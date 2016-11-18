package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BusinessInformationEntityUserExtensionRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BusinessInformationEntityUserExtensionRevisionRepository extends
        JpaRepository<BusinessInformationEntityUserExtensionRevision, Long> {

    @Query("select b from BusinessInformationEntityUserExtensionRevision b where b.topLevelAbie.topLevelAbieId = ?1")
    public List<BusinessInformationEntityUserExtensionRevision> findByTopLevelAbieId(long topLevelAbieId);
}
