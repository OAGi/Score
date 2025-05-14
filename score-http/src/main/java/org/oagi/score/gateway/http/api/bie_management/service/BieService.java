package org.oagi.score.gateway.http.api.bie_management.service;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.BieTransferOwnershipListRequest;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.BieUpdateStateListRequest;
import org.oagi.score.gateway.http.api.bie_management.model.BieEvent;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.model.ScoreRole.ADMINISTRATOR;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

@Service
@Transactional(readOnly = true)
public class BieService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private BieEditService bieEditService;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public BusinessContextSummaryRecord findBizCtxByAbieId(ScoreUser requester, AbieId abieId) {
        var abieQuery = repositoryFactory.abieQueryRepository(requester);
        TopLevelAsbiepId topLevelAsbiepId = abieQuery.getAbieSummary(abieId).ownerTopLevelAsbiepId();
        // return the first biz ctx of the specific topLevelAsbiepId
        var bizCtxQuery = repositoryFactory.businessContextQueryRepository(requester);
        return bizCtxQuery.getBusinessContextSummaryList(topLevelAsbiepId).get(0);
    }

//    @Transactional
//    public void deleteBieList(ScoreUser requester, List<BigInteger> topLevelAsbiepIds) {
//        if (topLevelAsbiepIds == null || topLevelAsbiepIds.isEmpty()) {
//            return;
//        }
//
//        /*
//         * Issue #772, #1010
//         */
//        ensureProperDeleteBieRequest(requester, topLevelAsbiepIds);
//
//        dslContext.query("SET FOREIGN_KEY_CHECKS = 0").execute();
//
//        dslContext.deleteFrom(ABIE).where(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();
//        dslContext.deleteFrom(ASBIE).where(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();
//        dslContext.deleteFrom(ASBIEP).where(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();
//
//        dslContext.deleteFrom(Tables.BBIE).where(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();
//        dslContext.deleteFrom(Tables.BBIEP).where(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();
//
//        dslContext.deleteFrom(Tables.BBIE_SC).where(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();
//        dslContext.deleteFrom(Tables.TOP_LEVEL_ASBIEP).where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();
//        dslContext.deleteFrom(Tables.BIZ_CTX_ASSIGNMENT).where(Tables.BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)).execute();
//
//        // Issue #1492
//        List<ULong> oasMessageBodyIdList = dslContext.selectDistinct(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID)
//                .from(OAS_MESSAGE_BODY)
//                .where(OAS_MESSAGE_BODY.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds))
//                .fetchInto(ULong.class);
//        if (!oasMessageBodyIdList.isEmpty()) {
//            List<ULong> oasRequestIdList = dslContext.selectDistinct(OAS_REQUEST.OAS_REQUEST_ID)
//                    .from(OAS_REQUEST)
//                    .where(OAS_REQUEST.OAS_MESSAGE_BODY_ID.in(oasMessageBodyIdList))
//                    .fetchInto(ULong.class);
//            if (!oasRequestIdList.isEmpty()) {
//                dslContext.deleteFrom(OAS_REQUEST_PARAMETER)
//                        .where(OAS_REQUEST_PARAMETER.OAS_REQUEST_ID.in(oasRequestIdList))
//                        .execute();
//                dslContext.deleteFrom(OAS_REQUEST)
//                        .where(OAS_REQUEST.OAS_REQUEST_ID.in(oasRequestIdList))
//                        .execute();
//            }
//            List<ULong> oasResponseIdList = dslContext.selectDistinct(OAS_RESPONSE.OAS_RESPONSE_ID)
//                    .from(OAS_RESPONSE)
//                    .where(OAS_RESPONSE.OAS_MESSAGE_BODY_ID.in(oasMessageBodyIdList))
//                    .fetchInto(ULong.class);
//            if (!oasResponseIdList.isEmpty()) {
//                dslContext.deleteFrom(OAS_RESPONSE_HEADERS)
//                        .where(OAS_RESPONSE_HEADERS.OAS_RESPONSE_ID.in(oasResponseIdList))
//                        .execute();
//                dslContext.deleteFrom(OAS_RESPONSE)
//                        .where(OAS_RESPONSE.OAS_RESPONSE_ID.in(oasResponseIdList))
//                        .execute();
//            }
//            dslContext.deleteFrom(OAS_MESSAGE_BODY)
//                    .where(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID.in(oasMessageBodyIdList))
//                    .execute();
//        }
//
//        // Issue #1615
//        List<ULong> biePackageIdList = dslContext.select(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID)
//                .from(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
//                .where(BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds))
//                .fetchInto(ULong.class);
//
//        dslContext.deleteFrom(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
//                .where(BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds))
//                .execute();
//
//        dslContext.query("SET FOREIGN_KEY_CHECKS = 1").execute();
//
//        List<ULong> topLevelAsbiepListThatHasThisAsSource = dslContext.select(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
//                .from(TOP_LEVEL_ASBIEP)
//                .where(TOP_LEVEL_ASBIEP.SOURCE_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds))
//                .fetchInto(ULong.class);
//        if (!topLevelAsbiepListThatHasThisAsSource.isEmpty()) {
//            dslContext.update(TOP_LEVEL_ASBIEP)
//                    .setNull(TOP_LEVEL_ASBIEP.SOURCE_TOP_LEVEL_ASBIEP_ID)
//                    .setNull(TOP_LEVEL_ASBIEP.SOURCE_ACTION)
//                    .setNull(TOP_LEVEL_ASBIEP.SOURCE_TIMESTAMP)
//                    .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepListThatHasThisAsSource))
//                    .execute();
//        }
//
//
//        // Issue #1635
//        dslContext.update(TOP_LEVEL_ASBIEP)
//                .setNull(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID)
//                .where(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds))
//                .execute();
//    }

//    private void ensureProperDeleteBieRequest(ScoreUser requester, List<BigInteger> topLevelAsbiepIds) {
//        // Issue #1569
//        // check to see if the BIE is referenced in an OpenAPI document
//        Result<Record1<ULong>> resultForOasDocId = dslContext.select(OAS_DOC.OAS_DOC_ID)
//                .from(OAS_DOC)
//                .fetch();
//        List<BigInteger> oasDocIds = Collections.emptyList();
//        if (resultForOasDocId != null) {
//            oasDocIds = resultForOasDocId.stream().map(r -> r.value1().toBigInteger()).collect(Collectors.toList());
//        }
//        if (!oasDocIds.isEmpty()) {
//            for (BigInteger oasDocId : oasDocIds) {
//                List<BigInteger> topLevelAsbiepIdsInOasDoc = new ArrayList<>();
//                GetBieForOasDocRequest getBieForOasDocRequest = new GetBieForOasDocRequest(requester);
//                getBieForOasDocRequest.setOasDocId(oasDocId);
//                GetBieForOasDocResponse bieForOasDocTable = oasDocService.getBieForOasDoc(getBieForOasDocRequest);
//                List<BieForOasDoc> bieListForOasDoc = bieForOasDocTable.getResults();
//                if (bieListForOasDoc != null) {
//                    topLevelAsbiepIdsInOasDoc = bieListForOasDoc.stream().map(s -> s.getTopLevelAsbiepId()).collect(Collectors.toList());
//                    if (topLevelAsbiepIdsInOasDoc != null) {
//                        for (TopLevelAsbiepId topLevelAsbiepId : topLevelAsbiepIds) {
//                            if (topLevelAsbiepIdsInOasDoc.contains(topLevelAsbiepId)) {
//                                throw new DataAccessForbiddenException("Cannot delete the BIE '" + topLevelAsbiepId + "'. please remove the BIE from the OpenAPI document first.");
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        Result<Record2<String, ULong>> result =
//                dslContext.select(TOP_LEVEL_ASBIEP.STATE, TOP_LEVEL_ASBIEP.OWNER_USER_ID)
//                        .from(TOP_LEVEL_ASBIEP)
//                        .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(
//                                topLevelAsbiepIds.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())
//                        ))
//                        .fetch();
//
//        // Issue #1576
//        // Administrator can discard BIEs in any state.
//        if (!requester.hasRole(ADMINISTRATOR)) {
//            BigInteger requesterUserId = requester.getUserId();
//            for (Record2<String, ULong> record : result) {
//                BieState bieState = BieState.valueOf(record.value1());
//                if (bieState == BieState.Production) {
//                    throw new DataAccessForbiddenException("Not allowed to delete the BIE in '" + bieState + "' state.");
//                }
//
//                if (!requesterUserId.equals(record.value2().toBigInteger())) {
//                    throw new DataAccessForbiddenException("Only allowed to delete the BIE by the owner.");
//                }
//            }
//        }
//
//        // Issue #1010
//        int failureCount = 0;
//        StringBuilder failureMessageBody = new StringBuilder();
//        BieReadRepository bieReadRepository = scoreRepositoryFactory.createBieReadRepository();
//        for (TopLevelAsbiepId topLevelAsbiepId : topLevelAsbiepIds) {
//            List<org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiep> reusedTopLevelAsbiepList =
//                    bieReadRepository.getReuseBieList(new GetReuseBieListRequest(requester)
//                                    .withTopLevelAsbiepId(topLevelAsbiepId, true))
//                            .getTopLevelAsbiepList();
//
//            if (!reusedTopLevelAsbiepList.isEmpty()) {
//                failureCount += 1;
//                Record source = selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId));
//                failureMessageBody = failureMessageBody.append("\n---\n[**")
//                        .append(source.get(ASCCP.PROPERTY_TERM))
//                        .append("**](")
//                        .append("/profile_bie/").append(topLevelAsbiepId)
//                        .append(") (")
//                        .append(source.get(ASBIEP.GUID))
//                        .append(") cannot be discarded due to the referential integrity violation by following BIEs:")
//                        .append("\n\n");
//                for (org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiep target : reusedTopLevelAsbiepList) {
//                    failureMessageBody = failureMessageBody.append("- [")
//                            .append(target.getPropertyTerm())
//                            .append("](")
//                            .append("/profile_bie/").append(target.getTopLevelAsbiepId())
//                            .append(") (")
//                            .append(target.getGuid())
//                            .append(")\n");
//                }
//            }
//        }
//
//        if (failureCount > 0) { // i.e. failed?
//            SendMessageRequest sendMessageRequest = new SendMessageRequest(
//                    sessionService.getScoreSystemUser())
//                    .withRecipient(requester)
//                    .withSubject("Failed to discard BIE" + ((failureCount > 1) ? "s" : ""))
//                    .withBody(failureMessageBody.toString())
//                    .withBodyContentType(SendMessageRequest.MARKDOWN_CONTENT_TYPE);
//
//            BigInteger errorMessageId = messageService.asyncSendMessage(sendMessageRequest).join()
//                    .getMessageIds().values().iterator().next();
//            throw new DataAccessForbiddenException(sendMessageRequest.getSubject(), errorMessageId);
//        }
//    }

    public Record2<String, String> selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(
            TopLevelAsbiepId topLevelAsbiepId) {
        return dslContext.select(ASCCP.PROPERTY_TERM, ASBIEP.GUID)
                .from(TOP_LEVEL_ASBIEP)
                .join(ASBIEP).on(and(
                        TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID),
                        TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID)
                ))
                .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId.value())))
                .fetchOne();
    }

    private SelectConditionStep<Record3<ULong, String, String>> selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepIdList(
            List<TopLevelAsbiepId> topLevelAsbiepIdList) {
        Condition cond = (topLevelAsbiepIdList.size() == 1) ?
                TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepIdList.get(0).value())) :
                TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(
                        topLevelAsbiepIdList.stream()
                                .map(e -> ULong.valueOf(e.value())).collect(Collectors.toSet())
                );
        return dslContext.select(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID, ASCCP.PROPERTY_TERM, ASBIEP.GUID)
                .from(TOP_LEVEL_ASBIEP)
                .join(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .where(cond);
    }

    @Transactional
    public void transferOwnership(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId, String targetLoginId) {
        long ownerAppUserId;
        // Issue #1576
        // Even if the administrator does not own BIE, they can transfer ownership.
        if (requester.hasRole(ADMINISTRATOR)) {
            ownerAppUserId = dslContext.select(TOP_LEVEL_ASBIEP.OWNER_USER_ID)
                    .from(TOP_LEVEL_ASBIEP)
                    .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId.value())))
                    .fetchOptionalInto(Long.class).orElse(0L);
        } else {
            ownerAppUserId = dslContext.select(APP_USER.APP_USER_ID)
                    .from(APP_USER)
                    .where(APP_USER.LOGIN_ID.equalIgnoreCase(requester.username()))
                    .fetchOptionalInto(Long.class).orElse(0L);
        }
        if (ownerAppUserId == 0L) {
            throw new IllegalArgumentException("Not found an owner user.");
        }

        Long targetAppUserId = dslContext.select(APP_USER.APP_USER_ID)
                .from(APP_USER)
                .where(APP_USER.LOGIN_ID.equalIgnoreCase(targetLoginId))
                .fetchOptionalInto(Long.class).orElse(null);
        if (targetAppUserId == null) {
            throw new IllegalArgumentException("Not found a target user.");
        }

        if (dslContext.selectCount()
                .from(TOP_LEVEL_ASBIEP)
                .where(and(
                        TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(ULong.valueOf(ownerAppUserId)),
                        TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId.value()))
                ))
                .fetchOptionalInto(Integer.class).orElse(0) == 0) {
            throw new IllegalArgumentException("This BIE is not owned by the current user.");
        }

        dslContext.update(TOP_LEVEL_ASBIEP)
                .set(TOP_LEVEL_ASBIEP.OWNER_USER_ID, ULong.valueOf(targetAppUserId))
                .where(and(
                        TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(ULong.valueOf(ownerAppUserId)),
                        TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId.value()))
                ))
                .execute();
    }

    public void fireBieEvent(BieEvent event) {
        try {
            simpMessagingTemplate.convertAndSend("/topic/bie/" + event.getTopLevelAsbiepId(), event);
        } catch (Exception ignore) {
            logger.error("Couldn't send BIE event: " + event, ignore);
        }
    }

    @Transactional
    public void updateStateBieList(ScoreUser user, BieUpdateStateListRequest request) {
        request.getTopLevelAsbiepIds().forEach(topLevelAsbiepId -> {
            bieEditService.updateState(user, topLevelAsbiepId, request.getToState());
        });
    }

    @Transactional
    public void transferOwnershipList(ScoreUser user, BieTransferOwnershipListRequest request) {
        request.getTopLevelAsbiepIds().forEach(topLevelAsbiepId -> {
            transferOwnership(user, topLevelAsbiepId, request.getTargetLoginId());
        });
    }

}
