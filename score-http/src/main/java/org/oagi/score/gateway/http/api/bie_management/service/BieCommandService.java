package org.oagi.score.gateway.http.api.bie_management.service;

import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.BieCreateRequest;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.repository.BusinessInformationEntityRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.InsertAbieArguments;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.InsertAsbiepArguments;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.InsertBizCtxAssignmentArguments;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.InsertTopLevelAsbiepArguments;
import org.oagi.score.gateway.http.api.bie_management.service.state_transition.BieStateTransitionService;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.oagi.score.gateway.http.common.model.ScoreRole.ADMINISTRATOR;

@Service
@Transactional
public class BieCommandService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private BusinessInformationEntityRepository bieRepository;

    @Autowired
    private BieStateTransitionService bieStateTransitionService;

    @Transactional
    public TopLevelAsbiepId createBie(ScoreUser requester, BieCreateRequest request) {
        if (requester == null) {
            throw new IllegalArgumentException("`requester` parameter must not be null.");
        }

        if (request.asccpManifestId() == null) {
            throw new IllegalArgumentException("`asccpManifestId` parameter must not be null.");
        }

        List<BusinessContextId> bizCtxIdList = request.bizCtxIdList();
        if (bizCtxIdList == null || bizCtxIdList.isEmpty()) {
            throw new IllegalArgumentException("`bizCtxIdList` parameter must not be null.");
        }

        var asccpQuery = repositoryFactory.asccpQueryRepository(requester);

        AsccpSummaryRecord asccp = asccpQuery.getAsccpSummary(request.asccpManifestId());
        if (asccp == null) {
            throw new IllegalArgumentException();
        }

        var accQuery = repositoryFactory.accQueryRepository(requester);

        AccSummaryRecord roleOfAcc = accQuery.getAccSummary(asccp.roleOfAccManifestId());
        if (roleOfAcc.isGroup()) {
            throw new IllegalArgumentException("Cannot create BIE of `ASCCP` with group `ACC`.");
        }

        String asccpPath = "ASCCP-" + asccp.asccpManifestId();
        String accPath = "ACC-" + asccp.roleOfAccManifestId();
        accPath = String.join(">",
                Arrays.asList(asccpPath, accPath));

        long millis = System.currentTimeMillis();

        var topLevelAsbiepCommand = repositoryFactory.topLevelAsbiepCommandRepository(requester);
        TopLevelAsbiepId topLevelAsbiepId = new InsertTopLevelAsbiepArguments(topLevelAsbiepCommand)
                .setUserId(requester.userId())
                .setReleaseId(asccp.release().releaseId())
                .setTimestamp(millis)
                .execute();

        AbieId abieId = new InsertAbieArguments(
                repositoryFactory.abieCommandRepository(requester))
                .setUserId(requester.userId())
                .setTopLevelAsbiepId(topLevelAsbiepId)
                .setAccManifestId(asccp.roleOfAccManifestId())
                .setPath(accPath)
                .setTimestamp(millis)
                .execute();

        new InsertBizCtxAssignmentArguments(topLevelAsbiepCommand)
                .setTopLevelAsbiepId(topLevelAsbiepId)
                .setBizCtxIds(bizCtxIdList)
                .execute();

        AsbiepId asbiepId = new InsertAsbiepArguments(
                repositoryFactory.asbiepCommandRepository(requester))
                .setAsccpManifestId(asccp.asccpManifestId())
                .setRoleOfAbieId(abieId)
                .setTopLevelAsbiepId(topLevelAsbiepId)
                .setPath(asccpPath)
                .setUserId(requester.userId())
                .setTimestamp(millis)
                .execute();

        topLevelAsbiepCommand.updateAsbiepId(asbiepId, topLevelAsbiepId);

        return topLevelAsbiepId;
    }

    public void discardBieList(ScoreUser requester,
                               Collection<TopLevelAsbiepId> topLevelAsbiepIdList,
                               Collection<TopLevelAsbiepId> dependencyTopLevelAsbiepIds) {
        if (topLevelAsbiepIdList == null || topLevelAsbiepIdList.isEmpty()) {
            return;
        }

        /*
         * Issue #772, #1010
         */
        ensureProperDeleteBieRequest(requester, topLevelAsbiepIdList, dependencyTopLevelAsbiepIds);

        Set<TopLevelAsbiepId> deleteTargetIds = new LinkedHashSet<>(topLevelAsbiepIdList);
        if (dependencyTopLevelAsbiepIds != null) {
            deleteTargetIds.addAll(dependencyTopLevelAsbiepIds);
        }

        var command = repositoryFactory.bieCommandRepository(requester);
        command.deleteByTopLevelAsbiepIdList(deleteTargetIds);
    }

    private void ensureProperDeleteBieRequest(ScoreUser requester,
                                              Collection<TopLevelAsbiepId> topLevelAsbiepIdList,
                                              Collection<TopLevelAsbiepId> dependencyTopLevelAsbiepIds) {
        Set<TopLevelAsbiepId> deleteTargetIds = new LinkedHashSet<>(topLevelAsbiepIdList);
        if (dependencyTopLevelAsbiepIds != null) {
            deleteTargetIds.addAll(dependencyTopLevelAsbiepIds);
        }
        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        var openApiDocumentQuery = repositoryFactory.openApiDocumentQueryRepository(requester);

        for (TopLevelAsbiepId topLevelAsbiepId : deleteTargetIds) {
            // Issue #1569
            // check to see if the BIE is referenced in an OpenAPI document
            if (openApiDocumentQuery.hasTopLevelAsbiepReference(topLevelAsbiepId)) {
                throw new DataAccessForbiddenException("Cannot delete the BIE '" + topLevelAsbiepId + "'. please remove the BIE from the OpenAPI document first.");
            }

            TopLevelAsbiepSummaryRecord topLevelAsbiepSummary =
                    topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);
            if (topLevelAsbiepSummary == null) {
                continue;
            }

            if (!requester.hasRole(ADMINISTRATOR) && topLevelAsbiepSummary.state() != BieState.WIP) {
                throw new DataAccessForbiddenException("Not allowed to delete the BIE in '" + topLevelAsbiepSummary.state() + "' state.");
            }

            // Issue #1576
            // Administrator can discard BIEs in any state.
            if (!requester.hasRole(ADMINISTRATOR)) {
                UserId requesterUserId = requester.userId();
                if (!requesterUserId.equals(topLevelAsbiepSummary.owner().userId())) {
                    throw new DataAccessForbiddenException("Only allowed to delete the BIE by the owner.");
                }
            }
        }

        // Issue #1010
        bieStateTransitionService.ensureDependencySelectionStateChange(
                requester,
                topLevelAsbiepIdList,
                BieState.Discard,
                dependencyTopLevelAsbiepIds,
                List.of());
    }

}
