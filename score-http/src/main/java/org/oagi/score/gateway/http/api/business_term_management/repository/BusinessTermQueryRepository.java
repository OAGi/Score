package org.oagi.score.gateway.http.api.business_term_management.repository;

import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.business_term_management.model.*;
import org.oagi.score.gateway.http.api.business_term_management.repository.criteria.AsbieBbieListFilterCriteria;
import org.oagi.score.gateway.http.api.business_term_management.repository.criteria.AssignedBusinessTermListFilterCriteria;
import org.oagi.score.gateway.http.api.business_term_management.repository.criteria.BusinessTermListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;

import java.util.Collection;
import java.util.List;

public interface BusinessTermQueryRepository {

    ResultAndCount<BusinessTermListEntryRecord> getBusinessTermList(
            BusinessTermListFilterCriteria filterCriteria, PageRequest pageRequest);

    ResultAndCount<BusinessTermListEntryRecord> getBusinessTermListByAssignedBieList(
            BusinessTermListFilterCriteria filterCriteria, PageRequest pageRequest);

    boolean checkUniqueness(BusinessTermId businessTermId, String businessTerm, String externalReferenceUri);

    boolean checkNameUniqueness(BusinessTermId businessTermId, String businessTerm);

    /**
     * #1752 - H2: returns {@code true} when the business term is referenced by any CC-level link
     * ({@code ascc_bizterm}/{@code bcc_bizterm}), i.e. it is assigned and cannot be discarded.
     */
    boolean isBusinessTermUsed(BusinessTermId businessTermId);

    BusinessTermDetailsRecord getBusinessTermDetails(BusinessTermId businessTermId);

    ResultAndCount<AssignedBusinessTermListEntryRecord> getAssignedBusinessTermList(
            AssignedBusinessTermListFilterCriteria filterCriteria, PageRequest pageRequest);

    boolean checkAssignmentUniqueness(
            AsbieId asbieId, BusinessTermId businessTermId, String typeCode, boolean primaryIndicator);

    boolean checkAssignmentUniqueness(
            BbieId bbieId, BusinessTermId businessTermId, String typeCode, boolean primaryIndicator);

    AssignedBusinessTermDetailsRecord getAssignedBusinessTermDetails(AsbieBusinessTermId asbieBusinessTermId);

    AssignedBusinessTermDetailsRecord getAssignedBusinessTermDetails(BbieBusinessTermId bbieBusinessTermId);

    ResultAndCount<AsbieBbieListEntryRecord> getAsbieBbieList(
            AsbieBbieListFilterCriteria filterCriteria, PageRequest pageRequest);

    List<AsbieBbieListEntryRecord> getAsbieBbieList(
            Collection<AsbieId> asbieIdList, Collection<BbieId> bbieIdList);


}
