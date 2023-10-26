package org.oagi.score.gateway.http.api.bie_management.service;

import lombok.Data;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.data.TopLevelAsbiep;
import org.oagi.score.gateway.http.api.bie_management.data.BieCopyRequest;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.event.BieCopyRequestEvent;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.redis.event.EventListenerContainer;
import org.oagi.score.repo.BusinessInformationEntityRepository;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repository.TopLevelAsbiepRepository;
import org.oagi.score.service.common.data.OagisComponentType;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.security.core.AuthenticatedPrincipal;
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
import static org.oagi.score.repo.api.bie.model.BieState.Initiating;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Service
@Transactional
public class BieCopyService implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private BieRepository repository;

    @Autowired
    private TopLevelAsbiepRepository topLevelAsbiepRepository;

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
    public void copyBie(AuthenticatedPrincipal user, BieCopyRequest request) {
        BigInteger sourceTopLevelAsbiepId = request.getTopLevelAsbiepId();
        if (sourceTopLevelAsbiepId == null) {
            throw new IllegalArgumentException("`topLevelAsbiepId` parameter must not be null.");
        }

        List<BigInteger> bizCtxIds = request.getBizCtxIds();
        if (bizCtxIds == null || bizCtxIds.isEmpty()) {
            throw new IllegalArgumentException("`bizCtxIds` parameter must not be null.");
        }

        BigInteger userId = sessionService.userId(user);
        if (userId == null) {
            throw new IllegalArgumentException("`userId` parameter must not be null.");
        }

        long millis = System.currentTimeMillis();

        TopLevelAsbiep sourceTopLevelAsbiep = topLevelAsbiepRepository.findById(sourceTopLevelAsbiepId);
        ULong copiedTopLevelAsbiepId = bieRepository.insertTopLevelAsbiep()
                .setReleaseId(sourceTopLevelAsbiep.getReleaseId())
                .setBieState(Initiating)
                .setVersion(sourceTopLevelAsbiep.getVersion())
                .setStatus(sourceTopLevelAsbiep.getStatus())
                .setInverseMode(sourceTopLevelAsbiep.isInverseMode())
                .setUserId(userId)
                .setTimestamp(millis)
                .setSource(sourceTopLevelAsbiepId, "Copy")
                .execute();

        BieCopyRequestEvent bieCopyRequestEvent = new BieCopyRequestEvent(
                sourceTopLevelAsbiepId, copiedTopLevelAsbiepId.toBigInteger(), bizCtxIds, userId
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
        RLock lock = redissonClient.getLock("BieCopyRequestEvent:" + bieCopyRequestEvent.hashCode());
        if (!lock.tryLock()) {
            return;
        }
        try {
            logger.debug("Received BieCopyRequestEvent: " + bieCopyRequestEvent);

            BieCopyContext copyContext = new BieCopyContext(bieCopyRequestEvent);
            copyContext.execute();
        } finally {
            lock.unlock();
        }
    }

    private List<BieCopyAbie> getAbieByOwnerTopLevelAsbiepId(BigInteger ownerTopLevelAsbiepId) {
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
                .where(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId)))
                .fetchInto(BieCopyAbie.class);
    }

    private List<BieCopyAsbie> getAsbieByOwnerTopLevelAsbiepId(BigInteger ownerTopLevelAsbiepId) {
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
                .where(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId)))
                .fetchInto(BieCopyAsbie.class);
    }

    private List<BieCopyBbie> getBbieByOwnerTopLevelAsbiepId(BigInteger ownerTopLevelAsbiepId) {
        return dslContext.select(
                BBIE.BBIE_ID,
                BBIE.GUID,
                BBIE.PATH,
                BBIE.HASH_PATH,
                BBIE.BASED_BCC_MANIFEST_ID,
                BBIE.FROM_ABIE_ID,
                BBIE.TO_BBIEP_ID,
                BBIE.BDT_PRI_RESTRI_ID,
                BBIE.CODE_LIST_MANIFEST_ID,
                BBIE.AGENCY_ID_LIST_MANIFEST_ID,
                BBIE.CARDINALITY_MIN,
                BBIE.CARDINALITY_MAX,
                BBIE.DEFAULT_VALUE,
                BBIE.FIXED_VALUE,
                BBIE.FACET_MIN_LENGTH,
                BBIE.FACET_MAX_LENGTH,
                BBIE.FACET_PATTERN,
                BBIE.FACET_MIN_INCLUSIVE,
                BBIE.FACET_MIN_EXCLUSIVE,
                BBIE.FACET_MAX_INCLUSIVE,
                BBIE.FACET_MAX_EXCLUSIVE,
                BBIE.IS_NILLABLE.as("nillable"),
                BBIE.IS_NULL.as("nill"),
                BBIE.DEFINITION,
                BBIE.EXAMPLE,
                BBIE.REMARK,
                BBIE.SEQ_KEY,
                BBIE.IS_USED.as("used"),
                BBIE.IS_DEPRECATED.as("deprecated"))
                .from(BBIE)
                .where(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId)))
                .fetchInto(BieCopyBbie.class);
    }

    private List<BieCopyAsbiep> getAsbiepByOwnerTopLevelAsbiepId(BigInteger ownerTopLevelAsbiepId) {
        return dslContext.select(
                ASBIEP.ASBIEP_ID,
                ASBIEP.GUID,
                ASBIEP.PATH,
                ASBIEP.HASH_PATH,
                ASBIEP.BASED_ASCCP_MANIFEST_ID,
                ASBIEP.ROLE_OF_ABIE_ID,
                ASBIEP.DEFINITION,
                ASBIEP.REMARK,
                ASBIEP.BIZ_TERM)
                .from(ASBIEP)
                .where(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId)))
                .fetchInto(BieCopyAsbiep.class);
    }

    private List<BieCopyBbiep> getBbiepByOwnerTopLevelAsbiepId(BigInteger ownerTopLevelAsbiepId) {
        return dslContext.select(
                BBIEP.BBIEP_ID,
                BBIEP.GUID,
                BBIEP.PATH,
                BBIEP.HASH_PATH,
                BBIEP.BASED_BCCP_MANIFEST_ID,
                BBIEP.DEFINITION,
                BBIEP.REMARK,
                BBIEP.BIZ_TERM)
                .from(BBIEP)
                .where(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId)))
                .fetchInto(BieCopyBbiep.class);
    }

    private List<BieCopyBbieSc> getBbieScByOwnerTopLevelAsbiepId(BigInteger ownerTopLevelAsbiepId) {
        return dslContext.select(
                BBIE_SC.BBIE_SC_ID,
                BBIE_SC.GUID,
                BBIE_SC.PATH,
                BBIE_SC.HASH_PATH,
                BBIE_SC.BBIE_ID,
                BBIE_SC.BASED_DT_SC_MANIFEST_ID,
                BBIE_SC.DT_SC_PRI_RESTRI_ID,
                BBIE_SC.CODE_LIST_MANIFEST_ID,
                BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID,
                BBIE_SC.CARDINALITY_MIN,
                BBIE_SC.CARDINALITY_MAX,
                BBIE_SC.DEFAULT_VALUE,
                BBIE_SC.FIXED_VALUE,
                BBIE_SC.FACET_MIN_LENGTH,
                BBIE_SC.FACET_MAX_LENGTH,
                BBIE_SC.FACET_PATTERN,
                BBIE_SC.FACET_MIN_INCLUSIVE,
                BBIE_SC.FACET_MIN_EXCLUSIVE,
                BBIE_SC.FACET_MAX_INCLUSIVE,
                BBIE_SC.FACET_MAX_EXCLUSIVE,
                BBIE_SC.DEFINITION,
                BBIE_SC.EXAMPLE,
                BBIE_SC.REMARK,
                BBIE_SC.BIZ_TERM,
                BBIE_SC.IS_USED.as("used"),
                BBIE_SC.IS_DEPRECATED.as("deprecated"))
                .from(BBIE_SC)
                .where(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(ownerTopLevelAsbiepId)))
                .fetchInto(BieCopyBbieSc.class);
    }

    @Data
    public static class BieCopyAbie {

        private BigInteger abieId;
        private String guid;
        private String path;
        private String hashPath;
        private BigInteger basedAccManifestId;
        private String definition;
        private String remark;
        private String bizTerm;

    }

    @Data
    public static class BieCopyAsbie {

        private BigInteger asbieId;
        private String guid;
        private String path;
        private String hashPath;
        private BigInteger fromAbieId;
        private BigInteger toAsbiepId;
        private BigInteger basedAsccManifestId;
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

        private BigInteger bbieId;
        private String guid;
        private String path;
        private String hashPath;
        private BigInteger basedBccManifestId;
        private BigInteger fromAbieId;
        private BigInteger toBbiepId;
        private Long bdtPriRestriId;
        private Long codeListManifestId;
        private Long agencyIdListManifestId;
        private int cardinalityMin;
        private int cardinalityMax;
        private String defaultValue;
        private boolean nillable;
        private String fixedValue;
        private BigInteger facetMinLength;
        private BigInteger facetMaxLength;
        private String facetPattern;
        private String facetMinInclusive;
        private String facetMinExclusive;
        private String facetMaxInclusive;
        private String facetMaxExclusive;
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

        private BigInteger asbiepId;
        private String guid;
        private String path;
        private String hashPath;
        private BigInteger basedAsccpManifestId;
        private BigInteger roleOfAbieId;
        private String definition;
        private String remark;
        private String bizTerm;

    }

    @Data
    public static class BieCopyBbiep {

        private BigInteger bbiepId;
        private String guid;
        private String path;
        private String hashPath;
        private BigInteger basedBccpManifestId;
        private String definition;
        private String remark;
        private String bizTerm;

    }

    @Data
    public static class BieCopyBbieSc {

        private BigInteger bbieScId;
        private String guid;
        private String path;
        private String hashPath;
        private BigInteger bbieId;
        private BigInteger basedDtScManifestId;
        private Long dtScPriRestriId;
        private Long codeListManifestId;
        private Long agencyIdListManifestId;
        private int cardinalityMin;
        private int cardinalityMax;
        private String defaultValue;
        private String fixedValue;
        private BigInteger facetMinLength;
        private BigInteger facetMaxLength;
        private String facetPattern;
        private String facetMinInclusive;
        private String facetMinExclusive;
        private String facetMaxInclusive;
        private String facetMaxExclusive;
        private String definition;
        private String example;
        private String remark;
        private String bizTerm;
        private boolean used;
        private boolean deprecated;

    }

    private class BieCopyContext {

        private final TopLevelAsbiep sourceTopLevelAsbiep;
        private final TopLevelAsbiep copiedTopLevelAsbiep;
        private final List<BigInteger> bizCtxIds;
        private final BigInteger userId;
        private final boolean isDeveloper;
        private Timestamp timestamp;

        private final List<BieCopyAbie> abieList;

        private final List<BieCopyAsbiep> asbiepList;
        private final Map<BigInteger, List<BieCopyAsbiep>> roleOfAbieToAsbiepMap;

        private final List<BieCopyBbiep> bbiepList;

        private final List<BieCopyAsbie> asbieList;
        private final Map<BigInteger, List<BieCopyAsbie>> fromAbieToAsbieMap;
        private final Map<BigInteger, List<BieCopyAsbie>> toAsbiepToAsbieMap;

        private final List<BieCopyBbie> bbieList;
        private final Map<BigInteger, List<BieCopyBbie>> fromAbieToBbieMap;
        private final Map<BigInteger, List<BieCopyBbie>> toBbiepToBbieMap;

        private final List<BieCopyBbieSc> bbieScList;
        private final Map<BigInteger, List<BieCopyBbieSc>> bbieToBbieScMap;

        public BieCopyContext(BieCopyRequestEvent bieCopyRequestEvent) {
            BigInteger sourceTopLevelAsbiepId = bieCopyRequestEvent.getSourceTopLevelAsbiepId();
            sourceTopLevelAsbiep = topLevelAsbiepRepository.findById(sourceTopLevelAsbiepId);

            BigInteger copiedTopLevelAsbiepId = bieCopyRequestEvent.getCopiedTopLevelAsbiepId();
            copiedTopLevelAsbiep = topLevelAsbiepRepository.findById(copiedTopLevelAsbiepId);

            bizCtxIds = bieCopyRequestEvent.getBizCtxIds();
            userId = bieCopyRequestEvent.getUserId();
            isDeveloper = sessionService.isDeveloper(userId);

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
            logger.debug("Begin copying from " + sourceTopLevelAsbiep.getTopLevelAsbiepId() +
                    " to " + copiedTopLevelAsbiep.getTopLevelAsbiepId());

            bieRepository.insertBizCtxAssignments()
                    .setTopLevelAsbiepId(copiedTopLevelAsbiep.getTopLevelAsbiepId())
                    .setBizCtxIds(bizCtxIds)
                    .execute();

            for (BieCopyAbie abie : abieList) {
                BigInteger previousAbieId = abie.getAbieId();
                BigInteger nextAbieId = insertAbie(abie);

                fireChangeEvent("abie", previousAbieId, nextAbieId);
            }

            for (BieCopyAsbiep asbiep : asbiepList) {
                BigInteger previousAsbiepId = asbiep.getAsbiepId();
                BigInteger nextAsbiepId = insertAsbiep(asbiep);

                fireChangeEvent("asbiep", previousAsbiepId, nextAsbiepId);
            }

            bieRepository.updateTopLevelAsbiep()
                    .setAsbiepId(ULong.valueOf(copiedTopLevelAsbiep.getAsbiepId()))
                    .setTopLevelAsbiepId(copiedTopLevelAsbiep.getTopLevelAsbiepId())
                    .execute();

            for (BieCopyBbiep bbiep : bbiepList) {
                BigInteger previousBbiepId = bbiep.getBbiepId();
                BigInteger nextBbiepId = insertBbiep(bbiep);

                fireChangeEvent("bbiep", previousBbiepId, nextBbiepId);
            }

            for (BieCopyAsbie asbie : asbieList) {
                BigInteger previousAsbieId = asbie.getAsbieId();
                BigInteger nextAsbieId = insertAsbie(asbie);

                fireChangeEvent("asbie", previousAsbieId, nextAsbieId);
            }

            for (BieCopyBbie bbie : bbieList) {
                BigInteger previousBbieId = bbie.getBbieId();
                BigInteger nextBbieId = insertBbie(bbie);

                fireChangeEvent("bbie", previousBbieId, nextBbieId);
            }

            for (BieCopyBbieSc bbieSc : bbieScList) {
                BigInteger previousBbieScId = bbieSc.getBbieId();
                BigInteger nextBbieScId = insertBbieSc(bbieSc);

                fireChangeEvent("bbie_sc", previousBbieScId, nextBbieScId);
            }

            // Issue #869
            if (isDeveloper) {
                removeBIEofEUEG();
            }

            repository.updateState(copiedTopLevelAsbiep.getTopLevelAsbiepId(), BieState.WIP);

            logger.debug("End copying from " + sourceTopLevelAsbiep.getTopLevelAsbiepId() +
                    " to " + copiedTopLevelAsbiep.getTopLevelAsbiepId());
        }

        private void removeBIEofEUEG() {
            dslContext.deleteFrom(ASBIE)
                    .where(ASBIE.ASBIE_ID.in(dslContext.select(ASBIE.ASBIE_ID)
                            .from(ASBIE)
                            .join(ASCC_MANIFEST).on(ASBIE.BASED_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.ASCC_MANIFEST_ID))
                            .join(ACC_MANIFEST).on(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                            .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                            .where(and(
                                    ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(copiedTopLevelAsbiep.getTopLevelAsbiepId())),
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
                                    BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(copiedTopLevelAsbiep.getTopLevelAsbiepId())),
                                    ACC.OAGIS_COMPONENT_TYPE.eq(OagisComponentType.UserExtensionGroup.getValue())
                            ))
                            .fetchInto(ULong.class)))
                    .execute();
        }

        private void fireChangeEvent(String type, BigInteger previousVal, BigInteger nextVal) {
            switch (type) {
                case "abie":
                    roleOfAbieToAsbiepMap.getOrDefault(previousVal, Collections.emptyList()).stream().forEach(asbiep -> {
                        asbiep.setRoleOfAbieId(nextVal);
                    });
                    fromAbieToAsbieMap.getOrDefault(previousVal, Collections.emptyList()).stream().forEach(asbie -> {
                        asbie.setFromAbieId(nextVal);
                    });
                    fromAbieToBbieMap.getOrDefault(previousVal, Collections.emptyList()).stream().forEach(bbie -> {
                        bbie.setFromAbieId(nextVal);
                    });

                    break;

                case "asbiep":
                    if (sourceTopLevelAsbiep.getAsbiepId().equals(previousVal)) {
                        copiedTopLevelAsbiep.setAsbiepId(nextVal);
                    }
                    toAsbiepToAsbieMap.getOrDefault(previousVal, Collections.emptyList()).stream().forEach(asbie -> {
                        asbie.setToAsbiepId(nextVal);
                    });

                    break;

                case "bbiep":
                    toBbiepToBbieMap.getOrDefault(previousVal, Collections.emptyList()).stream().forEach(bbie -> {
                        bbie.setToBbiepId(nextVal);
                    });

                    break;

                case "bbie":
                    bbieToBbieScMap.getOrDefault(previousVal, Collections.emptyList()).stream().forEach(bbieSc -> {
                        bbieSc.setBbieId(nextVal);
                    });

                    break;
            }
        }


        private BigInteger insertAbie(BieCopyAbie abie) {

            return dslContext.insertInto(ABIE)
                    .set(ABIE.GUID, ScoreGuid.randomGuid())
                    .set(ABIE.PATH, abie.getPath())
                    .set(ABIE.HASH_PATH, abie.getHashPath())
                    .set(ABIE.BASED_ACC_MANIFEST_ID, ULong.valueOf(abie.getBasedAccManifestId()))
                    .set(ABIE.DEFINITION, abie.getDefinition())
                    .set(ABIE.CREATED_BY, ULong.valueOf(userId))
                    .set(ABIE.LAST_UPDATED_BY, ULong.valueOf(userId))
                    .set(ABIE.CREATION_TIMESTAMP, timestamp.toLocalDateTime())
                    .set(ABIE.LAST_UPDATE_TIMESTAMP, timestamp.toLocalDateTime())
                    .set(ABIE.REMARK, abie.getRemark())
                    .set(ABIE.BIZ_TERM, abie.getBizTerm())
                    .set(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(copiedTopLevelAsbiep.getTopLevelAsbiepId()))
                    .returning(ABIE.ABIE_ID).fetchOne().getValue(ABIE.ABIE_ID).toBigInteger();
        }

        private BigInteger insertAsbiep(BieCopyAsbiep asbiep) {

            return dslContext.insertInto(ASBIEP)
                    .set(ASBIEP.GUID, ScoreGuid.randomGuid())
                    .set(ASBIEP.PATH, asbiep.getPath())
                    .set(ASBIEP.HASH_PATH, asbiep.getHashPath())
                    .set(ASBIEP.BASED_ASCCP_MANIFEST_ID, ULong.valueOf(asbiep.getBasedAsccpManifestId()))
                    .set(ASBIEP.ROLE_OF_ABIE_ID, ULong.valueOf(asbiep.getRoleOfAbieId()))
                    .set(ASBIEP.DEFINITION, asbiep.getDefinition())
                    .set(ASBIEP.REMARK, asbiep.getRemark())
                    .set(ASBIEP.BIZ_TERM, asbiep.getBizTerm())
                    .set(ASBIEP.CREATED_BY, ULong.valueOf(userId))
                    .set(ASBIEP.LAST_UPDATED_BY, ULong.valueOf(userId))
                    .set(ASBIEP.CREATION_TIMESTAMP, timestamp.toLocalDateTime())
                    .set(ASBIEP.LAST_UPDATE_TIMESTAMP, timestamp.toLocalDateTime())
                    .set(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(copiedTopLevelAsbiep.getTopLevelAsbiepId()))
                    .returning(ASBIEP.ASBIEP_ID).fetchOne().getValue(ASBIEP.ASBIEP_ID).toBigInteger();
        }

        private BigInteger insertBbiep(BieCopyBbiep bbiep) {

            return dslContext.insertInto(BBIEP)
                    .set(BBIEP.GUID, ScoreGuid.randomGuid())
                    .set(BBIEP.PATH, bbiep.getPath())
                    .set(BBIEP.HASH_PATH, bbiep.getHashPath())
                    .set(BBIEP.BASED_BCCP_MANIFEST_ID, ULong.valueOf(bbiep.getBasedBccpManifestId()))
                    .set(BBIEP.DEFINITION, bbiep.getDefinition())
                    .set(BBIEP.REMARK, bbiep.getRemark())
                    .set(BBIEP.BIZ_TERM, bbiep.getBizTerm())
                    .set(BBIEP.CREATED_BY, ULong.valueOf(userId))
                    .set(BBIEP.LAST_UPDATED_BY, ULong.valueOf(userId))
                    .set(BBIEP.CREATION_TIMESTAMP, timestamp.toLocalDateTime())
                    .set(BBIEP.LAST_UPDATE_TIMESTAMP, timestamp.toLocalDateTime())
                    .set(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(copiedTopLevelAsbiep.getTopLevelAsbiepId()))
                    .returning(BBIEP.BBIEP_ID).fetchOne().getValue(BBIEP.BBIEP_ID).toBigInteger();
        }

        private BigInteger insertAsbie(BieCopyAsbie asbie) {

            return dslContext.insertInto(ASBIE)
                    .set(ASBIE.GUID, ScoreGuid.randomGuid())
                    .set(ASBIE.PATH, asbie.getPath())
                    .set(ASBIE.HASH_PATH, asbie.getHashPath())
                    .set(ASBIE.FROM_ABIE_ID, ULong.valueOf(asbie.getFromAbieId()))
                    .set(ASBIE.TO_ASBIEP_ID, ULong.valueOf(asbie.getToAsbiepId()))
                    .set(ASBIE.BASED_ASCC_MANIFEST_ID, ULong.valueOf(asbie.getBasedAsccManifestId()))
                    .set(ASBIE.DEFINITION, asbie.getDefinition())
                    .set(ASBIE.REMARK, asbie.getRemark())
                    .set(ASBIE.CARDINALITY_MIN, asbie.getCardinalityMin())
                    .set(ASBIE.CARDINALITY_MAX, asbie.getCardinalityMax())
                    .set(ASBIE.IS_NILLABLE, (byte) ((asbie.isNillable()) ? 1 : 0))
                    .set(ASBIE.IS_USED, (byte) ((asbie.isUsed()) ? 1 : 0))
                    .set(ASBIE.IS_DEPRECATED, (byte) ((asbie.isDeprecated()) ? 1 : 0))
                    .set(ASBIE.SEQ_KEY, BigDecimal.valueOf(asbie.getSeqKey()))
                    .set(ASBIE.CREATED_BY, ULong.valueOf(userId))
                    .set(ASBIE.LAST_UPDATED_BY, ULong.valueOf(userId))
                    .set(ASBIE.CREATION_TIMESTAMP, timestamp.toLocalDateTime())
                    .set(ASBIE.LAST_UPDATE_TIMESTAMP, timestamp.toLocalDateTime())
                    .set(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(copiedTopLevelAsbiep.getTopLevelAsbiepId()))
                    .returning(ASBIE.ASBIE_ID).fetchOne().getValue(ASBIE.ASBIE_ID).toBigInteger();
        }

        private BigInteger insertBbie(BieCopyBbie bbie) {

            return dslContext.insertInto(BBIE)
                    .set(BBIE.GUID, ScoreGuid.randomGuid())
                    .set(BBIE.PATH, bbie.getPath())
                    .set(BBIE.HASH_PATH, bbie.getHashPath())
                    .set(BBIE.FROM_ABIE_ID, ULong.valueOf(bbie.getFromAbieId()))
                    .set(BBIE.TO_BBIEP_ID, ULong.valueOf(bbie.getToBbiepId()))
                    .set(BBIE.BASED_BCC_MANIFEST_ID, ULong.valueOf(bbie.getBasedBccManifestId()))
                    .set(BBIE.BDT_PRI_RESTRI_ID, (bbie.getBdtPriRestriId() != null) ? ULong.valueOf(bbie.getBdtPriRestriId()) : null)
                    .set(BBIE.CODE_LIST_MANIFEST_ID, (bbie.getCodeListManifestId() != null) ? ULong.valueOf(bbie.getCodeListManifestId()) : null)
                    .set(BBIE.AGENCY_ID_LIST_MANIFEST_ID, (bbie.getAgencyIdListManifestId() != null) ? ULong.valueOf(bbie.getAgencyIdListManifestId()) : null)
                    .set(BBIE.DEFAULT_VALUE, bbie.getDefaultValue())
                    .set(BBIE.FIXED_VALUE, bbie.getFixedValue())
                    .set(BBIE.FACET_MIN_LENGTH, (bbie.getFacetMinLength() != null) ? ULong.valueOf(bbie.getFacetMinLength()) : null)
                    .set(BBIE.FACET_MAX_LENGTH, (bbie.getFacetMaxLength() != null) ? ULong.valueOf(bbie.getFacetMaxLength()) : null)
                    .set(BBIE.FACET_PATTERN, bbie.getFacetPattern())
                    .set(BBIE.FACET_MIN_INCLUSIVE, bbie.getFacetMinInclusive())
                    .set(BBIE.FACET_MIN_EXCLUSIVE, bbie.getFacetMinExclusive())
                    .set(BBIE.FACET_MAX_INCLUSIVE, bbie.getFacetMaxInclusive())
                    .set(BBIE.FACET_MAX_EXCLUSIVE, bbie.getFacetMaxExclusive())
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
                    .set(BBIE.CREATED_BY, ULong.valueOf(userId))
                    .set(BBIE.LAST_UPDATED_BY, ULong.valueOf(userId))
                    .set(BBIE.CREATION_TIMESTAMP, timestamp.toLocalDateTime())
                    .set(BBIE.LAST_UPDATE_TIMESTAMP, timestamp.toLocalDateTime())
                    .set(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(copiedTopLevelAsbiep.getTopLevelAsbiepId()))
                    .returning(BBIE.BBIE_ID).fetchOne().getValue(BBIE.BBIE_ID).toBigInteger();
        }

        private BigInteger insertBbieSc(BieCopyBbieSc bbieSc) {

            return dslContext.insertInto(BBIE_SC)
                    .set(BBIE_SC.GUID, ScoreGuid.randomGuid())
                    .set(BBIE_SC.PATH, bbieSc.getPath())
                    .set(BBIE_SC.HASH_PATH, bbieSc.getHashPath())
                    .set(BBIE_SC.BBIE_ID, ULong.valueOf(bbieSc.getBbieId()))
                    .set(BBIE_SC.BASED_DT_SC_MANIFEST_ID, ULong.valueOf(bbieSc.getBasedDtScManifestId()))
                    .set(BBIE_SC.DT_SC_PRI_RESTRI_ID, (bbieSc.getDtScPriRestriId() != null) ? ULong.valueOf(bbieSc.getDtScPriRestriId()) : null)
                    .set(BBIE_SC.CODE_LIST_MANIFEST_ID, (bbieSc.getCodeListManifestId() != null) ? ULong.valueOf(bbieSc.getCodeListManifestId()) : null)
                    .set(BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID, (bbieSc.getAgencyIdListManifestId() != null) ? ULong.valueOf(bbieSc.getAgencyIdListManifestId()) : null)
                    .set(BBIE_SC.DEFAULT_VALUE, bbieSc.getDefaultValue())
                    .set(BBIE_SC.FIXED_VALUE, bbieSc.getFixedValue())
                    .set(BBIE_SC.FACET_MIN_LENGTH, (bbieSc.getFacetMinLength() != null) ? ULong.valueOf(bbieSc.getFacetMinLength()) : null)
                    .set(BBIE_SC.FACET_MAX_LENGTH, (bbieSc.getFacetMaxLength() != null) ? ULong.valueOf(bbieSc.getFacetMaxLength()) : null)
                    .set(BBIE_SC.FACET_PATTERN, bbieSc.getFacetPattern())
                    .set(BBIE_SC.FACET_MIN_INCLUSIVE, bbieSc.getFacetMinInclusive())
                    .set(BBIE_SC.FACET_MIN_EXCLUSIVE, bbieSc.getFacetMinExclusive())
                    .set(BBIE_SC.FACET_MAX_INCLUSIVE, bbieSc.getFacetMaxInclusive())
                    .set(BBIE_SC.FACET_MAX_EXCLUSIVE, bbieSc.getFacetMaxExclusive())
                    .set(BBIE_SC.DEFINITION, bbieSc.getDefinition())
                    .set(BBIE_SC.EXAMPLE, bbieSc.getExample())
                    .set(BBIE_SC.REMARK, bbieSc.getRemark())
                    .set(BBIE_SC.BIZ_TERM, bbieSc.getBizTerm())
                    .set(BBIE_SC.CARDINALITY_MIN, bbieSc.getCardinalityMin())
                    .set(BBIE_SC.CARDINALITY_MAX, bbieSc.getCardinalityMax())
                    .set(BBIE_SC.IS_USED, (byte) ((bbieSc.isUsed()) ? 1 : 0))
                    .set(BBIE_SC.IS_DEPRECATED, (byte) ((bbieSc.isDeprecated()) ? 1 : 0))
                    .set(BBIE_SC.CREATED_BY, ULong.valueOf(userId))
                    .set(BBIE_SC.LAST_UPDATED_BY, ULong.valueOf(userId))
                    .set(BBIE_SC.CREATION_TIMESTAMP, timestamp.toLocalDateTime())
                    .set(BBIE_SC.LAST_UPDATE_TIMESTAMP, timestamp.toLocalDateTime())
                    .set(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(copiedTopLevelAsbiep.getTopLevelAsbiepId()))
                    .returning(BBIE_SC.BBIE_SC_ID).fetchOne().getValue(BBIE_SC.BBIE_SC_ID).toBigInteger();
        }
    }

}
