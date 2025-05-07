package org.oagi.score.gateway.http.api.bie_management.service;

import lombok.Data;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.event.BieCopyRequestEvent;
import org.oagi.score.gateway.http.api.bie_management.repository.BusinessInformationEntityRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.InsertBizCtxAssignmentArguments;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.InsertTopLevelAsbiepArguments;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.event.EventListenerContainer;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
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
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.api.bie_management.model.BieState.Initiating;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

@Service
@Transactional
public class BieCopyService implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private BieRepository repository;

    @Autowired
    private BusinessInformationEntityRepository bieRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisMessageListenerContainer messageListenerContainer;

    @Autowired
    private EventListenerContainer eventListenerContainer;

    private final String INTERESTED_EVENT_NAME = "bieCopyRequestEvent";

    @Override
    public void afterPropertiesSet() throws Exception {
        eventListenerContainer.addMessageListener(this, "onEventReceived",
                new ChannelTopic(INTERESTED_EVENT_NAME));
    }

    @Transactional
    public void copyBie(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId, List<BusinessContextId> businessContextIdList) {
        TopLevelAsbiepId sourceTopLevelAsbiepId = topLevelAsbiepId;
        if (sourceTopLevelAsbiepId == null) {
            throw new IllegalArgumentException("`topLevelAsbiepId` parameter must not be null.");
        }

        if (businessContextIdList == null || businessContextIdList.isEmpty()) {
            throw new IllegalArgumentException("`bizCtxIds` parameter must not be null.");
        }

        UserId userId = requester.userId();
        if (userId == null) {
            throw new IllegalArgumentException("`userId` parameter must not be null.");
        }

        long millis = System.currentTimeMillis();

        var query = repositoryFactory.topLevelAsbiepQueryRepository(requester);

        TopLevelAsbiepSummaryRecord sourceTopLevelAsbiep = query.getTopLevelAsbiepSummary(sourceTopLevelAsbiepId);
        TopLevelAsbiepId copiedTopLevelAsbiepId = new InsertTopLevelAsbiepArguments(
                repositoryFactory.topLevelAsbiepCommandRepository(requester))
                .setReleaseId(sourceTopLevelAsbiep.release().releaseId())
                .setBieState(Initiating)
                .setVersion(sourceTopLevelAsbiep.version())
                .setStatus(sourceTopLevelAsbiep.status())
                .setInverseMode(sourceTopLevelAsbiep.inverseMode())
                .setUserId(userId)
                .setTimestamp(millis)
                .setBasedTopLevelAsbiepId(sourceTopLevelAsbiep.basedTopLevelAsbiepId())
                .setSource(sourceTopLevelAsbiepId, "Copy")
                .execute();

        BieCopyRequestEvent bieCopyRequestEvent = new BieCopyRequestEvent(
                userId, sourceTopLevelAsbiepId, copiedTopLevelAsbiepId, businessContextIdList
        );

        /*
         * Message Publishing
         */
        redisTemplate.convertAndSend(INTERESTED_EVENT_NAME, bieCopyRequestEvent);
    }

    /**
     * This method is invoked by 'bieCopyRequestEvent' channel subscriber.
     *
     * @param bieCopyRequestEvent
     */
    @Transactional
    public void onEventReceived(BieCopyRequestEvent bieCopyRequestEvent) {
        RLock lock = redissonClient.getLock("BieCopyRequestEvent: " + bieCopyRequestEvent.hashCode());
        if (!lock.tryLock()) {
            return;
        }
        try {
            logger.debug("Received BieCopyRequestEvent: " + bieCopyRequestEvent);

            ScoreUser requester = sessionService.getScoreUserByUserId(bieCopyRequestEvent.getUserId());
            BieCopyContext copyContext = new BieCopyContext(requester, bieCopyRequestEvent);
            copyContext.execute();
        } finally {
            lock.unlock();
        }
    }

    private List<BieCopyAbie> getAbieByOwnerTopLevelAsbiepId(TopLevelAsbiepId ownerTopLevelAsbiepId) {
        return dslContext.select(
                        ABIE.ABIE_ID,
                        ABIE.GUID,
                        ABIE.PATH,
                        ABIE.HASH_PATH,
                        ABIE.BASED_ACC_MANIFEST_ID,
                        ABIE.DEFINITION,
                        ABIE.REMARK,
                        ABIE.BIZ_TERM)
                .from(ABIE)
                .where(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId.value())))
                .fetchInto(BieCopyAbie.class);
    }

    private List<BieCopyAsbie> getAsbieByOwnerTopLevelAsbiepId(TopLevelAsbiepId ownerTopLevelAsbiepId) {
        return dslContext.select(
                        ASBIE.ASBIE_ID,
                        ASBIE.GUID,
                        ASBIE.PATH,
                        ASBIE.HASH_PATH,
                        ASBIE.FROM_ABIE_ID,
                        ASBIE.TO_ASBIEP_ID,
                        ASBIE.BASED_ASCC_MANIFEST_ID,
                        ASBIE.DEFINITION,
                        ASBIE.CARDINALITY_MIN,
                        ASBIE.CARDINALITY_MAX,
                        ASBIE.IS_NILLABLE.as("nillable"),
                        ASBIE.REMARK,
                        ASBIE.SEQ_KEY,
                        ASBIE.IS_USED.as("used"),
                        ASBIE.IS_DEPRECATED.as("deprecated"))
                .from(ASBIE)
                .where(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId.value())))
                .fetchInto(BieCopyAsbie.class);
    }

    private List<BieCopyBbie> getBbieByOwnerTopLevelAsbiepId(TopLevelAsbiepId ownerTopLevelAsbiepId) {
        return dslContext.select(
                        BBIE.BBIE_ID,
                        BBIE.GUID,
                        BBIE.PATH,
                        BBIE.HASH_PATH,
                        BBIE.BASED_BCC_MANIFEST_ID,
                        BBIE.FROM_ABIE_ID,
                        BBIE.TO_BBIEP_ID,
                        BBIE.XBT_MANIFEST_ID,
                        BBIE.CODE_LIST_MANIFEST_ID,
                        BBIE.AGENCY_ID_LIST_MANIFEST_ID,
                        BBIE.CARDINALITY_MIN,
                        BBIE.CARDINALITY_MAX,
                        BBIE.DEFAULT_VALUE,
                        BBIE.FIXED_VALUE,
                        BBIE.FACET_MIN_LENGTH,
                        BBIE.FACET_MAX_LENGTH,
                        BBIE.FACET_PATTERN,
                        BBIE.IS_NILLABLE.as("nillable"),
                        BBIE.IS_NULL.as("nill"),
                        BBIE.DEFINITION,
                        BBIE.EXAMPLE,
                        BBIE.REMARK,
                        BBIE.SEQ_KEY,
                        BBIE.IS_USED.as("used"),
                        BBIE.IS_DEPRECATED.as("deprecated"))
                .from(BBIE)
                .where(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId.value())))
                .fetchInto(BieCopyBbie.class);
    }

    private List<BieCopyAsbiep> getAsbiepByOwnerTopLevelAsbiepId(TopLevelAsbiepId ownerTopLevelAsbiepId) {
        return dslContext.select(
                        ASBIEP.ASBIEP_ID,
                        ASBIEP.GUID,
                        ASBIEP.PATH,
                        ASBIEP.HASH_PATH,
                        ASBIEP.BASED_ASCCP_MANIFEST_ID,
                        ASBIEP.ROLE_OF_ABIE_ID,
                        ASBIEP.DEFINITION,
                        ASBIEP.REMARK,
                        ASBIEP.BIZ_TERM,
                        ASBIEP.DISPLAY_NAME)
                .from(ASBIEP)
                .where(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId.value())))
                .fetchInto(BieCopyAsbiep.class);
    }

    private List<BieCopyBbiep> getBbiepByOwnerTopLevelAsbiepId(TopLevelAsbiepId ownerTopLevelAsbiepId) {
        return dslContext.select(
                        BBIEP.BBIEP_ID,
                        BBIEP.GUID,
                        BBIEP.PATH,
                        BBIEP.HASH_PATH,
                        BBIEP.BASED_BCCP_MANIFEST_ID,
                        BBIEP.DEFINITION,
                        BBIEP.REMARK,
                        BBIEP.BIZ_TERM,
                        BBIEP.DISPLAY_NAME)
                .from(BBIEP)
                .where(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId.value())))
                .fetchInto(BieCopyBbiep.class);
    }

    private List<BieCopyBbieSc> getBbieScByOwnerTopLevelAsbiepId(TopLevelAsbiepId ownerTopLevelAsbiepId) {
        return dslContext.select(
                        BBIE_SC.BBIE_SC_ID,
                        BBIE_SC.GUID,
                        BBIE_SC.PATH,
                        BBIE_SC.HASH_PATH,
                        BBIE_SC.BBIE_ID,
                        BBIE_SC.BASED_DT_SC_MANIFEST_ID,
                        BBIE_SC.XBT_MANIFEST_ID,
                        BBIE_SC.CODE_LIST_MANIFEST_ID,
                        BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID,
                        BBIE_SC.CARDINALITY_MIN,
                        BBIE_SC.CARDINALITY_MAX,
                        BBIE_SC.DEFAULT_VALUE,
                        BBIE_SC.FIXED_VALUE,
                        BBIE_SC.FACET_MIN_LENGTH,
                        BBIE_SC.FACET_MAX_LENGTH,
                        BBIE_SC.FACET_PATTERN,
                        BBIE_SC.DEFINITION,
                        BBIE_SC.EXAMPLE,
                        BBIE_SC.REMARK,
                        BBIE_SC.BIZ_TERM,
                        BBIE_SC.DISPLAY_NAME,
                        BBIE_SC.IS_USED.as("used"),
                        BBIE_SC.IS_DEPRECATED.as("deprecated"))
                .from(BBIE_SC)
                .where(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId.value())))
                .fetchInto(BieCopyBbieSc.class);
    }

    @Data
    public static class BieCopyAbie {

        private AbieId abieId;
        private Guid guid;
        private String path;
        private String hashPath;
        private AccManifestId basedAccManifestId;
        private String definition;
        private String remark;
        private String bizTerm;

    }

    @Data
    public static class BieCopyAsbie {

        private AsbieId asbieId;
        private Guid guid;
        private String path;
        private String hashPath;
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
        private boolean deprecated;

    }

    @Data
    public static class BieCopyBbie {

        private BbieId bbieId;
        private Guid guid;
        private String path;
        private String hashPath;
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
        private BigInteger facetMinLength;
        private BigInteger facetMaxLength;
        private String facetPattern;
        private boolean nill;
        private String definition;
        private String example;
        private String remark;
        private double seqKey;
        private boolean used;
        private boolean deprecated;

    }

    @Data
    public static class BieCopyAsbiep {

        private AsbiepId asbiepId;
        private Guid guid;
        private String path;
        private String hashPath;
        private AsccpManifestId basedAsccpManifestId;
        private AbieId roleOfAbieId;
        private String definition;
        private String remark;
        private String bizTerm;
        private String displayName;

    }

    @Data
    public static class BieCopyBbiep {

        private BbiepId bbiepId;
        private Guid guid;
        private String path;
        private String hashPath;
        private BccpManifestId basedBccpManifestId;
        private String definition;
        private String remark;
        private String bizTerm;
        private String displayName;

    }

    @Data
    public static class BieCopyBbieSc {

        private BbieScId bbieScId;
        private Guid guid;
        private String path;
        private String hashPath;
        private BbieId bbieId;
        private DtScManifestId basedDtScManifestId;
        private XbtManifestId xbtManifestId;
        private CodeListManifestId codeListManifestId;
        private AgencyIdListManifestId agencyIdListManifestId;
        private int cardinalityMin;
        private int cardinalityMax;
        private String defaultValue;
        private String fixedValue;
        private BigInteger facetMinLength;
        private BigInteger facetMaxLength;
        private String facetPattern;
        private String definition;
        private String example;
        private String remark;
        private String bizTerm;
        private String displayName;
        private boolean used;
        private boolean deprecated;

    }

    private class BieCopyContext {

        private final ScoreUser requester;
        private final TopLevelAsbiepSummaryRecord sourceTopLevelAsbiep;
        private TopLevelAsbiepId copiedTopLevelAsbiepId;
        private AsbiepId copiedAsbiepId;
        private final List<BusinessContextId> bizCtxIds;
        private final UserId userId;
        private final boolean isDeveloper;
        private Timestamp timestamp;

        private final List<BieCopyAbie> abieList;

        private final List<BieCopyAsbiep> asbiepList;
        private final Map<AbieId, List<BieCopyAsbiep>> roleOfAbieToAsbiepMap;

        private final List<BieCopyBbiep> bbiepList;

        private final List<BieCopyAsbie> asbieList;
        private final Map<AbieId, List<BieCopyAsbie>> fromAbieToAsbieMap;
        private final Map<AsbiepId, List<BieCopyAsbie>> toAsbiepToAsbieMap;

        private final List<BieCopyBbie> bbieList;
        private final Map<AbieId, List<BieCopyBbie>> fromAbieToBbieMap;
        private final Map<BbiepId, List<BieCopyBbie>> toBbiepToBbieMap;

        private final List<BieCopyBbieSc> bbieScList;
        private final Map<BbieId, List<BieCopyBbieSc>> bbieToBbieScMap;

        public BieCopyContext(ScoreUser requester, BieCopyRequestEvent bieCopyRequestEvent) {
            var query = repositoryFactory.topLevelAsbiepQueryRepository(requester);

            TopLevelAsbiepId sourceTopLevelAsbiepId = bieCopyRequestEvent.getSourceTopLevelAsbiepId();
            sourceTopLevelAsbiep = query.getTopLevelAsbiepSummary(sourceTopLevelAsbiepId);

            copiedTopLevelAsbiepId = bieCopyRequestEvent.getCopiedTopLevelAsbiepId();

            bizCtxIds = bieCopyRequestEvent.getBizCtxIdList();
            this.requester = requester;
            userId = bieCopyRequestEvent.getUserId();
            isDeveloper = sessionService.getScoreUserByUserId(userId).isDeveloper();

            abieList = getAbieByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);

            asbiepList = getAsbiepByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);
            roleOfAbieToAsbiepMap = asbiepList.stream().collect(groupingBy(BieCopyAsbiep::getRoleOfAbieId));

            bbiepList = getBbiepByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);

            asbieList = getAsbieByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);
            fromAbieToAsbieMap = asbieList.stream().collect(groupingBy(BieCopyAsbie::getFromAbieId));
            toAsbiepToAsbieMap = asbieList.stream().collect(groupingBy(BieCopyAsbie::getToAsbiepId));

            bbieList = getBbieByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);
            fromAbieToBbieMap = bbieList.stream().collect(groupingBy(BieCopyBbie::getFromAbieId));
            toBbiepToBbieMap = bbieList.stream().collect(groupingBy(BieCopyBbie::getToBbiepId));

            bbieScList = getBbieScByOwnerTopLevelAsbiepId(sourceTopLevelAsbiepId);
            bbieToBbieScMap = bbieScList.stream().collect(groupingBy(BieCopyBbieSc::getBbieId));
        }

        public void execute() {
            timestamp = new Timestamp(System.currentTimeMillis());
            logger.debug("Begin copying from " + sourceTopLevelAsbiep.topLevelAsbiepId() +
                    " to " + copiedTopLevelAsbiepId);

            var topLevelAsbiepCommand = repositoryFactory.topLevelAsbiepCommandRepository(requester);
            new InsertBizCtxAssignmentArguments(topLevelAsbiepCommand)
                    .setTopLevelAsbiepId(copiedTopLevelAsbiepId)
                    .setBizCtxIds(bizCtxIds)
                    .execute();

            for (BieCopyAbie abie : abieList) {
                AbieId previousAbieId = abie.getAbieId();
                AbieId nextAbieId = insertAbie(abie);

                fireChangeEvent(previousAbieId, nextAbieId);
            }

            for (BieCopyAsbiep asbiep : asbiepList) {
                AsbiepId previousAsbiepId = asbiep.getAsbiepId();
                AsbiepId nextAsbiepId = insertAsbiep(asbiep);

                fireChangeEvent(previousAsbiepId, nextAsbiepId);
            }

            topLevelAsbiepCommand.updateAsbiepId(copiedAsbiepId, copiedTopLevelAsbiepId);

            for (BieCopyBbiep bbiep : bbiepList) {
                BbiepId previousBbiepId = bbiep.getBbiepId();
                BbiepId nextBbiepId = insertBbiep(bbiep);

                fireChangeEvent(previousBbiepId, nextBbiepId);
            }

            for (BieCopyAsbie asbie : asbieList) {
                AsbieId previousAsbieId = asbie.getAsbieId();
                AsbieId nextAsbieId = insertAsbie(asbie);

                fireChangeEvent(previousAsbieId, nextAsbieId);
            }

            for (BieCopyBbie bbie : bbieList) {
                BbieId previousBbieId = bbie.getBbieId();
                BbieId nextBbieId = insertBbie(bbie);

                fireChangeEvent(previousBbieId, nextBbieId);
            }

            for (BieCopyBbieSc bbieSc : bbieScList) {
                BbieScId previousBbieScId = bbieSc.getBbieScId();
                BbieScId nextBbieScId = insertBbieSc(bbieSc);

                fireChangeEvent(previousBbieScId, nextBbieScId);
            }

            // Issue #869
            if (isDeveloper) {
                removeBIEofEUEG();
            }

            repository.updateState(copiedTopLevelAsbiepId, BieState.WIP);

            logger.debug("End copying from " + sourceTopLevelAsbiep.topLevelAsbiepId() +
                    " to " + copiedTopLevelAsbiepId);
        }

        private void removeBIEofEUEG() {
            dslContext.deleteFrom(ASBIE)
                    .where(ASBIE.ASBIE_ID.in(dslContext.select(ASBIE.ASBIE_ID)
                            .from(ASBIE)
                            .join(ASCC_MANIFEST).on(ASBIE.BASED_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.ASCC_MANIFEST_ID))
                            .join(ACC_MANIFEST).on(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                            .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                            .where(and(
                                    ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(copiedTopLevelAsbiepId.value())),
                                    ACC.OAGIS_COMPONENT_TYPE.eq(OagisComponentType.UserExtensionGroup.getValue())
                            ))
                            .fetchInto(ULong.class)))
                    .execute();

            dslContext.deleteFrom(BBIE)
                    .where(BBIE.BBIE_ID.in(dslContext.select(BBIE.BBIE_ID)
                            .from(BBIE)
                            .join(BCC_MANIFEST).on(BBIE.BASED_BCC_MANIFEST_ID.eq(BCC_MANIFEST.BCC_MANIFEST_ID))
                            .join(ACC_MANIFEST).on(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                            .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                            .where(and(
                                    BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(copiedTopLevelAsbiepId.value())),
                                    ACC.OAGIS_COMPONENT_TYPE.eq(OagisComponentType.UserExtensionGroup.getValue())
                            ))
                            .fetchInto(ULong.class)))
                    .execute();
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
            if (sourceTopLevelAsbiep.asbiepId().equals(previousVal)) {
                copiedAsbiepId = nextVal;
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

        private AbieId insertAbie(BieCopyAbie abie) {

            return new AbieId(
                    dslContext.insertInto(ABIE)
                            .set(ABIE.GUID, ScoreGuidUtils.randomGuid())
                            .set(ABIE.PATH, abie.getPath())
                            .set(ABIE.HASH_PATH, abie.getHashPath())
                            .set(ABIE.BASED_ACC_MANIFEST_ID, ULong.valueOf(abie.getBasedAccManifestId().value()))
                            .set(ABIE.DEFINITION, abie.getDefinition())
                            .set(ABIE.CREATED_BY, ULong.valueOf(userId.value()))
                            .set(ABIE.LAST_UPDATED_BY, ULong.valueOf(userId.value()))
                            .set(ABIE.CREATION_TIMESTAMP, timestamp.toLocalDateTime())
                            .set(ABIE.LAST_UPDATE_TIMESTAMP, timestamp.toLocalDateTime())
                            .set(ABIE.REMARK, abie.getRemark())
                            .set(ABIE.BIZ_TERM, abie.getBizTerm())
                            .set(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(copiedTopLevelAsbiepId.value()))
                            .returning(ABIE.ABIE_ID).fetchOne().getValue(ABIE.ABIE_ID).toBigInteger()
            );
        }

        private AsbiepId insertAsbiep(BieCopyAsbiep asbiep) {

            return new AsbiepId(
                    dslContext.insertInto(ASBIEP)
                            .set(ASBIEP.GUID, ScoreGuidUtils.randomGuid())
                            .set(ASBIEP.PATH, asbiep.getPath())
                            .set(ASBIEP.HASH_PATH, asbiep.getHashPath())
                            .set(ASBIEP.BASED_ASCCP_MANIFEST_ID, ULong.valueOf(asbiep.getBasedAsccpManifestId().value()))
                            .set(ASBIEP.ROLE_OF_ABIE_ID, ULong.valueOf(asbiep.getRoleOfAbieId().value()))
                            .set(ASBIEP.DEFINITION, asbiep.getDefinition())
                            .set(ASBIEP.REMARK, asbiep.getRemark())
                            .set(ASBIEP.BIZ_TERM, asbiep.getBizTerm())
                            .set(ASBIEP.DISPLAY_NAME, asbiep.getDisplayName())
                            .set(ASBIEP.CREATED_BY, ULong.valueOf(userId.value()))
                            .set(ASBIEP.LAST_UPDATED_BY, ULong.valueOf(userId.value()))
                            .set(ASBIEP.CREATION_TIMESTAMP, timestamp.toLocalDateTime())
                            .set(ASBIEP.LAST_UPDATE_TIMESTAMP, timestamp.toLocalDateTime())
                            .set(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(copiedTopLevelAsbiepId.value()))
                            .returning(ASBIEP.ASBIEP_ID).fetchOne().getValue(ASBIEP.ASBIEP_ID).toBigInteger()
            );
        }

        private BbiepId insertBbiep(BieCopyBbiep bbiep) {

            return new BbiepId(
                    dslContext.insertInto(BBIEP)
                            .set(BBIEP.GUID, ScoreGuidUtils.randomGuid())
                            .set(BBIEP.PATH, bbiep.getPath())
                            .set(BBIEP.HASH_PATH, bbiep.getHashPath())
                            .set(BBIEP.BASED_BCCP_MANIFEST_ID, ULong.valueOf(bbiep.getBasedBccpManifestId().value()))
                            .set(BBIEP.DEFINITION, bbiep.getDefinition())
                            .set(BBIEP.REMARK, bbiep.getRemark())
                            .set(BBIEP.BIZ_TERM, bbiep.getBizTerm())
                            .set(BBIEP.DISPLAY_NAME, bbiep.getDisplayName())
                            .set(BBIEP.CREATED_BY, ULong.valueOf(userId.value()))
                            .set(BBIEP.LAST_UPDATED_BY, ULong.valueOf(userId.value()))
                            .set(BBIEP.CREATION_TIMESTAMP, timestamp.toLocalDateTime())
                            .set(BBIEP.LAST_UPDATE_TIMESTAMP, timestamp.toLocalDateTime())
                            .set(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(copiedTopLevelAsbiepId.value()))
                            .returning(BBIEP.BBIEP_ID).fetchOne().getValue(BBIEP.BBIEP_ID).toBigInteger()
            );
        }

        private AsbieId insertAsbie(BieCopyAsbie asbie) {

            return new AsbieId(
                    dslContext.insertInto(ASBIE)
                            .set(ASBIE.GUID, ScoreGuidUtils.randomGuid())
                            .set(ASBIE.PATH, asbie.getPath())
                            .set(ASBIE.HASH_PATH, asbie.getHashPath())
                            .set(ASBIE.FROM_ABIE_ID, ULong.valueOf(asbie.getFromAbieId().value()))
                            .set(ASBIE.TO_ASBIEP_ID, ULong.valueOf(asbie.getToAsbiepId().value()))
                            .set(ASBIE.BASED_ASCC_MANIFEST_ID, ULong.valueOf(asbie.getBasedAsccManifestId().value()))
                            .set(ASBIE.DEFINITION, asbie.getDefinition())
                            .set(ASBIE.REMARK, asbie.getRemark())
                            .set(ASBIE.CARDINALITY_MIN, asbie.getCardinalityMin())
                            .set(ASBIE.CARDINALITY_MAX, asbie.getCardinalityMax())
                            .set(ASBIE.IS_NILLABLE, (byte) ((asbie.isNillable()) ? 1 : 0))
                            .set(ASBIE.IS_USED, (byte) ((asbie.isUsed()) ? 1 : 0))
                            .set(ASBIE.IS_DEPRECATED, (byte) ((asbie.isDeprecated()) ? 1 : 0))
                            .set(ASBIE.SEQ_KEY, BigDecimal.valueOf(asbie.getSeqKey()))
                            .set(ASBIE.CREATED_BY, ULong.valueOf(userId.value()))
                            .set(ASBIE.LAST_UPDATED_BY, ULong.valueOf(userId.value()))
                            .set(ASBIE.CREATION_TIMESTAMP, timestamp.toLocalDateTime())
                            .set(ASBIE.LAST_UPDATE_TIMESTAMP, timestamp.toLocalDateTime())
                            .set(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(copiedTopLevelAsbiepId.value()))
                            .returning(ASBIE.ASBIE_ID).fetchOne().getValue(ASBIE.ASBIE_ID).toBigInteger()
            );
        }

        private BbieId insertBbie(BieCopyBbie bbie) {

            return new BbieId(
                    dslContext.insertInto(BBIE)
                            .set(BBIE.GUID, ScoreGuidUtils.randomGuid())
                            .set(BBIE.PATH, bbie.getPath())
                            .set(BBIE.HASH_PATH, bbie.getHashPath())
                            .set(BBIE.FROM_ABIE_ID, ULong.valueOf(bbie.getFromAbieId().value()))
                            .set(BBIE.TO_BBIEP_ID, ULong.valueOf(bbie.getToBbiepId().value()))
                            .set(BBIE.BASED_BCC_MANIFEST_ID, ULong.valueOf(bbie.getBasedBccManifestId().value()))
                            .set(BBIE.XBT_MANIFEST_ID, (bbie.getXbtManifestId() != null) ? ULong.valueOf(bbie.getXbtManifestId().value()) : null)
                            .set(BBIE.CODE_LIST_MANIFEST_ID, (bbie.getCodeListManifestId() != null) ? ULong.valueOf(bbie.getCodeListManifestId().value()) : null)
                            .set(BBIE.AGENCY_ID_LIST_MANIFEST_ID, (bbie.getAgencyIdListManifestId() != null) ? ULong.valueOf(bbie.getAgencyIdListManifestId().value()) : null)
                            .set(BBIE.DEFAULT_VALUE, bbie.getDefaultValue())
                            .set(BBIE.FIXED_VALUE, bbie.getFixedValue())
                            .set(BBIE.FACET_MIN_LENGTH, (bbie.getFacetMinLength() != null) ? ULong.valueOf(bbie.getFacetMinLength()) : null)
                            .set(BBIE.FACET_MAX_LENGTH, (bbie.getFacetMaxLength() != null) ? ULong.valueOf(bbie.getFacetMaxLength()) : null)
                            .set(BBIE.FACET_PATTERN, bbie.getFacetPattern())
                            .set(BBIE.DEFINITION, bbie.getDefinition())
                            .set(BBIE.EXAMPLE, bbie.getExample())
                            .set(BBIE.REMARK, bbie.getRemark())
                            .set(BBIE.CARDINALITY_MIN, bbie.getCardinalityMin())
                            .set(BBIE.CARDINALITY_MAX, bbie.getCardinalityMax())
                            .set(BBIE.IS_NILLABLE, (byte) ((bbie.isNillable()) ? 1 : 0))
                            .set(BBIE.IS_NULL, (byte) ((bbie.isNill()) ? 1 : 0))
                            .set(BBIE.SEQ_KEY, BigDecimal.valueOf(bbie.getSeqKey()))
                            .set(BBIE.IS_USED, (byte) ((bbie.isUsed()) ? 1 : 0))
                            .set(BBIE.IS_DEPRECATED, (byte) ((bbie.isDeprecated()) ? 1 : 0))
                            .set(BBIE.CREATED_BY, ULong.valueOf(userId.value()))
                            .set(BBIE.LAST_UPDATED_BY, ULong.valueOf(userId.value()))
                            .set(BBIE.CREATION_TIMESTAMP, timestamp.toLocalDateTime())
                            .set(BBIE.LAST_UPDATE_TIMESTAMP, timestamp.toLocalDateTime())
                            .set(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(copiedTopLevelAsbiepId.value()))
                            .returning(BBIE.BBIE_ID).fetchOne().getValue(BBIE.BBIE_ID).toBigInteger()
            );
        }

        private BbieScId insertBbieSc(BieCopyBbieSc bbieSc) {

            return new BbieScId(
                    dslContext.insertInto(BBIE_SC)
                            .set(BBIE_SC.GUID, ScoreGuidUtils.randomGuid())
                            .set(BBIE_SC.PATH, bbieSc.getPath())
                            .set(BBIE_SC.HASH_PATH, bbieSc.getHashPath())
                            .set(BBIE_SC.BBIE_ID, ULong.valueOf(bbieSc.getBbieId().value()))
                            .set(BBIE_SC.BASED_DT_SC_MANIFEST_ID, ULong.valueOf(bbieSc.getBasedDtScManifestId().value()))
                            .set(BBIE_SC.XBT_MANIFEST_ID, (bbieSc.getXbtManifestId() != null) ? ULong.valueOf(bbieSc.getXbtManifestId().value()) : null)
                            .set(BBIE_SC.CODE_LIST_MANIFEST_ID, (bbieSc.getCodeListManifestId() != null) ? ULong.valueOf(bbieSc.getCodeListManifestId().value()) : null)
                            .set(BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID, (bbieSc.getAgencyIdListManifestId() != null) ? ULong.valueOf(bbieSc.getAgencyIdListManifestId().value()) : null)
                            .set(BBIE_SC.DEFAULT_VALUE, bbieSc.getDefaultValue())
                            .set(BBIE_SC.FIXED_VALUE, bbieSc.getFixedValue())
                            .set(BBIE_SC.FACET_MIN_LENGTH, (bbieSc.getFacetMinLength() != null) ? ULong.valueOf(bbieSc.getFacetMinLength()) : null)
                            .set(BBIE_SC.FACET_MAX_LENGTH, (bbieSc.getFacetMaxLength() != null) ? ULong.valueOf(bbieSc.getFacetMaxLength()) : null)
                            .set(BBIE_SC.FACET_PATTERN, bbieSc.getFacetPattern())
                            .set(BBIE_SC.DEFINITION, bbieSc.getDefinition())
                            .set(BBIE_SC.EXAMPLE, bbieSc.getExample())
                            .set(BBIE_SC.REMARK, bbieSc.getRemark())
                            .set(BBIE_SC.BIZ_TERM, bbieSc.getBizTerm())
                            .set(BBIE_SC.DISPLAY_NAME, bbieSc.getDisplayName())
                            .set(BBIE_SC.CARDINALITY_MIN, bbieSc.getCardinalityMin())
                            .set(BBIE_SC.CARDINALITY_MAX, bbieSc.getCardinalityMax())
                            .set(BBIE_SC.IS_USED, (byte) ((bbieSc.isUsed()) ? 1 : 0))
                            .set(BBIE_SC.IS_DEPRECATED, (byte) ((bbieSc.isDeprecated()) ? 1 : 0))
                            .set(BBIE_SC.CREATED_BY, ULong.valueOf(userId.value()))
                            .set(BBIE_SC.LAST_UPDATED_BY, ULong.valueOf(userId.value()))
                            .set(BBIE_SC.CREATION_TIMESTAMP, timestamp.toLocalDateTime())
                            .set(BBIE_SC.LAST_UPDATE_TIMESTAMP, timestamp.toLocalDateTime())
                            .set(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(copiedTopLevelAsbiepId.value()))
                            .returning(BBIE_SC.BBIE_SC_ID).fetchOne().getValue(BBIE_SC.BBIE_SC_ID).toBigInteger()
            );
        }
    }

}
