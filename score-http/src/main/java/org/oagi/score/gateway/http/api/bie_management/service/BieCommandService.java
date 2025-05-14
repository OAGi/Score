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
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.message_management.model.MessageId;
import org.oagi.score.gateway.http.api.message_management.service.MessageCommandService;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.oagi.score.gateway.http.common.model.ScoreRole.ADMINISTRATOR;

@Service
@Transactional
public class BieCommandService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private BusinessInformationEntityRepository bieRepository;

    @Autowired
    private MessageCommandService messageCommandService;

    @Autowired
    private SessionService sessionService;

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

    public void discardBieList(ScoreUser requester, Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {
        if (topLevelAsbiepIdList == null || topLevelAsbiepIdList.isEmpty()) {
            return;
        }

        /*
         * Issue #772, #1010
         */
        ensureProperDeleteBieRequest(requester, topLevelAsbiepIdList);

        var command = repositoryFactory.bieCommandRepository(requester);
        command.deleteByTopLevelAsbiepIdList(topLevelAsbiepIdList);
    }

    private void ensureProperDeleteBieRequest(ScoreUser requester, Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {

        int failureCount = 0;
        StringBuilder failureMessageBody = new StringBuilder();

        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        var openApiDocumentQuery = repositoryFactory.openApiDocumentQueryRepository(requester);

        // Issue #1569
        // check to see if the BIE is referenced in an OpenAPI document
        for (TopLevelAsbiepId topLevelAsbiepId : topLevelAsbiepIdList) {
            if (openApiDocumentQuery.hasTopLevelAsbiepReference(topLevelAsbiepId)) {
                throw new DataAccessForbiddenException("Cannot delete the BIE '" + topLevelAsbiepId + "'. please remove the BIE from the OpenAPI document first.");
            }

            TopLevelAsbiepSummaryRecord topLevelAsbiepSummary = null;

            // Issue #1576
            // Administrator can discard BIEs in any state.
            if (!requester.hasRole(ADMINISTRATOR)) {
                topLevelAsbiepSummary = topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);
                if (topLevelAsbiepSummary.state() == BieState.Production) {
                    throw new DataAccessForbiddenException("Not allowed to delete the BIE in '" + topLevelAsbiepSummary.state() + "' state.");
                }

                UserId requesterUserId = requester.userId();
                if (!requesterUserId.equals(topLevelAsbiepSummary.owner().userId())) {
                    throw new DataAccessForbiddenException("Only allowed to delete the BIE by the owner.");
                }
            }

            // Issue #1010
            List<TopLevelAsbiepSummaryRecord> reusedTopLevelAsbiepList =
                    topLevelAsbiepQuery.getReusedTopLevelAsbiepSummaryList(topLevelAsbiepId);
            if (!reusedTopLevelAsbiepList.isEmpty()) {
                failureCount += 1;

                if (topLevelAsbiepSummary == null) {
                    topLevelAsbiepSummary = topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);
                }
                failureMessageBody = failureMessageBody.append("\n---\n[**")
                        .append(topLevelAsbiepSummary.propertyTerm())
                        .append("**](")
                        .append("/profile_bie/").append(topLevelAsbiepId)
                        .append(") (")
                        .append(topLevelAsbiepSummary.guid())
                        .append(") cannot be discarded due to the referential integrity violation by following BIEs:")
                        .append("\n\n");
                for (TopLevelAsbiepSummaryRecord target : reusedTopLevelAsbiepList) {
                    failureMessageBody = failureMessageBody.append("- [")
                            .append(target.propertyTerm())
                            .append("](")
                            .append("/profile_bie/").append(target.topLevelAsbiepId())
                            .append(") (")
                            .append(target.guid())
                            .append(")\n");
                }
            }
        }

        if (failureCount > 0) { // i.e. failed?
            String subject = "Failed to discard BIE" + ((failureCount > 1) ? "s" : "");
            MessageId errorMessageId = messageCommandService.asyncSendMessage(
                    sessionService.getScoreSystemUser(),
                    Arrays.asList(requester.userId()),
                    subject,
                    failureMessageBody.toString(),
                    "text/markdown").join().values().iterator().next();

            throw new DataAccessForbiddenException(subject, errorMessageId.value());
        }
    }

}
