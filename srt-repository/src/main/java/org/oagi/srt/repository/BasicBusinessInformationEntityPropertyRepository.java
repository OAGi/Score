package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicBusinessInformationEntityProperty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BasicBusinessInformationEntityPropertyRepository
        extends JpaRepository<BasicBusinessInformationEntityProperty, Long>,
        BulkInsertRepository<BasicBusinessInformationEntityProperty> {

}
