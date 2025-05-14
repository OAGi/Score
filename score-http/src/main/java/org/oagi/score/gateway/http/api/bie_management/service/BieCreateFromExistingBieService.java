package org.oagi.score.gateway.http.api.bie_management.service;

import lombok.Data;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.BieCreateRequest;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.CreateBieFromExistingBieRequest;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.CreateInheritedBieRequest;
import org.oagi.score.gateway.http.api.bie_management.model.event.BieCreateFromExistingBieRequestEvent;
import org.oagi.score.gateway.http.api.bie_management.repository.BieCommandRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.BieQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.event.EventListenerContainer;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AppUserRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.TopLevelAsbiepRecord;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.api.bie_management.model.BieState.Initiating;
import static org.oagi.score.gateway.http.common.model.ScoreRole.DEVELOPER;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.ScoreDigestUtils.sha256;

@Service
@Transactional
public class BieCreateFromExistingBieService implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RepositoryFactory repositoryFactory;

    private BieCommandRepository command(ScoreUser requester) {
        return repositoryFactory.bieCommandRepository(requester);
    }

    ;

    private BieQueryRepository query(ScoreUser requester) {
        return repositoryFactory.bieQueryRepository(requester);
    }

    ;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private BieRepository repository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private EventListenerContainer eventListenerContainer;

    @Autowired
    private BieCommandService bieCommandService;

    private final String INTERESTED_EVENT_NAME = "bieCreateFromExistingBieRequestEvent";

    @Override
    public void afterPropertiesSet() throws Exception {
        eventListenerContainer.addMessageListener(this, "onEventReceived",
                new ChannelTopic(INTERESTED_EVENT_NAME));
    }

    @Transactional
    public void createBieFromExistingBie(ScoreUser requester, CreateBieFromExistingBieRequest request) {

        TopLevelAsbiepId topLevelAsbiepId = request.getTopLevelAsbiepId();
        AsbiepId asbiepId = dslContext.select(ASBIE.TO_ASBIEP_ID)
                .from(ASBIE)
                .where(
                        and(ASBIE.HASH_PATH.eq(request.getAsbieHashPath()),
                                ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId.value()))))
                .fetchOneInto(AsbiepId.class);

        TopLevelAsbiepRecord topLevelAsbiepRecord = dslContext.selectFrom(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId.value())))
                .fetchOne();

        AppUserRecord bieOwnerRecord = dslContext.selectFrom(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(topLevelAsbiepRecord.getOwnerUserId()))
                .fetchOne();

        if (requester.hasRole(DEVELOPER) && bieOwnerRecord.getIsDeveloper() == 0) {
            throw new IllegalArgumentException("Developer does not allow to create new BIE along with the end user's BIE.");
        }

        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        TopLevelAsbiepSummaryRecord sourceTopLevelAsbiep = topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);

        if (asbiepId != null) {
            UserId requesterId = requester.userId();
            TopLevelAsbiepId copiedTopLevelAsbiepId =
                    repository.createTopLevelAsbiep(requesterId, sourceTopLevelAsbiep.release().releaseId(), Initiating);
            BieCreateFromExistingBieRequestEvent event = new BieCreateFromExistingBieRequestEvent(
                    topLevelAsbiepId, copiedTopLevelAsbiepId, asbiepId,
                    Collections.emptyList(), requesterId
            );
            /*
             * Message Publishing
             */
            redisTemplate.convertAndSend(INTERESTED_EVENT_NAME, event);
        } else {
            // Create empty BIE.
            if (request.getAsccpManifestId() == null) {
                throw new IllegalArgumentException("Unable to find data to create BIE.");
            }

            List<BusinessContextId> bizCtxIds = dslContext.select(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID)
                    .from(BIZ_CTX_ASSIGNMENT)
                    .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepRecord.getTopLevelAsbiepId()))
                    .fetchStreamInto(BusinessContextId.class).collect(Collectors.toList());

            bieCommandService.createBie(requester, new BieCreateRequest(
                    request.getAsccpManifestId(), bizCtxIds));
        }
    }

    @Transactional
    public void createInheritedBie(ScoreUser requester, CreateInheritedBieRequest request) {

        ULong basedTopLevelAsbiepId = ULong.valueOf(request.getBasedTopLevelAsbiepId());

        TopLevelAsbiepRecord topLevelAsbiepRecord = dslContext.selectFrom(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(basedTopLevelAsbiepId))
                .fetchOne();
        AsbiepId asbiepId = new AsbiepId(topLevelAsbiepRecord.getAsbiepId().toBigInteger());
        TopLevelAsbiepId copiedTopLevelAsbiepId =
                repository.createTopLevelAsbiep(requester.userId(),
                        new ReleaseId(topLevelAsbiepRecord.getReleaseId().toBigInteger()),
                        new TopLevelAsbiepId(topLevelAsbiepRecord.getTopLevelAsbiepId().toBigInteger()), Initiating,
                        topLevelAsbiepRecord.getVersion(), topLevelAsbiepRecord.getStatus());
        BieCreateFromExistingBieRequestEvent event = new BieCreateFromExistingBieRequestEvent(
                new TopLevelAsbiepId(topLevelAsbiepRecord.getTopLevelAsbiepId().toBigInteger()), copiedTopLevelAsbiepId, asbiepId,
                Collections.emptyList(), requester.userId()
        );

        /*
         * Message Publishing
         */
        redisTemplate.convertAndSend(INTERESTED_EVENT_NAME, event);
    }


    /**
     * This method is invoked by 'bieCreateFromExistingBieRequestEvent' channel subscriber.
     *
     * @param event
     */
    @Transactional
    public void onEventReceived(BieCreateFromExistingBieRequestEvent event) {
        RLock lock = redissonClient.getLock("BieCreateFromExistingBieRequestEvent:" + event.hashCode());
        if (!lock.tryLock()) {
            return;
        }
        try {
            logger.debug("Received BieCreateFromExistingBieRequestEvent: " + event);

            ScoreUser requester = sessionService.getScoreUserByUserId(event.getUserId());
            BieCreateFromExistingBieContext context = new BieCreateFromExistingBieContext(requester, event);
            context.execute();
        } finally {
            lock.unlock();
        }
    }

    private List<BieCreateFromExistingBieAbie> getAbieByOwnerTopLevelAsbiepId(TopLevelAsbiepId ownerTopLevelAsbiepId) {
        return dslContext.select(
                        ABIE.ABIE_ID,
                        ABIE.GUID,
                        ABIE.PATH,
                        ABIE.BASED_ACC_MANIFEST_ID
                ).from(ABIE)
                .where(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId.value())))
                .fetchInto(BieCreateFromExistingBieAbie.class);
    }

    private List<BieCreateFromExistingBieAsbie> getAsbieByOwnerTopLevelAsbiepId(TopLevelAsbiepId ownerTopLevelAsbiepId) {
        return dslContext.select(
                        ASBIE.ASBIE_ID,
                        ASBIE.GUID,
                        ASBIE.PATH,
                        ASBIE.FROM_ABIE_ID,
                        ASBIE.TO_ASBIEP_ID,
                        ASBIE.BASED_ASCC_MANIFEST_ID,
                        ASBIE.DEFINITION,
                        ASBIE.CARDINALITY_MIN,
                        ASBIE.CARDINALITY_MAX,
                        ASBIE.IS_NILLABLE.as("nillable"),
                        ASBIE.REMARK,
                        ASBIE.SEQ_KEY,
                        ASBIE.IS_USED.as("used")).from(ASBIE)
                .where(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId.value())))
                .fetchInto(BieCreateFromExistingBieAsbie.class);
    }

    private List<BieCreateFromExistingBieBbie> getBbieByOwnerTopLevelAsbiepId(TopLevelAsbiepId ownerTopLevelAsbiepId) {
        return dslContext.select(
                        BBIE.BBIE_ID,
                        BBIE.GUID,
                        BBIE.PATH,
                        BBIE.BASED_BCC_MANIFEST_ID,
                        BBIE.FROM_ABIE_ID,
                        BBIE.TO_BBIEP_ID,
                        BBIE.XBT_MANIFEST_ID,
                        BBIE.CODE_LIST_MANIFEST_ID,
                        BBIE.AGENCY_ID_LIST_MANIFEST_ID,
                        BBIE.CARDINALITY_MIN,
                        BBIE.CARDINALITY_MAX,
                        BBIE.DEFAULT_VALUE,
                        BBIE.IS_NILLABLE.as("nillable"),
                        BBIE.FIXED_VALUE,
                        BBIE.IS_NULL.as("nill"),
                        BBIE.DEFINITION,
                        BBIE.EXAMPLE,
                        BBIE.REMARK,
                        BBIE.SEQ_KEY,
                        BBIE.IS_USED.as("used")).from(BBIE)
                .where(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId.value())))
                .fetchInto(BieCreateFromExistingBieBbie.class);
    }

    private List<BieCreateFromExistingBieAsbiep> getAsbiepByOwnerTopLevelAsbiepId(TopLevelAsbiepId ownerTopLevelAsbiepId) {
        return dslContext.select(
                        ASBIEP.ASBIEP_ID,
                        ASBIEP.GUID,
                        ASBIEP.PATH,
                        ASBIEP.BASED_ASCCP_MANIFEST_ID,
                        ASBIEP.ROLE_OF_ABIE_ID,
                        ASBIEP.DEFINITION,
                        ASBIEP.REMARK,
                        ASBIEP.BIZ_TERM).from(ASBIEP)
                .where(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId.value())))
                .fetchInto(BieCreateFromExistingBieAsbiep.class);
    }

    private List<BieCreateFromExistingBieBbiep> getBbiepByOwnerTopLevelAsbiepId(TopLevelAsbiepId ownerTopLevelAsbiepId) {
        return dslContext.select(
                        BBIEP.BBIEP_ID,
                        BBIEP.GUID,
                        BBIEP.PATH,
                        BBIEP.BASED_BCCP_MANIFEST_ID,
                        BBIEP.DEFINITION,
                        BBIEP.REMARK,
                        BBIEP.BIZ_TERM).from(BBIEP)
                .where(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId.value())))
                .fetchInto(BieCreateFromExistingBieBbiep.class);
    }

    private List<BieCreateFromExistingBieBbieSc> getBbieScByOwnerTopLevelAsbiepId(TopLevelAsbiepId ownerTopLevelAsbiepId) {
        return dslContext.select(
                        BBIE_SC.BBIE_SC_ID,
                        BBIE_SC.GUID,
                        BBIE_SC.PATH,
                        BBIE_SC.BBIE_ID,
                        BBIE_SC.BASED_DT_SC_MANIFEST_ID,
                        BBIE_SC.XBT_MANIFEST_ID,
                        BBIE_SC.CODE_LIST_MANIFEST_ID,
                        BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID,
                        BBIE_SC.CARDINALITY_MIN,
                        BBIE_SC.CARDINALITY_MAX,
                        BBIE_SC.DEFAULT_VALUE,
                        BBIE_SC.FIXED_VALUE,
                        BBIE_SC.DEFINITION,
                        BBIE_SC.EXAMPLE,
                        BBIE_SC.REMARK,
                        BBIE_SC.BIZ_TERM,
                        BBIE_SC.IS_USED.as("used")).from(BBIE_SC)
                .where(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId.value())))
                .fetchInto(BieCreateFromExistingBieBbieSc.class);
    }

    @Data
    public static class BieCreateFromExistingBieAbie {

        private AbieId abieId;
        private String guid;
        private String path;
        private AccManifestId basedAccManifestId;

    }

    @Data
    public static class BieCreateFromExistingBieAsbie {

        private AsbieId asbieId;
        private Guid guid;
        private String path;
        private AbieId fromAbieId;
        private AsbiepId toAsbiepId;
        private AsccManifestId basedAsccManifestId;
        private String definition;
        private int cardinalityMin;
        private int cardinalityMax;
        private boolean nillable;
        private String remark;
        private double seqKey;
        private boolean used;

    }

    @Data
    public static class BieCreateFromExistingBieBbie {

        private BbieId bbieId;
        private Guid guid;
        private String path;
        private BccManifestId basedBccManifestId;
        private AbieId fromAbieId;
        private BbiepId toBbiepId;
        private XbtManifestId xbtManifestId;
        private CodeListManifestId codeListManifestId;
        private AgencyIdListManifestId agencyIdListManifestId;
        private int cardinalityMin;
        private int cardinalityMax;
        private String defaultValue;
        private boolean nillable;
        private String fixedValue;
        private boolean nill;
        private String definition;
        private String example;
        private String remark;
        private double seqKey;
        private boolean used;

    }

    @Data
    public static class BieCreateFromExistingBieAsbiep {

        private AsbiepId asbiepId;
        private Guid guid;
        private String path;
        private AsccpManifestId basedAsccpManifestId;
        private AbieId roleOfAbieId;
        private String definition;
        private String remark;
        private String bizTerm;

    }

    @Data
    public static class BieCreateFromExistingBieBbiep {

        private BbiepId bbiepId;
        private Guid guid;
        private String path;
        private BccpManifestId basedBccpManifestId;
        private String definition;
        private String remark;
        private String bizTerm;

    }

    @Data
    public static class BieCreateFromExistingBieBbieSc {

        private BbieScId bbieScId;
        private String guid;
        private String path;
        private BbieId bbieId;
        private DtScManifestId basedDtScManifestId;
        private XbtManifestId xbtManifestId;
        private CodeListManifestId codeListManifestId;
        private AgencyIdListManifestId agencyIdListManifestId;
        private int cardinalityMin;
        private int cardinalityMax;
        private String defaultValue;
        private String fixedValue;
        private String definition;
        private String example;
        private String remark;
        private String bizTerm;
        private boolean used;

    }

    private class BieCreateFromExistingBieContext {

        private final TopLevelAsbiepSummaryRecord sourceTopLevelAsbiep;
        private TopLevelAsbiepId targetTopLevelAsbiepId;
        private AsbiepId targetAsbiepId;
        private List<BusinessContextId> bizCtxIds;
        private final UserId userId;
        private final String sourceAsccpKey;

        private LocalDateTime timestamp;


        private List<BieCreateFromExistingBieAbie> abieList;

        private List<BieCreateFromExistingBieAsbiep> asbiepList;
        private final Map<AbieId, List<BieCreateFromExistingBieAsbiep>> roleOfAbieToAsbiepMap;

        private List<BieCreateFromExistingBieBbiep> bbiepList;

        private List<BieCreateFromExistingBieAsbie> asbieList;
        private final Map<AbieId, List<BieCreateFromExistingBieAsbie>> fromAbieToAsbieMap;
        private final Map<AsbiepId, List<BieCreateFromExistingBieAsbie>> toAsbiepToAsbieMap;

        private List<BieCreateFromExistingBieBbie> bbieList;
        private final Map<AbieId, List<BieCreateFromExistingBieBbie>> fromAbieToBbieMap;
        private final Map<BbiepId, List<BieCreateFromExistingBieBbie>> toBbiepToBbieMap;

        private List<BieCreateFromExistingBieBbieSc> bbieScList;
        private final Map<BbieId, List<BieCreateFromExistingBieBbieSc>> bbieToBbieScMap;

        public BieCreateFromExistingBieContext(ScoreUser requester, BieCreateFromExistingBieRequestEvent event) {

            var query = query(requester);

            sourceAsccpKey = CcType.ASCCP.name() + "-" + dslContext.select(ASBIEP.BASED_ASCCP_MANIFEST_ID)
                    .from(ASBIEP)
                    .where(ASBIEP.ASBIEP_ID.eq(ULong.valueOf(event.getAsbiepId().value())))
                    .fetchOneInto(ULong.class).toBigInteger().toString();
            TopLevelAsbiepId sourceTopLevelAsbiepId = event.getSourceTopLevelAsbiepId();
            var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
            sourceTopLevelAsbiep = topLevelAsbiepQuery.getTopLevelAsbiepSummary(sourceTopLevelAsbiepId);

            targetTopLevelAsbiepId = event.getTargetTopLevelAsbiepId();

            bizCtxIds = event.getBizCtxIds();
            if (bizCtxIds == null || bizCtxIds.isEmpty()) {
                bizCtxIds = dslContext.select(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID)
                        .from(BIZ_CTX_ASSIGNMENT)
                        .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(sourceTopLevelAsbiepId.value())))
                        .fetchInto(BusinessContextId.class);
            }
            userId = event.getUserId();

            abieList = getAbieByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);

            asbiepList = getAsbiepByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);
            roleOfAbieToAsbiepMap = asbiepList.stream().collect(groupingBy(BieCreateFromExistingBieAsbiep::getRoleOfAbieId));

            asbieList = getAsbieByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);
            fromAbieToAsbieMap = asbieList.stream().collect(groupingBy(BieCreateFromExistingBieAsbie::getFromAbieId));
            toAsbiepToAsbieMap = asbieList.stream().collect(groupingBy(BieCreateFromExistingBieAsbie::getToAsbiepId));

            bbiepList = getBbiepByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);

            bbieList = getBbieByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);
            fromAbieToBbieMap = bbieList.stream().collect(groupingBy(BieCreateFromExistingBieBbie::getFromAbieId));
            toBbiepToBbieMap = bbieList.stream().collect(groupingBy(BieCreateFromExistingBieBbie::getToBbiepId));

            bbieScList = getBbieScByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);
            bbieToBbieScMap = bbieScList.stream().collect(groupingBy(BieCreateFromExistingBieBbieSc::getBbieId));

            arrange(event);
        }

        public void execute() {
            timestamp = LocalDateTime.now();
            logger.debug("Begin creating BIE from existing BIE with the source " + sourceTopLevelAsbiep.topLevelAsbiepId() +
                    " and the target " + targetTopLevelAsbiepId);

            repository.createBizCtxAssignments(
                    targetTopLevelAsbiepId,
                    bizCtxIds);

            for (BieCreateFromExistingBieAbie abie : abieList) {
                AbieId previousAbieId = abie.getAbieId();
                AbieId nextAbieId = insertAbie(abie);

                fireChangeEvent(previousAbieId, nextAbieId);
            }

            for (BieCreateFromExistingBieAsbiep asbiep : asbiepList) {
                AsbiepId previousAsbiepId = asbiep.getAsbiepId();
                AsbiepId nextAsbiepId = insertAsbiep(asbiep);

                fireChangeEvent(previousAsbiepId, nextAsbiepId);
            }

            repository.updateAsbiepIdOnTopLevelAsbiep(
                    targetAsbiepId,
                    targetTopLevelAsbiepId);

            for (BieCreateFromExistingBieBbiep bbiep : bbiepList) {
                BbiepId previousBbiepId = bbiep.getBbiepId();
                BbiepId nextBbiepId = insertBbiep(bbiep);

                fireChangeEvent(previousBbiepId, nextBbiepId);
            }

            for (BieCreateFromExistingBieAsbie asbie : asbieList) {
                AsbieId previousAsbieId = asbie.getAsbieId();
                AsbieId nextAsbieId = insertAsbie(asbie);

                fireChangeEvent(previousAsbieId, nextAsbieId);
            }

            for (BieCreateFromExistingBieBbie bbie : bbieList) {
                BbieId previousBbieId = bbie.getBbieId();
                BbieId nextBbieId = insertBbie(bbie);

                fireChangeEvent(previousBbieId, nextBbieId);
            }

            for (BieCreateFromExistingBieBbieSc bbieSc : bbieScList) {
                BbieScId previousBbieScId = bbieSc.getBbieScId();
                BbieScId nextBbieScId = insertBbieSc(bbieSc);

                fireChangeEvent(previousBbieScId, nextBbieScId);
            }

            repository.updateState(targetTopLevelAsbiepId, BieState.WIP);

            logger.debug("End create BIE from " + sourceTopLevelAsbiep.topLevelAsbiepId() +
                    " to " + targetTopLevelAsbiepId);
        }

        private void arrange(BieCreateFromExistingBieRequestEvent event) {
            List<BieCreateFromExistingBieAbie> abieList = new ArrayList();
            List<BieCreateFromExistingBieAsbiep> asbiepList = new ArrayList();
            List<BieCreateFromExistingBieBbiep> bbiepList = new ArrayList();
            List<BieCreateFromExistingBieAsbie> asbieList = new ArrayList();
            List<BieCreateFromExistingBieBbie> bbieList = new ArrayList();
            List<BieCreateFromExistingBieBbieSc> bbieScList = new ArrayList();

            Map<AsbiepId, BieCreateFromExistingBieAsbiep> asbiepMap = this.asbiepList.stream()
                    .collect(Collectors.toMap(BieCreateFromExistingBieAsbiep::getAsbiepId, Function.identity()));

            Map<AbieId, BieCreateFromExistingBieAbie> abieMap = this.abieList.stream()
                    .collect(Collectors.toMap(BieCreateFromExistingBieAbie::getAbieId, Function.identity()));

            Map<BbiepId, BieCreateFromExistingBieBbiep> bbiepMap = this.bbiepList.stream()
                    .collect(Collectors.toMap(BieCreateFromExistingBieBbiep::getBbiepId, Function.identity()));

            Queue<BieCreateFromExistingBieAsbiep> asbiepQueue = new LinkedList<>();
            BieCreateFromExistingBieAsbiep topLevelAsbiep = asbiepMap.get(event.getAsbiepId());
            asbiepQueue.add(topLevelAsbiep);

            while (!asbiepQueue.isEmpty()) {
                BieCreateFromExistingBieAsbiep currentAsbiep = asbiepQueue.poll();
                asbiepList.add(currentAsbiep);
                BieCreateFromExistingBieAbie roleOfAbie = abieMap.get(currentAsbiep.getRoleOfAbieId());
                abieList.add(roleOfAbie);

                List<BieCreateFromExistingBieAsbie> asbies = this.asbieList.stream()
                        .filter(e -> e.getFromAbieId().equals(roleOfAbie.getAbieId())).collect(Collectors.toList());

                asbieList.addAll(asbies);
                asbies.forEach(e -> {
                    if (asbiepMap.get(e.getToAsbiepId()) != null) {
                        asbiepQueue.add(asbiepMap.get(e.getToAsbiepId()));
                    }
                });

                List<BieCreateFromExistingBieBbie> bbies = this.bbieList.stream()
                        .filter(e -> e.getFromAbieId().equals(roleOfAbie.getAbieId())).collect(Collectors.toList());
                bbieList.addAll(bbies);
                bbies.forEach(e -> {
                    bbiepList.add(bbiepMap.get(e.getToBbiepId()));
                    bbieScList.addAll(this.bbieScList.stream()
                            .filter(sc -> sc.getBbieId().equals(e.getBbieId()))
                            .collect(Collectors.toList()));
                });
            }

            this.abieList = abieList;
            this.asbieList = asbieList;
            this.bbieList = bbieList;
            this.asbiepList = asbiepList;
            this.bbiepList = bbiepList;
            this.bbieScList = bbieScList;

            this.targetAsbiepId = topLevelAsbiep.getAsbiepId();
        }

        private void fireChangeEvent(AbieId previousVal, AbieId nextVal) {
            roleOfAbieToAsbiepMap.getOrDefault(previousVal, Collections.emptyList()).stream().forEach(asbiep -> {
                asbiep.setRoleOfAbieId(nextVal);
            });
            fromAbieToAsbieMap.getOrDefault(previousVal, Collections.emptyList()).stream().forEach(asbie -> {
                asbie.setFromAbieId(nextVal);
            });
            fromAbieToBbieMap.getOrDefault(previousVal, Collections.emptyList()).stream().forEach(bbie -> {
                bbie.setFromAbieId(nextVal);
            });
        }

        private void fireChangeEvent(AsbiepId previousVal, AsbiepId nextVal) {
            if (previousVal.equals(targetAsbiepId)) {
                targetAsbiepId = nextVal;
            }

            toAsbiepToAsbieMap.getOrDefault(previousVal, Collections.emptyList()).stream().forEach(asbie -> {
                asbie.setToAsbiepId(nextVal);
            });
        }

        private void fireChangeEvent(BbiepId previousVal, BbiepId nextVal) {
            toBbiepToBbieMap.getOrDefault(previousVal, Collections.emptyList()).stream().forEach(bbie -> {
                bbie.setToBbiepId(nextVal);
            });
        }

        private void fireChangeEvent(AsbieId previousVal, AsbieId nextVal) {
        }

        private void fireChangeEvent(BbieId previousVal, BbieId nextVal) {
            bbieToBbieScMap.getOrDefault(previousVal, Collections.emptyList()).stream().forEach(bbieSc -> {
                bbieSc.setBbieId(nextVal);
            });
        }

        private void fireChangeEvent(BbieScId previousVal, BbieScId nextVal) {
        }

        private AbieId insertAbie(BieCreateFromExistingBieAbie abie) {

            return new AbieId(
                    dslContext.insertInto(ABIE)
                            .set(ABIE.GUID, ScoreGuidUtils.randomGuid())
                            .set(ABIE.PATH, getPath(abie.getPath()))
                            .set(ABIE.HASH_PATH, getHashPath(abie.getPath()))
                            .set(ABIE.BASED_ACC_MANIFEST_ID, ULong.valueOf(abie.getBasedAccManifestId().value()))
                            .set(ABIE.CREATED_BY, ULong.valueOf(userId.value()))
                            .set(ABIE.LAST_UPDATED_BY, ULong.valueOf(userId.value()))
                            .set(ABIE.CREATION_TIMESTAMP, timestamp)
                            .set(ABIE.LAST_UPDATE_TIMESTAMP, timestamp)
                            // .set(ABIE.STATE, BieState.Initiating.getValue())
                            .set(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(targetTopLevelAsbiepId.value()))
                            .returning(ABIE.ABIE_ID).fetchOne().getValue(ABIE.ABIE_ID).toBigInteger()
            );
        }

        private AsbiepId insertAsbiep(BieCreateFromExistingBieAsbiep asbiep) {

            return new AsbiepId(
                    dslContext.insertInto(ASBIEP)
                            .set(ASBIEP.GUID, ScoreGuidUtils.randomGuid())
                            .set(ASBIEP.PATH, getPath(asbiep.getPath()))
                            .set(ASBIEP.HASH_PATH, getHashPath(asbiep.getPath()))
                            .set(ASBIEP.BASED_ASCCP_MANIFEST_ID, ULong.valueOf(asbiep.getBasedAsccpManifestId().value()))
                            .set(ASBIEP.ROLE_OF_ABIE_ID, ULong.valueOf(asbiep.getRoleOfAbieId().value()))
                            .set(ASBIEP.DEFINITION, asbiep.getDefinition())
                            .set(ASBIEP.REMARK, asbiep.getRemark())
                            .set(ASBIEP.BIZ_TERM, asbiep.getBizTerm())
                            .set(ASBIEP.CREATED_BY, ULong.valueOf(userId.value()))
                            .set(ASBIEP.LAST_UPDATED_BY, ULong.valueOf(userId.value()))
                            .set(ASBIEP.CREATION_TIMESTAMP, timestamp)
                            .set(ASBIEP.LAST_UPDATE_TIMESTAMP, timestamp)
                            .set(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(targetTopLevelAsbiepId.value()))
                            .returning(ASBIEP.ASBIEP_ID).fetchOne().getValue(ASBIEP.ASBIEP_ID).toBigInteger()
            );
        }

        private BbiepId insertBbiep(BieCreateFromExistingBieBbiep bbiep) {

            return new BbiepId(
                    dslContext.insertInto(BBIEP)
                            .set(BBIEP.GUID, ScoreGuidUtils.randomGuid())
                            .set(BBIEP.PATH, getPath(bbiep.getPath()))
                            .set(BBIEP.HASH_PATH, getHashPath(bbiep.getPath()))
                            .set(BBIEP.BASED_BCCP_MANIFEST_ID, ULong.valueOf(bbiep.getBasedBccpManifestId().value()))
                            .set(BBIEP.DEFINITION, bbiep.getDefinition())
                            .set(BBIEP.REMARK, bbiep.getRemark())
                            .set(BBIEP.BIZ_TERM, bbiep.getBizTerm())
                            .set(BBIEP.CREATED_BY, ULong.valueOf(userId.value()))
                            .set(BBIEP.LAST_UPDATED_BY, ULong.valueOf(userId.value()))
                            .set(BBIEP.CREATION_TIMESTAMP, timestamp)
                            .set(BBIEP.LAST_UPDATE_TIMESTAMP, timestamp)
                            .set(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(targetTopLevelAsbiepId.value()))
                            .returning(BBIEP.BBIEP_ID).fetchOne().getValue(BBIEP.BBIEP_ID).toBigInteger()
            );
        }

        private AsbieId insertAsbie(BieCreateFromExistingBieAsbie asbie) {

            return new AsbieId(
                    dslContext.insertInto(ASBIE)
                            .set(ASBIE.GUID, ScoreGuidUtils.randomGuid())
                            .set(ASBIE.PATH, getPath(asbie.getPath()))
                            .set(ASBIE.HASH_PATH, getHashPath(asbie.getPath()))
                            .set(ASBIE.FROM_ABIE_ID, ULong.valueOf(asbie.getFromAbieId().value()))
                            .set(ASBIE.TO_ASBIEP_ID, ULong.valueOf(asbie.getToAsbiepId().value()))
                            .set(ASBIE.BASED_ASCC_MANIFEST_ID, ULong.valueOf(asbie.getBasedAsccManifestId().value()))
                            .set(ASBIE.DEFINITION, asbie.getDefinition())
                            .set(ASBIE.REMARK, asbie.getRemark())
                            .set(ASBIE.CARDINALITY_MIN, asbie.getCardinalityMin())
                            .set(ASBIE.CARDINALITY_MAX, asbie.getCardinalityMax())
                            .set(ASBIE.IS_NILLABLE, (byte) ((asbie.isNillable()) ? 1 : 0))
                            .set(ASBIE.IS_USED, (byte) ((asbie.isUsed()) ? 1 : 0))
                            .set(ASBIE.SEQ_KEY, BigDecimal.valueOf(asbie.getSeqKey()))
                            .set(ASBIE.CREATED_BY, ULong.valueOf(userId.value()))
                            .set(ASBIE.LAST_UPDATED_BY, ULong.valueOf(userId.value()))
                            .set(ASBIE.CREATION_TIMESTAMP, timestamp)
                            .set(ASBIE.LAST_UPDATE_TIMESTAMP, timestamp)
                            .set(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(targetTopLevelAsbiepId.value()))
                            .returning(ASBIE.ASBIE_ID).fetchOne().getValue(ASBIE.ASBIE_ID).toBigInteger()
            );
        }

        private BbieId insertBbie(BieCreateFromExistingBieBbie bbie) {

            return new BbieId(
                    dslContext.insertInto(BBIE)
                            .set(BBIE.GUID, ScoreGuidUtils.randomGuid())
                            .set(BBIE.PATH, getPath(bbie.getPath()))
                            .set(BBIE.HASH_PATH, getHashPath(bbie.getPath()))
                            .set(BBIE.FROM_ABIE_ID, ULong.valueOf(bbie.getFromAbieId().value()))
                            .set(BBIE.TO_BBIEP_ID, ULong.valueOf(bbie.getToBbiepId().value()))
                            .set(BBIE.BASED_BCC_MANIFEST_ID, ULong.valueOf(bbie.getBasedBccManifestId().value()))
                            .set(BBIE.XBT_MANIFEST_ID, (bbie.getXbtManifestId() != null) ? ULong.valueOf(bbie.getXbtManifestId().value()) : null)
                            .set(BBIE.CODE_LIST_MANIFEST_ID, (bbie.getCodeListManifestId() != null) ? ULong.valueOf(bbie.getCodeListManifestId().value()) : null)
                            .set(BBIE.AGENCY_ID_LIST_MANIFEST_ID, (bbie.getAgencyIdListManifestId() != null) ? ULong.valueOf(bbie.getAgencyIdListManifestId().value()) : null)
                            .set(BBIE.DEFAULT_VALUE, bbie.getDefaultValue())
                            .set(BBIE.FIXED_VALUE, bbie.getFixedValue())
                            .set(BBIE.DEFINITION, bbie.getDefinition())
                            .set(BBIE.EXAMPLE, bbie.getExample())
                            .set(BBIE.REMARK, bbie.getRemark())
                            .set(BBIE.CARDINALITY_MIN, bbie.getCardinalityMin())
                            .set(BBIE.CARDINALITY_MAX, bbie.getCardinalityMax())
                            .set(BBIE.IS_NILLABLE, (byte) ((bbie.isNillable()) ? 1 : 0))
                            .set(BBIE.IS_NULL, (byte) ((bbie.isNill()) ? 1 : 0))
                            .set(BBIE.SEQ_KEY, BigDecimal.valueOf(bbie.getSeqKey()))
                            .set(BBIE.IS_USED, (byte) ((bbie.isUsed()) ? 1 : 0))
                            .set(BBIE.CREATED_BY, ULong.valueOf(userId.value()))
                            .set(BBIE.LAST_UPDATED_BY, ULong.valueOf(userId.value()))
                            .set(BBIE.CREATION_TIMESTAMP, timestamp)
                            .set(BBIE.LAST_UPDATE_TIMESTAMP, timestamp)
                            .set(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(targetTopLevelAsbiepId.value()))
                            .returning(BBIE.BBIE_ID).fetchOne().getValue(BBIE.BBIE_ID).toBigInteger()
            );
        }

        private BbieScId insertBbieSc(BieCreateFromExistingBieBbieSc bbieSc) {

            return new BbieScId(
                    dslContext.insertInto(BBIE_SC)
                            .set(BBIE_SC.GUID, ScoreGuidUtils.randomGuid())
                            .set(BBIE_SC.PATH, getPath(bbieSc.getPath()))
                            .set(BBIE_SC.HASH_PATH, getHashPath(bbieSc.getPath()))
                            .set(BBIE_SC.BBIE_ID, ULong.valueOf(bbieSc.getBbieId().value()))
                            .set(BBIE_SC.BASED_DT_SC_MANIFEST_ID, ULong.valueOf(bbieSc.getBasedDtScManifestId().value()))
                            .set(BBIE_SC.XBT_MANIFEST_ID, (bbieSc.getXbtManifestId() != null) ? ULong.valueOf(bbieSc.getXbtManifestId().value()) : null)
                            .set(BBIE_SC.CODE_LIST_MANIFEST_ID, (bbieSc.getCodeListManifestId() != null) ? ULong.valueOf(bbieSc.getCodeListManifestId().value()) : null)
                            .set(BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID, (bbieSc.getAgencyIdListManifestId() != null) ? ULong.valueOf(bbieSc.getAgencyIdListManifestId().value()) : null)
                            .set(BBIE_SC.DEFAULT_VALUE, bbieSc.getDefaultValue())
                            .set(BBIE_SC.FIXED_VALUE, bbieSc.getFixedValue())
                            .set(BBIE_SC.DEFINITION, bbieSc.getDefinition())
                            .set(BBIE_SC.EXAMPLE, bbieSc.getExample())
                            .set(BBIE_SC.REMARK, bbieSc.getRemark())
                            .set(BBIE_SC.BIZ_TERM, bbieSc.getBizTerm())
                            .set(BBIE_SC.CARDINALITY_MIN, bbieSc.getCardinalityMin())
                            .set(BBIE_SC.CARDINALITY_MAX, bbieSc.getCardinalityMax())
                            .set(BBIE_SC.IS_USED, (byte) ((bbieSc.isUsed()) ? 1 : 0))
                            .set(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(targetTopLevelAsbiepId.value()))
                            .set(BBIE.CREATED_BY, ULong.valueOf(userId.value()))
                            .set(BBIE.LAST_UPDATED_BY, ULong.valueOf(userId.value()))
                            .set(BBIE.CREATION_TIMESTAMP, timestamp)
                            .set(BBIE.LAST_UPDATE_TIMESTAMP, timestamp)
                            .returning(BBIE_SC.BBIE_SC_ID).fetchOne().getValue(BBIE_SC.BBIE_SC_ID).toBigInteger()
            );
        }

        private String getPath(String path) {
            String seperator = sourceAsccpKey + ">";
            String[] tokens = path.split(sourceAsccpKey);
            if (tokens.length < 2) {
                return sourceAsccpKey;
            } else {
                if (path.endsWith(sourceAsccpKey)) {
                    return sourceAsccpKey;
                }
                return seperator + path.split(seperator)[1];
            }
        }

        private String getHashPath(String path) {
            return sha256(getPath(path));
        }
    }

}