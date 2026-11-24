package org.oagi.score.gateway.http.api.business_term_management.service;

import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationQueryService;
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

    @Autowired
    private ApplicationConfigurationQueryService applicationConfigurationQueryService;

    /**
     * #1752 - H1: enforce the Business Term access policy server-side. A tenant with the feature
     * disabled cannot read business-term data via a direct API call. Developer-role users may still
     * <em>view</em> terms and the assignments already made, so this read gate does not exclude them;
     * creating and assigning is gated by {@link #assertBusinessTermManageable}.
     */
    public void assertBusinessTermEnabled(ScoreUser requester) {
        if (!applicationConfigurationQueryService.isBusinessTermEnabled(requester)) {
            throw new DataAccessForbiddenException("Business Term management is not enabled.");
        }
    }

    /**
     * The read gate plus the developer exclusion, for endpoints that only support creating or
     * assigning business terms (uniqueness checks, the BIE picker for assignment, the import
     * template). Public so the controller can apply it to endpoints that do not touch the repository.
     */
    public void assertBusinessTermManageable(ScoreUser requester) {
        assertBusinessTermEnabled(requester);
        if (requester.isDeveloper()) {
            throw new DataAccessForbiddenException(
                    "Business Term management is not available to developer-role users.");
        }
    }

    public ResultAndCount<BusinessTermListEntryRecord> getBusinessTermList(
            ScoreUser requester, BusinessTermListFilterCriteria filterCriteria, PageRequest pageRequest) {
        assertBusinessTermEnabled(requester);

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

        assertBusinessTermManageable(requester);
        var businessTermQuery = repositoryFactory.businessTermQueryRepository(requester);
        return businessTermQuery.checkUniqueness(businessTermId, businessTerm, externalReferenceUri);
    }

    public BusinessTermDetailsRecord getBusinessTermDetails(ScoreUser requester, BusinessTermId businessTermId) {

        assertBusinessTermEnabled(requester);
        var businessTermQuery = repositoryFactory.businessTermQueryRepository(requester);
        return businessTermQuery.getBusinessTermDetails(businessTermId);
    }

    public List<BusinessTermUsageRecord> getBusinessTermUsageList(ScoreUser requester, BusinessTermId businessTermId) {

        assertBusinessTermEnabled(requester);
        var businessTermQuery = repositoryFactory.businessTermQueryRepository(requester);
        return businessTermQuery.getBusinessTermUsageList(businessTermId);
    }

    public ResultAndCount<AssignedBusinessTermListEntryRecord> getAssignedBusinessTermList(
            ScoreUser requester, AssignedBusinessTermListFilterCriteria filterCriteria, PageRequest pageRequest) {

        assertBusinessTermEnabled(requester);
        var businessTermQuery = repositoryFactory.businessTermQueryRepository(requester);
        return businessTermQuery.getAssignedBusinessTermList(filterCriteria, pageRequest);
    }

    public boolean checkAssignmentUniqueness(
            ScoreUser requester, AsbieId asbieId, BusinessTermId businessTermId,
            String typeCode, AsbieBusinessTermId exceptAsbieBusinessTermId) {

        assertBusinessTermManageable(requester);
        var businessTermQuery = repositoryFactory.businessTermQueryRepository(requester);
        return businessTermQuery.checkAssignmentUniqueness(asbieId, businessTermId, typeCode, exceptAsbieBusinessTermId);
    }

    public boolean checkAssignmentUniqueness(
            ScoreUser requester, BbieId bbieId, BusinessTermId businessTermId,
            String typeCode, BbieBusinessTermId exceptBbieBusinessTermId) {

        assertBusinessTermManageable(requester);
        var businessTermQuery = repositoryFactory.businessTermQueryRepository(requester);
        return businessTermQuery.checkAssignmentUniqueness(bbieId, businessTermId, typeCode, exceptBbieBusinessTermId);
    }

    public ResultAndCount<AsbieBbieListEntryRecord> getAsbieBbieList(
            ScoreUser requester, AsbieBbieListFilterCriteria filterCriteria, PageRequest pageRequest) {

        assertBusinessTermManageable(requester);
        var businessTermQuery = repositoryFactory.businessTermQueryRepository(requester);
        return businessTermQuery.getAsbieBbieList(filterCriteria, pageRequest);
    }

    public List<AsbieBbieListEntryRecord> getAsbieBbieList(
            ScoreUser requester, Collection<AsbieId> asbieIdList, Collection<BbieId> bbieIdList) {

        assertBusinessTermManageable(requester);
        var businessTermQuery = repositoryFactory.businessTermQueryRepository(requester);
        return businessTermQuery.getAsbieBbieList(asbieIdList, bbieIdList);
    }

    public AssignedBusinessTermDetailsRecord getAssignedBusinessTermDetails(
            ScoreUser requester, AsbieBusinessTermId asbieBusinessTermId) {

        assertBusinessTermEnabled(requester);
        var businessTermQuery = repositoryFactory.businessTermQueryRepository(requester);
        return businessTermQuery.getAssignedBusinessTermDetails(asbieBusinessTermId);
    }

    public AssignedBusinessTermDetailsRecord getAssignedBusinessTermDetails(
            ScoreUser requester, BbieBusinessTermId bbieBusinessTermId) {

        assertBusinessTermEnabled(requester);
        var businessTermQuery = repositoryFactory.businessTermQueryRepository(requester);
        return businessTermQuery.getAssignedBusinessTermDetails(bbieBusinessTermId);
    }
}
