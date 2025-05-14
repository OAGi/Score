package org.oagi.score.gateway.http.api.business_term_management.service;

import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.business_term_management.model.*;
import org.oagi.score.gateway.http.api.business_term_management.repository.criteria.AsbieBbieListFilterCriteria;
import org.oagi.score.gateway.http.api.business_term_management.repository.criteria.AssignedBusinessTermListFilterCriteria;
import org.oagi.score.gateway.http.api.business_term_management.repository.criteria.BusinessTermListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BusinessTermQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    public ResultAndCount<BusinessTermListEntryRecord> getBusinessTermList(
            ScoreUser requester, BusinessTermListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var businessTermQuery = repositoryFactory.businessTermQueryRepository(requester);
        if ((filterCriteria.byAssignedAsbieIdList() != null && filterCriteria.byAssignedAsbieIdList().size() > 0) ||
                (filterCriteria.byAssignedBbieIdList() != null && filterCriteria.byAssignedBbieIdList().size() > 0)) {
            return businessTermQuery.getBusinessTermListByAssignedBieList(filterCriteria, pageRequest);
        } else {
            return businessTermQuery.getBusinessTermList(filterCriteria, pageRequest);
        }
    }

    public boolean checkUniqueness(
            ScoreUser requester, BusinessTermId businessTermId, String businessTerm, String externalReferenceUri) {

        var businessTermQuery = repositoryFactory.businessTermQueryRepository(requester);
        return businessTermQuery.checkUniqueness(businessTermId, businessTerm, externalReferenceUri);
    }

    public boolean checkNameUniqueness(
            ScoreUser requester, BusinessTermId businessTermId, String businessTerm) {

        var businessTermQuery = repositoryFactory.businessTermQueryRepository(requester);
        return businessTermQuery.checkNameUniqueness(businessTermId, businessTerm);
    }

    public BusinessTermDetailsRecord getBusinessTermDetails(ScoreUser requester, BusinessTermId businessTermId) {

        var businessTermQuery = repositoryFactory.businessTermQueryRepository(requester);
        return businessTermQuery.getBusinessTermDetails(businessTermId);
    }

    public ResultAndCount<AssignedBusinessTermListEntryRecord> getAssignedBusinessTermList(
            ScoreUser requester, AssignedBusinessTermListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var businessTermQuery = repositoryFactory.businessTermQueryRepository(requester);
        return businessTermQuery.getAssignedBusinessTermList(filterCriteria, pageRequest);
    }

    public boolean checkAssignmentUniqueness(
            ScoreUser requester, AsbieId asbieId, BusinessTermId businessTermId,
            String typeCode, Boolean primaryIndicator) {

        var businessTermQuery = repositoryFactory.businessTermQueryRepository(requester);
        return businessTermQuery.checkAssignmentUniqueness(asbieId, businessTermId, typeCode, primaryIndicator);
    }

    public boolean checkAssignmentUniqueness(
            ScoreUser requester, BbieId bbieId, BusinessTermId businessTermId,
            String typeCode, Boolean primaryIndicator) {

        var businessTermQuery = repositoryFactory.businessTermQueryRepository(requester);
        return businessTermQuery.checkAssignmentUniqueness(bbieId, businessTermId, typeCode, primaryIndicator);
    }

    public ResultAndCount<AsbieBbieListEntryRecord> getAsbieBbieList(
            ScoreUser requester, AsbieBbieListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var businessTermQuery = repositoryFactory.businessTermQueryRepository(requester);
        return businessTermQuery.getAsbieBbieList(filterCriteria, pageRequest);
    }

    public List<AsbieBbieListEntryRecord> getAsbieBbieList(
            ScoreUser requester, Collection<AsbieId> asbieIdList, Collection<BbieId> bbieIdList) {

        var businessTermQuery = repositoryFactory.businessTermQueryRepository(requester);
        return businessTermQuery.getAsbieBbieList(asbieIdList, bbieIdList);
    }

    public AssignedBusinessTermDetailsRecord getAssignedBusinessTermDetails(
            ScoreUser requester, AsbieBusinessTermId asbieBusinessTermId) {

        var businessTermQuery = repositoryFactory.businessTermQueryRepository(requester);
        return businessTermQuery.getAssignedBusinessTermDetails(asbieBusinessTermId);
    }

    public AssignedBusinessTermDetailsRecord getAssignedBusinessTermDetails(
            ScoreUser requester, BbieBusinessTermId bbieBusinessTermId) {

        var businessTermQuery = repositoryFactory.businessTermQueryRepository(requester);
        return businessTermQuery.getAssignedBusinessTermDetails(bbieBusinessTermId);
    }
}
