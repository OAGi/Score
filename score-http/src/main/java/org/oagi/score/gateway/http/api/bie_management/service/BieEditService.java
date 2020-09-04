package org.oagi.score.gateway.http.api.bie_management.service;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.types.ULong;
import org.oagi.score.data.ACC;
import org.oagi.score.data.AppUser;
import org.oagi.score.data.BieState;
import org.oagi.score.data.TopLevelAsbiep;
import org.oagi.score.entity.jooq.Tables;
import org.oagi.score.entity.jooq.tables.records.*;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.*;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree.*;
import org.oagi.score.gateway.http.api.bie_management.service.edit_tree.BieEditTreeController;
import org.oagi.score.gateway.http.api.bie_management.service.edit_tree.DefaultBieEditTreeController;
import org.oagi.score.gateway.http.api.cc_management.data.CcState;
import org.oagi.score.gateway.http.api.cc_management.service.ExtensionService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.helper.SrtGuid;
import org.oagi.score.redis.event.EventListenerContainer;
import org.oagi.score.repository.TopLevelAsbiepRepository;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.entity.jooq.Tables.*;

@Service
@Transactional(readOnly = true)
public class BieEditService implements InitializingBean {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TopLevelAsbiepRepository topLevelAsbiepRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private BieRepository bieRepository;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ExtensionService extensionService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisMessageListenerContainer messageListenerContainer;

    @Autowired
    private EventListenerContainer eventListenerContainer;

    private String PURGE_BIE_EVENT_NAME = "purgeBieEvent";

    @Override
    public void afterPropertiesSet() throws Exception {
        eventListenerContainer.addMessageListener(this,
                "onPurgeBieEventReceived",
                new ChannelTopic(PURGE_BIE_EVENT_NAME));
    }

    private BieEditTreeController getTreeController(AuthenticatedPrincipal user, BieEditNode node) {
        return getTreeController(user, node.getTopLevelAsbiepId(), node.isDerived(), node.isLocked());
    }

    private BieEditTreeController getTreeController(AuthenticatedPrincipal user, long topLevelAsbiepId) {
        return getTreeController(user, topLevelAsbiepId, false, false);
    }

    private BieEditTreeController getTreeController(AuthenticatedPrincipal user, long topLevelAsbiepId,
                                                    boolean isDerived, boolean isLocked) {
        DefaultBieEditTreeController bieEditTreeController =
                applicationContext.getBean(DefaultBieEditTreeController.class);

        TopLevelAsbiep topLevelAsbiep = topLevelAsbiepRepository.findById(topLevelAsbiepId);
        bieEditTreeController.initialize(user, topLevelAsbiep);
        if (isDerived || isLocked) {
            bieEditTreeController.setForceBieUpdate(false);
        }

        return bieEditTreeController;
    }

    @Transactional
    public BieEditAbieNode getRootNode(AuthenticatedPrincipal user, long topLevelAsbiepId) {
        BieEditTreeController treeController = getTreeController(user, topLevelAsbiepId);
        return treeController.getRootNode(topLevelAsbiepId);
    }

    @Transactional
    public BccForBie getBcc(AuthenticatedPrincipal user, long bccId) {
        return bieRepository.getBcc(bccId);
    }

    @Transactional
    public List<BieEditNode> getDescendants(AuthenticatedPrincipal user, BieEditNode node, boolean hideUnused) {
        BieEditTreeController treeController = getTreeController(user, node);
        return treeController.getDescendants(user, node, hideUnused).stream()
                .map(e -> {
                    if (node.isDerived() || node.isLocked()) {
                        e.setLocked(true);
                    }
                    return e;
                }).collect(Collectors.toList());
    }

    @Transactional
    public BieEditNodeDetail getDetail(AuthenticatedPrincipal user, BieEditNode node) {
        BieEditTreeController treeController = getTreeController(user, node);
        return treeController.getDetail(node);
    }

    @Transactional
    public void updateState(AuthenticatedPrincipal user, long topLevelAsbiepId, BieState state) {
        BieEditTreeController treeController = getTreeController(user, topLevelAsbiepId);
        treeController.updateState(state);
    }


    @Transactional
    public BieEditUpdateResponse updateDetails(AuthenticatedPrincipal user, BieEditUpdateRequest request) {
        long topLevelAsbiepId = request.getTopLevelAsbiepId();
        BieEditTreeController treeController = getTreeController(user, topLevelAsbiepId);

        BieEditUpdateResponse response = new BieEditUpdateResponse();
        BieEditAbieNodeDetail abieNodeDetail = request.getAbieNodeDetail();
        if (abieNodeDetail != null) {
            response.setAbieNodeResult(treeController.updateDetail(abieNodeDetail));
        }

        for (BieEditAsbiepNodeDetail asbiepNodeDetail : request.getAsbiepNodeDetails()) {
            response.getAsbiepNodeResults().put(asbiepNodeDetail.getGuid(),
                    treeController.updateDetail(asbiepNodeDetail));
        }

        for (BieEditBbiepNodeDetail bbiepNodeDetail : request.getBbiepNodeDetails()) {
            response.getBbiepNodeResults().put(bbiepNodeDetail.getGuid(),
                    treeController.updateDetail(bbiepNodeDetail));
        }

        for (BieEditBbieScNodeDetail bbieScNodeDetail : request.getBbieScNodeDetails()) {
            response.getBbieScNodeResults().put(bbieScNodeDetail.getGuid(),
                    treeController.updateDetail(bbieScNodeDetail));
        }

        this.updateTopLevelAsbiepLastUpdated(user, topLevelAsbiepId);

        return response;
    }

    @Transactional
    public CreateExtensionResponse createLocalAbieExtension(AuthenticatedPrincipal user, BieEditAsbiepNode extension) {
        long asccpId = extension.getAsccpId();
        long releaseId = extension.getReleaseId();
        long roleOfAccId = bieRepository.getRoleOfAccIdByAsccpId(asccpId);

        CreateExtensionResponse response = new CreateExtensionResponse();

        ACC ueAcc = extensionService.getExistsUserExtension(roleOfAccId, releaseId);

        response.setCanEdit(false);
        response.setCanView(false);

        if (ueAcc != null) {
            ACC latestUeAcc = extensionService.getLatestUserExtension(ueAcc.getAccId(), releaseId);
            boolean isSameBetweenRequesterAndOwner = sessionService.userId(user) == latestUeAcc.getOwnerUserId();
            if (ueAcc.getState() == CcState.Published.getValue()) {
                response.setCanEdit(true);
                response.setCanView(true);
            } else if (ueAcc.getState() == CcState.Candidate.getValue()) {
                response.setCanView(true);
            } else {
                if (isSameBetweenRequesterAndOwner) {
                    response.setCanEdit(true);
                    response.setCanView(true);
                }
            }
        } else {
            response.setCanEdit(true);
            response.setCanView(true);
        }

        response.setExtensionId(createAbieExtension(user, roleOfAccId, releaseId));
        return response;
    }

    @Transactional
    public CreateExtensionResponse createGlobalAbieExtension(AuthenticatedPrincipal user, BieEditAsbiepNode extension) {
        long releaseId = extension.getReleaseId();
        long roleOfAccId = dslContext.select(Tables.ACC.ACC_ID)
                .from(Tables.ACC)
                .where(and(
                        Tables.ACC.OBJECT_CLASS_TERM.eq("All Extension"),
                        Tables.ACC.REVISION_NUM.eq(0)))
                .fetchOneInto(Long.class);

        CreateExtensionResponse response = new CreateExtensionResponse();

        ACC ueAcc = extensionService.getExistsUserExtension(roleOfAccId, releaseId);

        response.setCanEdit(false);
        response.setCanView(false);

        if (ueAcc != null) {
            ACC latestUeAcc = extensionService.getLatestUserExtension(ueAcc.getAccId(), releaseId);
            boolean isSameBetweenRequesterAndOwner = sessionService.userId(user) == latestUeAcc.getOwnerUserId();
            if (ueAcc.getState() == CcState.Published.getValue()) {
                response.setCanEdit(true);
                response.setCanView(true);
            } else if (ueAcc.getState() == CcState.Candidate.getValue()) {
                response.setCanView(true);
            } else {
                if (isSameBetweenRequesterAndOwner) {
                    response.setCanEdit(true);
                    response.setCanView(true);
                }
            }
        } else {
            response.setCanEdit(true);
            response.setCanView(true);
        }
        response.setExtensionId(createAbieExtension(user, roleOfAccId, releaseId));
        return response;
    }

    private long createAbieExtension(AuthenticatedPrincipal user, long roleOfAccId, long releaseId) {
        BieEditAcc eAcc = bieRepository.getAccByCurrentAccId(roleOfAccId, releaseId);
        ACC ueAcc = extensionService.getExistsUserExtension(roleOfAccId, releaseId);

        long ueAccId = extensionService.appendUserExtension(eAcc, ueAcc, releaseId, user);
        return ueAccId;
    }

    @Transactional
    public void updateTopLevelAsbiepLastUpdated(AuthenticatedPrincipal user, long topLevelAsbiepId){
        topLevelAsbiepRepository.updateTopLevelAsbiepLastUpdated(sessionService.userId(user), topLevelAsbiepId);
    }

    @Transactional
    public void reuseBIE(AuthenticatedPrincipal user, ReuseBIERequest request) {
        AppUser requester = sessionService.getAppUser(user);

        TopLevelAsbiepRecord topLevelAsbiepRecord = dslContext.selectFrom(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(request.getTopLevelAsbiepId())))
                .fetchOne();

        AppUserRecord bieOwnerRecord = dslContext.selectFrom(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(topLevelAsbiepRecord.getOwnerUserId()))
                .fetchOne();

        if (requester.isDeveloper() && bieOwnerRecord.getIsDeveloper() == 0) {
            throw new IllegalArgumentException("Developer does not allow to reuse end user's BIE.");
        }

        if (topLevelAsbiepRecord.getOwnerUserId().longValue() != requester.getAppUserId()) {
            throw new IllegalArgumentException("Requester is not an owner of the target BIE.");
        }
        if (BieState.valueOf(topLevelAsbiepRecord.getState()) != BieState.Editing) {
            throw new IllegalArgumentException("Target BIE cannot edit.");
        }

        AsbieRecord asbieRecord = dslContext.selectFrom(ASBIE)
                .where(and(
                        ASBIE.ASBIE_ID.eq(ULong.valueOf(request.getAsbieId())),
                        ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepRecord.getTopLevelAsbiepId())
                ))
                .fetchOne();
        ULong prevToAsbiepId = asbieRecord.getToAsbiepId();

        ULong ownerTopLevelAsbiepOfToAsbiep =
                dslContext.select(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID)
                        .from(ASBIEP)
                        .where(ASBIEP.ASBIEP_ID.eq(asbieRecord.getToAsbiepId()))
                        .fetchOneInto(ULong.class);

        boolean isReused = !asbieRecord.getOwnerTopLevelAsbiepId().equals(ownerTopLevelAsbiepOfToAsbiep);
        if (isReused) {
            throw new IllegalArgumentException("Target BIE already has reused BIE.");
        }

        ULong reuseAsbiepId = dslContext.select(TOP_LEVEL_ASBIEP.ASBIEP_ID)
                .from(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(request.getReuseTopLevelAsbiepId())))
                .fetchOneInto(ULong.class);

        asbieRecord.setToAsbiepId(reuseAsbiepId);
        asbieRecord.setLastUpdatedBy(ULong.valueOf(requester.getAppUserId()));
        asbieRecord.setLastUpdateTimestamp(new Timestamp(System.currentTimeMillis()));
        asbieRecord.update(
                ASBIE.TO_ASBIEP_ID,
                ASBIE.LAST_UPDATED_BY,
                ASBIE.LAST_UPDATE_TIMESTAMP);

        // Delete orphan ASBIEP record.
        dslContext.deleteFrom(ASBIEP)
                .where(ASBIEP.ASBIEP_ID.eq(prevToAsbiepId))
                .execute();

        PurgeBieEvent event = new PurgeBieEvent(
                asbieRecord.getOwnerTopLevelAsbiepId().toBigInteger());
        /*
         * Message Publishing
         */
        redisTemplate.convertAndSend(PURGE_BIE_EVENT_NAME, event);
    }

    /**
     * This method is invoked by 'purgeBieEvent' channel subscriber.
     *
     * @param purgeBieEvent
     */
    @Transactional
    public void onPurgeBieEventReceived(PurgeBieEvent purgeBieEvent) {
        ULong topLevelAsbiepId = ULong.valueOf(purgeBieEvent.getTopLevelAsbiepId());

        while (true) {
            List<ULong> unreferencedAbieList = dslContext.select(ABIE.ABIE_ID)
                    .from(ABIE)
                    .leftJoin(ASBIEP).on(and(
                            ABIE.ABIE_ID.eq(ASBIEP.ROLE_OF_ABIE_ID),
                            ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID)
                    ))
                    .leftJoin(TOP_LEVEL_ASBIEP).on(ASBIEP.ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.ASBIEP_ID))
                    .where(and(
                            ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                            TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.isNull(),
                            ASBIEP.ASBIEP_ID.isNull()
                    ))
                    .fetchInto(ULong.class);

            if (unreferencedAbieList.isEmpty()) {
                break;
            }

            List<Record2<ULong, ULong>> unreferencedAsbieList = dslContext.select(ASBIE.ASBIE_ID, ASBIE.TO_ASBIEP_ID)
                    .from(ASBIE)
                    .where(and(
                            ASBIE.FROM_ABIE_ID.in(unreferencedAbieList),
                            ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId)
                    ))
                    .fetch();

            if (!unreferencedAsbieList.isEmpty()) {
                dslContext.deleteFrom(ASBIE)
                        .where(ASBIE.ASBIE_ID.in(
                                unreferencedAsbieList.stream()
                                        .map(e -> e.get(ASBIE.ASBIE_ID)).collect(Collectors.toList())
                        ))
                        .execute();

                dslContext.deleteFrom(ASBIEP)
                        .where(and(
                                ASBIEP.ASBIEP_ID.in(unreferencedAsbieList.stream()
                                        .map(e -> e.get(ASBIE.TO_ASBIEP_ID)).collect(Collectors.toList())),
                                ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId)
                        ))
                        .execute();
            }

            List<Record2<ULong, ULong>> unreferencedBbieList = dslContext.select(BBIE.BBIE_ID, BBIE.TO_BBIEP_ID)
                    .from(BBIE)
                    .where(and(
                            BBIE.FROM_ABIE_ID.in(unreferencedAbieList),
                            BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId)
                    ))
                    .fetch();

            if (!unreferencedBbieList.isEmpty()) {
                dslContext.deleteFrom(BBIE_SC)
                        .where(and(
                                BBIE_SC.BBIE_ID.in(unreferencedBbieList.stream()
                                        .map(e -> e.get(BBIE.BBIE_ID)).collect(Collectors.toList())),
                                BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId)
                        ))
                        .execute();

                dslContext.deleteFrom(BBIE)
                        .where(BBIE.BBIE_ID.in(
                                unreferencedBbieList.stream()
                                        .map(e -> e.get(BBIE.BBIE_ID)).collect(Collectors.toList())
                        ))
                        .execute();

                dslContext.deleteFrom(BBIEP)
                        .where(and(
                                BBIEP.BBIEP_ID.in(unreferencedBbieList.stream()
                                        .map(e -> e.get(BBIE.TO_BBIEP_ID)).collect(Collectors.toList())),
                                BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId)
                        ))
                        .execute();
            }

            dslContext.deleteFrom(ABIE)
                    .where(ABIE.ABIE_ID.in(unreferencedAbieList))
                    .execute();
        }

    }

    @Transactional
    public void removeReusedBIE(AuthenticatedPrincipal user, RemoveReusedBIERequest request) {
        AppUser requester = sessionService.getAppUser(user);
        ULong asbieId = ULong.valueOf(request.getAsbieId());

        Record2<ULong, ULong> res =
                dslContext.select(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID, ASBIE.TO_ASBIEP_ID)
                        .from(ASBIE)
                        .where(ASBIE.ASBIE_ID.eq(asbieId))
                        .fetchOne();

        ULong topLevelAsbiepId = res.get(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID);
        ULong asbiepId = res.get(ASBIE.TO_ASBIEP_ID);

        TopLevelAsbiepRecord topLevelAsbiepRecord = dslContext.selectFrom(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId))
                .fetchOne();

        AppUserRecord bieOwnerRecord = dslContext.selectFrom(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(topLevelAsbiepRecord.getOwnerUserId()))
                .fetchOne();

        if (requester.isDeveloper() && bieOwnerRecord.getIsDeveloper() == 0) {
            throw new IllegalArgumentException("Developer does not allow to remove the end user's reused BIE.");
        }

        if (topLevelAsbiepRecord.getOwnerUserId().longValue() != requester.getAppUserId()) {
            throw new IllegalArgumentException("Requester is not an owner of the target BIE.");
        }
        if (BieState.valueOf(topLevelAsbiepRecord.getState()) != BieState.Editing) {
            throw new IllegalArgumentException("Target BIE cannot edit.");
        }

        Record3<ULong, ULong, ULong> currentAsbiep = dslContext.select(
                ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID, ASBIEP.BASED_ASCCP_ID, ASBIEP.ROLE_OF_ABIE_ID)
                .from(ASBIEP)
                .where(ASBIEP.ASBIEP_ID.eq(asbiepId))
                .fetchOne();

        boolean isReused = !currentAsbiep.get(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID).equals(topLevelAsbiepId);
        if (!isReused) {
            throw new IllegalArgumentException("Target BIE does not have reused BIE.");
        }

        ULong basedAsccpId = currentAsbiep.get(ASBIEP.BASED_ASCCP_ID);
        ULong basedAccId = dslContext.select(ABIE.BASED_ACC_ID)
                .from(ABIE)
                .where(ABIE.ABIE_ID.eq(currentAsbiep.get(ASBIEP.ROLE_OF_ABIE_ID)))
                .fetchOneInto(ULong.class);

        ULong userId = ULong.valueOf(requester.getAppUserId());
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        AbieRecord abieRecord = new AbieRecord();
        abieRecord.setGuid(SrtGuid.randomGuid());
        abieRecord.setBasedAccId(basedAccId);
        abieRecord.setCreatedBy(userId);
        abieRecord.setLastUpdatedBy(userId);
        abieRecord.setState(topLevelAsbiepRecord.getState());
        abieRecord.setCreationTimestamp(timestamp);
        abieRecord.setLastUpdateTimestamp(timestamp);
        abieRecord.setOwnerTopLevelAsbiepId(topLevelAsbiepId);

        abieRecord.setAbieId(
                dslContext.insertInto(ABIE)
                        .set(abieRecord)
                        .returning(ABIE.ABIE_ID).fetchOne().getAbieId()
        );

        AsbiepRecord asbiepRecord = new AsbiepRecord();
        asbiepRecord.setGuid(SrtGuid.randomGuid());
        asbiepRecord.setBasedAsccpId(basedAsccpId);
        asbiepRecord.setRoleOfAbieId(abieRecord.getAbieId());
        asbiepRecord.setCreatedBy(userId);
        asbiepRecord.setLastUpdatedBy(userId);
        asbiepRecord.setCreationTimestamp(timestamp);
        asbiepRecord.setLastUpdateTimestamp(timestamp);
        asbiepRecord.setOwnerTopLevelAsbiepId(topLevelAsbiepId);

        asbiepRecord.setAsbiepId(
                dslContext.insertInto(ASBIEP)
                        .set(asbiepRecord)
                        .returning(ASBIEP.ASBIEP_ID).fetchOne().getAsbiepId()
        );

        dslContext.update(ASBIE)
                .set(ASBIE.TO_ASBIEP_ID, asbiepRecord.getAsbiepId())
                .set(ASBIE.LAST_UPDATED_BY, userId)
                .set(ASBIE.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(ASBIE.ASBIE_ID.eq(asbieId))
                .execute();
    }

}
